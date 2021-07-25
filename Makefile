OWNER_ID = "a596a4a9-214d-45c7-b918-d63cdd08582d"
IMAGE = "src/test/resources/pepper.jpeg"

upload:
	@curl --silent \
		--request POST \
		--form file="@$(IMAGE)" \
		"http://localhost:8080/documents/owners/$(OWNER_ID)" | jq

temporary-url:
	@curl --silent \
		--request GET \
		"http://localhost:8080/documents/owners/$(OWNER_ID)/$(FILENAME)" | jq

