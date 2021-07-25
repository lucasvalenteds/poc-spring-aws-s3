terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "3.51.0"
    }
  }
}

variable "access_key" {
  type = string
}

variable "secret_key" {
  type = string
}

variable "region" {
    default = "us-east-1"
}

variable "bucket_name" {
    default = "example-company-s3"
}

provider "aws" {
    access_key = var.access_key
    secret_key = var.secret_key
    region = var.region
}

resource "aws_s3_bucket" "poc_spring_aws_s3_bucket" {
    bucket = var.bucket_name
    acl = "public-read-write"
}

