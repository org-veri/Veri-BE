#!/bin/bash

set -e

APP_NAME="veri-be"
IMAGE_NAME="ghcr.io/9oormthon-univ-veri/${APP_NAME}"
TAG=$(date +%Y%m%d-%H%M%S)

 ./gradlew clean build -x test
docker buildx inspect builder > /dev/null 2>&1 || docker buildx create --name builder --use

docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t ${IMAGE_NAME}:${TAG} \
  -t ${IMAGE_NAME}:latest \
  --push .
