#! make

# MAKEFLAGS += --silent
CURRENT_GIT_BRANCH:=$(shell git rev-parse --abbrev-ref HEAD)
CLJ_KONDO_BIN_EXISTS:=$(shell command -v clj-kondo 2> /dev/null)
IMAGE_NAME=ashigaru-health-backend


# General

install:
	lein deps
.PHONY: install

clean:
	lein clean
	rm -rf \
		target \
		pom.xml
.PHONY: clean

run-dev:
	lein trampoline ring server-headless
.PHONY: run-dev


# Build

build:
	lein ring uberjar
.PHONY: build


# Tests

format-check:
	lein cljfmt check
.PHONY: format-check

format:
	lein cljfmt fix
PHONY: format

clj-kondo-setup:
ifdef CLJ_KONDO_BIN_EXISTS
	clj-kondo --parallel --lint $(shell lein classpath)
else
	lein clj-kondo --parallel --lint $(shell lein classpath)
endif
.PHONY: clj-kondo-setup

lint:
ifdef CLJ_KONDO_BIN_EXISTS
	clj-kondo --lint src
else
	lein clj-kondo --lint src
endif
.PHONY: lint

test:
.PHONY: test

full-test: format-check lint test
.PHONY: full-test


# Deploy

docker-build: TAG=latest
docker-build:
	docker build --tag $(IMAGE_NAME):$(TAG) .
.PHONY: docker-build

deploy: full-test deploy-no-test
.PHONY: deploy

deploy-no-test: docker-build
	docker-compose up --detach
.PHONY: deploy-no-test

heroku-deploy:
	git push --force heroku $(CURRENT_GIT_BRANCH):master
.PHONY: heroku-deploy
