# POC: Spring AWS S3

It demonstrates how to upload files to a S3 bucket and create a temporary download link to them.

The goal is to develop a web service that receives files such as images and PDFs and sends them to a S3 bucket. The web service should allow the user to generate a temporary URL to download the file or open it in applications such as the browser or image viewer.

The web service should choose the S3 bucket based on the environment. When running in production the files should be stored on a bucket managed by AWS. When running in development or testing the files should be stored in a bucket managed by a local Localstack instance running inside a Docker container. The container should be provisioned automatically before starting integration tests and automatically destroyed when finished.

The web service configuration should be informed via environment variables and fallback to properties file.

## How to run

### Software

| Description | Command |
| :--- | :--- |
| Run tests | `./gradlew test` |
| Run application (AWS) | `./gradlew run --args='dev'` |
| Run application (Localstack) | `./gradlew run --args='local'` |

> Running targeting AWS requires [AWS CLI environment variables](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-envvars.html) to be defined.

### Infrastructure

| Description | Command |
| :--- | :--- |
| Provision S3 instance in AWS | `make provision-s3-aws` |
| Destroy S3 instance in AWS | `make destroy-s3-aws` |
| Provision S3 instance in Localstack | `make provision-s3-localstack` |
| Destroy S3 instance in Localstack | `make destroy-s3-localstack` |
| Show Localstack logs | `make show-localstack-logs` |

> Commands to provision and destroy S3 instance in AWS requires [AWS CLI environment variables](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-envvars.html) to be defined.

### Manual testing

| Description | Command |
| :--- | :--- |
| Upload file | `make upload FILE=<filename>` |
| Create temporary URL | `make temporary-url FILE=<filename>` |
| List files | `make list-files` |
| Delete files | `make delete-files` |

> The parameter `FILE=<filename>` of tasks to upload file and create temporary URL is optional.

## Preview

```
$ make upload FILE=example.gif 
{
  "url": "document-service/uploads/a596a4a9-214d-45c7-b918-d63cdd08582d/082988d6-778b-44c1-b515-bad12503f639.gif"
}
```

```
$ make temporary-url FILE=082988d6-778b-44c1-b515-bad12503f639.gif
{
  "url": "http://localhost:4566/example-company-s3-us-east-1/document-service/uploads/a596a4a9-214d-45c7-b918-d63cdd08582d/082988d6-778b-44c1-b515-bad12503f639.gif?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210725T161626Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=admin%2F20210725%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=43445856f8521d27c42bd916f2832d71b69bef7f4e82f05ec67e4846a6eab126"
}
```

```
$ make list-files 
2021-07-25 16:16:14     176728 document-service/uploads/a596a4a9-214d-45c7-b918-d63cdd08582d/082988d6-778b-44c1-b515-bad12503f639.gif
2021-07-25 16:15:50      35511 document-service/uploads/a596a4a9-214d-45c7-b918-d63cdd08582d/b884067c-c3f8-4721-b2be-19d11f2c43c7.jpeg
2021-07-25 16:15:59      35511 document-service/uploads/a596a4a9-214d-45c7-b918-d63cdd08582d/f982f1b9-4b39-4a79-b950-daeac74279b3.jpeg
```

```
$ make delete-files 
delete: s3://example-company-s3-us-east-1/document-service/uploads/a596a4a9-214d-45c7-b918-d63cdd08582d/082988d6-778b-44c1-b515-bad12503f639.gif
delete: s3://example-company-s3-us-east-1/document-service/uploads/a596a4a9-214d-45c7-b918-d63cdd08582d/f982f1b9-4b39-4a79-b950-daeac74279b3.jpeg
delete: s3://example-company-s3-us-east-1/document-service/uploads/a596a4a9-214d-45c7-b918-d63cdd08582d/b884067c-c3f8-4721-b2be-19d11f2c43c7.jpeg
```

