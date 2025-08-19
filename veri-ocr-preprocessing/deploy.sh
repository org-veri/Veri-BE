#!/bin/bash

zip -r ../veri-ocr-preprocessing.zip . \
  -x ".vscode/*" "deploy.sh" "layer/**" "*.zip"
aws s3 cp ../veri-ocr-preprocessing.zip s3://3-veri-s3-bucket/veri-ocr-preprocessing.zip
