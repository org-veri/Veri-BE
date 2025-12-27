import {GetObjectCommand, PutObjectCommand, S3Client} from "@aws-sdk/client-s3";
import sharp from "sharp";

const s3 = new S3Client({});
const LONG_EDGE = parseInt(process.env.LONG_EDGE || "2000", 10);

const SKEW_SEARCH_MIN = parseFloat(process.env.SKEW_SEARCH_MIN || "-5");
const SKEW_SEARCH_MAX = parseFloat(process.env.SKEW_SEARCH_MAX || "5");
const SKEW_SEARCH_STEP = parseFloat(process.env.SKEW_SEARCH_STEP || "0.5");
const SKEW_DOWNSCALE = parseInt(process.env.SKEW_DOWNSCALE || "1000", 10);

/** @param {import('stream').Readable} stream */
const streamToBuffer = (stream) =>
    new Promise((resolve, reject) => {
        const chunks = [];
        stream.on("data", (chunk) => chunks.push(chunk));
        stream.on("error", reject);
        stream.on("end", () => resolve(Buffer.concat(chunks)));
    });

/**
 * 가로 투영 분산 계산
 * @param {Uint8Array} pixels
 * @param {number} width
 * @param {number} height
 */
function horizontalProjectionVariance(pixels, width, height) {
    const rowSums = new Float64Array(height);
    for (let y = 0; y < height; y++) {
        let sum = 0;
        const rowStart = y * width;
        for (let x = 0; x < width; x++) {
            sum += pixels[rowStart + x] > 0 ? 1 : 0; // 이진화(0/255) 가정
        }
        rowSums[y] = sum;
    }
    let mean = 0;
    for (let i = 0; i < height; i++) mean += rowSums[i];
    mean /= height;

    let variance = 0;
    for (let i = 0; i < height; i++) {
        const d = rowSums[i] - mean;
        variance += d * d;
    }
    return variance / height;
}

/**
 * 스큐 각도 추정: 분산 최대화 각도 찾기
 * @param {Buffer} inputBuffer
 * @returns {Promise<number>}
 */
async function estimateSkewAngleWithSharp(inputBuffer) {
    const base = sharp(inputBuffer, {animated: false})
        .grayscale()
        .resize({
            width: SKEW_DOWNSCALE,
            height: SKEW_DOWNSCALE,
            fit: "inside",
            withoutEnlargement: true,
        });

    const meta = await base.metadata();
    if (!meta.width || !meta.height) return 0;

    const bg = {r: 255, g: 255, b: 255, alpha: 1};
    let bestAngle = 0;
    let bestScore = -Infinity;

    for (let angle = SKEW_SEARCH_MIN; angle <= SKEW_SEARCH_MAX + 1e-9; angle += SKEW_SEARCH_STEP) {
        const rotated = base.clone().rotate(angle, {background: bg}).threshold(128);
        const raw = await rotated.toColourspace("b-w").raw().toBuffer({resolveWithObject: true});

        const width = raw.info.width;
        const height = raw.info.height;
        const pixels = new Uint8Array(raw.data.buffer, raw.data.byteOffset, raw.data.byteLength);

        const score = horizontalProjectionVariance(pixels, width, height);
        if (score > bestScore) {
            bestScore = score;
            bestAngle = angle;
        }
    }
    return bestAngle;
}

export const handler = async (event) => {
    for (const rec of event.Records) {
        const bucket = rec.s3.bucket.name;
        const key = decodeURIComponent(rec.s3.object.key.replace(/\+/g, " "));

        if (!key.startsWith("public/ocr/")) continue;

        try {
            // 1) 원본 다운로드
            const response = await s3.send(new GetObjectCommand({Bucket: bucket, Key: key}));
            const imageBuffer = await streamToBuffer(response.Body);

            // 2) 스큐 추정 → 전처리
            const angle = await estimateSkewAngleWithSharp(imageBuffer);
            console.log(`Estimated skew angle: ${angle.toFixed(2)}°`);

            const processedImageBuffer = await sharp(imageBuffer, {animated: false})
                .rotate(angle, {background: {r: 255, g: 255, b: 255, alpha: 1}})
                .resize({
                    width: LONG_EDGE,
                    height: LONG_EDGE,
                    fit: "inside",
                    withoutEnlargement: true,
                })
                .grayscale()
                .sharpen()
                .threshold(128)
                .jpeg({quality: 90, progressive: true})
                .toBuffer();

            // 3) 업로드
            const filename = key.split("/").pop().split(".").slice(0, -1).join(".");
            const destKey = `public/ocr-preprocessed/${filename}.jpg`;

            await s3.send(
                new PutObjectCommand({
                    Bucket: bucket,
                    Key: destKey,
                    Body: processedImageBuffer,
                    ContentType: "image/jpeg",
                })
            );

            console.log(`Successfully processed ${key} (deskew=${angle.toFixed(2)}°) → ${destKey}`);
        } catch (error) {
            console.error(`Error processing image ${key}:`, error);
        }
    }
};
