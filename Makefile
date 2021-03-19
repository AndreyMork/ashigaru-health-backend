#! make

MAKEFLAGS += --silent
CURRENT_GIT_BRANCH:=$(shell git rev-parse --abbrev-ref HEAD)
CLJ_KONDO_BIN_EXISTS:=$(shell command -v clj-kondo 2> /dev/null)
IMAGE_NAME=ashigaru-health-backend
SRC_DIR=src
TEST_DIR=test

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

run-dev: PORT=3000
run-dev:
	PORT=$(PORT) \
	lein trampoline run
.PHONY: run-dev

run-dev-watch:
	ls $(SRC_DIR)/**/*.clj | entr -r make run-dev
.PHONY: run-dev


# Build

build:
	lein uberjar
.PHONY: build


# Tests

format-check:
	lein cljfmt check
.PHONY: format-check

format:
	lein cljfmt fix
PHONY: format

clj-kondo-setup:
	rm -rf .clj-kondo/.cache
ifdef CLJ_KONDO_BIN_EXISTS
	clj-kondo --parallel --lint $(shell lein classpath)
else
	lein clj-kondo --parallel --lint $(shell lein classpath)
endif
.PHONY: clj-kondo-setup

lint:
ifdef CLJ_KONDO_BIN_EXISTS
	clj-kondo --parallel --lint $(SRC_DIR) $(TEST_DIR)
else
	lein clj-kondo --parallel --lint $(SRC_DIR) $(TEST_DIR)
endif
.PHONY: lint

test:
	lein midje
.PHONY: test

test-coverage:
	lein cloverage --runner :midje
.PHONY: test-coverage

full-test: format-check test
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
