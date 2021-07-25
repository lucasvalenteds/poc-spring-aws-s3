#!/bin/sh

aws_profile="localstack"
aws_access_key="admin"
aws_secret_key="12345"

aws_region="us-east-1"
aws_s3_bucket="example-company-s3-$aws_region"
aws_url="http://localhost:4566"

aws configure set aws_access_key_id "$aws_access_key" --profile "$aws_profile"
aws configure set aws_secret_access_key "$aws_secret_key" --profile "$aws_profile"
aws configure set default.region "$aws_region" --profile "$aws_profile"

bucket_exists=$(
    aws s3api head-bucket \
        --bucket "$aws_s3_bucket" \
        --profile "$aws_profile" \
        --endpoint-url "$aws_url" 2>&1
)

if [ -n "$bucket_exists" ]; then
    aws s3api create-bucket \
        --bucket "$aws_s3_bucket" \
        --profile "$aws_profile" \
        --endpoint-url "$aws_url"
fi

printf "AWS URL: %s\n" "$aws_url"
printf "S3 Bucket: %s\n" "$aws_s3_bucket"
