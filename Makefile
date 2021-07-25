OWNER_ID = "a596a4a9-214d-45c7-b918-d63cdd08582d"
FILE = "src/test/resources/pepper.jpeg"

upload:
	@curl --silent \
		--request POST \
		--form file="@$(FILE)" \
		"http://localhost:8080/documents/owners/$(OWNER_ID)" | jq

temporary-url:
	@curl --silent \
		--request GET \
		"http://localhost:8080/documents/owners/$(OWNER_ID)/$(FILE)" | jq

LOCALSTACK_BUCKET = "example-company-s3-us-east-1"
LOCALSTACK_URL = "http://localhost:4566"
LOCALSTACK_PROFILE = "localstack"

list-files:
	@docker-compose --file ./infrastructure/docker-compose.yml \
		exec localstack \
		aws s3 ls s3://$(LOCALSTACK_BUCKET) \
		--recursive \
		--endpoint-url $(LOCALSTACK_URL) \
		--profile $(LOCALSTACK_PROFILE)

delete-files:
	@docker-compose --file ./infrastructure/docker-compose.yml \
		exec localstack \
		aws s3 rm s3://$(LOCALSTACK_BUCKET) \
		--recursive \
		--endpoint-url $(LOCALSTACK_URL) \
		--profile $(LOCALSTACK_PROFILE)
