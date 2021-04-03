# set dotenv-load := false

src-dir := "src"
test-dir := "test"
image-name := "ashigaru-health-backend"

# General

default:
  @just --list

run:
  APP_DB_HOST="" \
  clojure --main ashigaru-health.main

# Build

jar:
 clojure -X:env/dev:env/test:project/jar

build:
  clojure -X:project/uberjar


# Tests

format-check:
  clojure -M:format/check

format:
  clojure -M:format/fix

lint:
  clj-kondo --parallel --lint {{src-dir}} {{test-dir}}

test +args="":
  clojure -A:env/test -m kaocha.runner {{args}}

full-test: format-check test


# Deploy

docker-build tag="latest":
  docker build --tag {{image-name}}:{{tag}} .

deploy: docker-build
  docker-compose up --detach

heroku-deploy:
  git push --force heroku HEAD:master
