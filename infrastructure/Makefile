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

show-localstack-logs:
	@docker-compose logs --follow

