#! make

MAKEFLAGS += --silent
CURRENT_GIT_BRANCH:=$(shell git rev-parse --abbrev-ref HEAD)
IMAGE_NAME=ashigaru-health-backend
SRC_DIR=src
TEST_DIR=test

# General

# Build

build:
	rm -rf target
	clojure -X:project/uberjar
.PHONY: build


# Tests

format-check:
	clojure -M:format/check
.PHONY: format-check

format:
	clojure -M:format/fix
PHONY: format

lint:
	clj-kondo --parallel --lint $(SRC_DIR) $(TEST_DIR)
.PHONY: lint

test:
	clojure -M:test/env:test/midje
.PHONY: test

full-test: format-check test
.PHONY: full-test


# Deploy

docker-build: TAG=latest
docker-build:
	docker build --tag $(IMAGE_NAME):$(TAG) .
.PHONY: docker-build

deploy: full-test deploy-no-tests
.PHONY: deploy

deploy-no-tests: docker-build
	docker-compose up --detach
.PHONY: deploy-no-tests

heroku-deploy:
	git push --force heroku $(CURRENT_GIT_BRANCH):master
.PHONY: heroku-deploy
