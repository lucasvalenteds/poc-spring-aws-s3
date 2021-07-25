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
| Run application | `./gradlew run` |

> Set environment variable `AWS_URL=http://localhost:4566` to use Localstack instead of AWS for manual testing.

### Infrastructure

| Description | Command |
| :--- | :--- |
| Provision S3 instance in Localstack | `make provision-s3-localstack` |
| Destroy S3 instance in Localstack | `make destroy-s3-localstack` |
| Provision S3 instance in AWS | `make provision-s3-aws` |
| Destroy S3 instance in AWS | `make destroy-s3-aws` |

> Commands to provision and destroy S3 instance in AWS requires [AWS CLI environment variables](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-envvars.html) to be defined.

### Manual testing

| Description | Command |
| :--- | :--- |
| Upload file | `make upload FILE=<filename>` |
| Create temporary URL | `make temporary-url FILE=<filename>` |
| List files | `make list-files` |
| Delete files | `make delete-files` |

