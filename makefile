.PHONY: build buildx-and-push run down ensure-builder build-and-push-dev

# 이미지/플랫폼 설정
APP_NAME=veri-be
IMAGE=ghcr.io/org-veri/${APP_NAME}
APP_TAG ?= latest
TIMESTAMP_TAG := $(shell date +%Y%m%d%H%M%S)

PLATFORMS    ?= linux/amd64,linux/arm64
DOCKERFILE   ?= ./Dockerfile
BUILD_CONTEXT?= .

build:
	./gradlew :core:core-api:clean :core:core-api:build -x test
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

buildx-and-push: ensure-builder
	./gradlew :core:core-api:clean :core:core-api:build -x test
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

.PHONY: deploy deploy-blue deploy-green check-green-health
deploy-blue:
	@echo "Deploying to \033[94mBLUE Server\033[0m..."
	@ssh aws-very 'cd workspace/veri-be && ./deploy.sh'

deploy-green:
	@echo "Deploying to \033[32mGREEN Server\033[0m..."
	@ssh oracle 'cd workspace/app/veri-be/ && ./deploy.sh'

check-green-health:
	@echo "Checking \033[32mGREEN Server\033[0m Health..."
	@ssh oracle ' \
        while true; do \
            if curl -sSf http://localhost:8080/actuator/health | grep -q "UP"; then \
                break; \
            else \
                sleep 5; \
            fi; \
        done \
    '
	@echo "\033[32mGREEN Server\033[0m is healthy!"

deploy: deploy-green check-green-health deploy-blue
