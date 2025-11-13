.PHONY: build build-and-push run down ensure-builder

# 이미지/플랫폼 설정
APP_NAME="veri-be"
IMAGE="ghcr.io/org-veri/${APP_NAME}"
APP_TAG ?= latest
TIMESTAMP_TAG := $(shell date +%Y%m%d%H%M%S)

PLATFORMS    ?= linux/amd64,linux/arm64
DOCKERFILE   ?= ./Dockerfile
BUILD_CONTEXT?= .

build:
	./gradlew clean build -x test
	docker build -f $(DOCKERFILE) -t $(IMAGE):$(APP_TAG) $(BUILD_CONTEXT)
	docker tag $(IMAGE):$(APP_TAG) $(IMAGE):$(TIMESTAMP_TAG)
	@if [ "$(APP_TAG)" != "latest" ]; then \
	  docker tag $(IMAGE):$(APP_TAG) $(IMAGE):latest; \
	fi

BUILDER_NAME = builder
ensure-builder:
	@if ! docker buildx inspect $(BUILDER_NAME) >/dev/null 2>&1; then \
	  docker buildx create --name $(BUILDER_NAME) --use; \
	else \
	  docker buildx use $(BUILDER_NAME); \
	fi
	@docker buildx inspect --bootstrap >/dev/null

build-and-push: ensure-builder
	./gradlew clean build -x test
	@if [ "$(APP_TAG)" != "latest" ]; then \
	  EXTRA_TAGS="-t $(IMAGE):latest"; \
	else \
	  EXTRA_TAGS=""; \
	fi; \
	docker buildx build \
	  --platform $(PLATFORMS) \
	  -f $(DOCKERFILE) \
	  -t $(IMAGE):$(APP_TAG) \
	  -t $(IMAGE):$(TIMESTAMP_TAG) \
	  $$EXTRA_TAGS \
	  --push \
	  $(BUILD_CONTEXT)

.PHONY: clean clean-images


.PHONY: deploy
deploy:
	@ssh aws-very 'cd veri-be/ && ./deploy.sh'
