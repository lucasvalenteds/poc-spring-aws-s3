provision-s3-aws:
	@terraform init
	@terraform validate
	@terraform apply -auto-approve \
		-var access_key="$(AWS_ACCESS_KEY_ID)" \
		-var secret_key="$(AWS_SECRET_ACCESS_KEY)"

destroy-s3-aws:
	@terraform destroy -auto-approve \
		-var access_key="$(AWS_ACCESS_KEY_ID)" \
		-var secret_key="$(AWS_SECRET_ACCESS_KEY)"

provision-s3-localstack:
	@docker-compose up \
		--detach \
		--force-recreate \
		--renew-anon-volumes \
		--remove-orphans

destroy-s3-localstack:
	@docker-compose down \
		--remove-orphans \
		--volumes

inspect-s3-localstack:
	@docker-compose logs --follow

list-files:
	@docker-compose exec localstack \
		aws s3 ls s3://example-company-s3-us-east-1 \
		--recursive \
		--endpoint-url http://localhost:4566 \
		--profile localstack

delete-files:
	@docker-compose exec localstack \
		aws s3 rm s3://example-company-s3-us-east-1 \
		--recursive \
		--endpoint-url http://localhost:4566 \
		--profile localstack
