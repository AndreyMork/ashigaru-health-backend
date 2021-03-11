FROM clojure:openjdk-17-alpine AS build-image

WORKDIR /usr/src

COPY project.clj ./
# RUN ["lein", "deps"]

COPY src src
RUN ["lein", "ring", "uberjar"]


FROM openjdk:17-alpine

WORKDIR /usr/app

COPY --from=build-image /usr/src/target/app.jar .

ENV PORT 3000

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]
