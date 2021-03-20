# FROM clojure:openjdk-17-alpine AS build-image
FROM clojure AS build-image

WORKDIR /usr/src
COPY project.clj ./
COPY src src
COPY resources resources

RUN ["lein", "uberjar"]


FROM openjdk:17-alpine

WORKDIR /usr/app
COPY --from=build-image /usr/src/target/app.jar .

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]
