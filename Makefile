#! make

MAKEFLAGS += --silent
GIT_BRANCH=$(shell git rev-parse --abbrev-ref HEAD)
IMAGE_NAME=ashigaru-health-backend

# General

install:
	lein deps
.PHONY: install

clean:
	lein clean
	rm -rf \
		pom.xml \
		.clj-condo
.PHONY: clean

run-dev:
	lein trampoline ring server-headless
.PHONY: run-dev

# Build

build:
	lein ring uberjar
.PHONY: build

# Tests

# format-check:
# .PHONY: format-check

# format:
# .PHONY: format

# lint:
# .PHONY: lint

# test:
# .PHONY: test

# full-test: format-check lint test
# .PHONY: full-test


# Deploy

docker-build: TAG=latest
docker-build:
	docker build --tag $(IMAGE_NAME):$(TAG) .
.PHONY: docker-build

deploy: docker-build
	docker-compose up --detach
.PHONY: deploy
