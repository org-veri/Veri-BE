import {
  S3Client,
  GetObjectCommand,
  PutObjectCommand,
} from "@aws-sdk/client-s3";
import sharp from "sharp";

const s3 = new S3Client({});
const LONG_EDGE = parseInt(process.env.LONG_EDGE || "2000", 10);

const streamToBuffer = (stream) =>
  new Promise((resolve, reject) => {
    const chunks = [];
    stream.on("data", (chunk) => chunks.push(chunk));
    stream.on("error", reject);
    stream.on("end", () => resolve(Buffer.concat(chunks)));
  });

export const handler = async (event) => {
  for (const rec of event.Records) {
    const bucket = rec.s3.bucket.name;
    const key = decodeURIComponent(rec.s3.object.key.replace(/\+/g, " "));

    if (!key.startsWith("public/ocr/")) {
      continue;
    }

    try {
      // 1. S3에서 원본 이미지 다운로드
      const getObjectParams = { Bucket: bucket, Key: key };
      const response = await s3.send(new GetObjectCommand(getObjectParams));
      const imageBuffer = await streamToBuffer(response.Body);

      // 2. sharp 라이브러리로 이미지 전처리
      const processedImageBuffer = await sharp(imageBuffer, { animated: false })
        .resize({
          width: LONG_EDGE,
          height: LONG_EDGE,
          fit: "inside",
          withoutEnlargement: true,
        }) // 리사이즈
        .grayscale() // 그레이스케일
        .sharpen() // 샤프닝
        .threshold(128) // 이진화
        .jpeg({ quality: 90, progressive: true }) // JPEG 포맷으로 변환
        .toBuffer();

      // 3. 처리된 이미지를 S3에 업로드
      const filename = key.split("/").pop().split(".").slice(0, -1).join(".");
      const destKey = `public/ocr-preprocessed/${filename}.jpg`;

      const putObjectParams = {
        Bucket: bucket,
        Key: destKey,
        Body: processedImageBuffer,
        ContentType: "image/jpeg",
      };
      await s3.send(new PutObjectCommand(putObjectParams));

      console.log(`Successfully processed ${key} and uploaded to ${destKey}`);
    } catch (error) {
      console.error(`Error processing image ${key}:`, error);
    }
  }
};
