FROM clojure AS building

WORKDIR /usr/src
COPY deps.edn .
# cache dependencies
RUN ["clojure", "-X:project/jar"]

COPY resources resources
COPY src src

RUN ["clojure", "-X:project/uberjar"]


FROM openjdk:17-alpine

WORKDIR /usr/app
COPY --from=building /usr/src/target/app.jar .

HEALTHCHECK --interval=10s --timeout=10s --retries=20 \
  CMD wget localhost:$PORT/healthz --spider

EXPOSE $PORT

ENTRYPOINT ["java", "-jar", "app.jar"]
