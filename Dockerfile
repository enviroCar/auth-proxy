FROM maven:3-jdk-8-alpine AS BUILDER

COPY . /app

WORKDIR /app

RUN mvn --batch-mode --errors --fail-fast \
  --define maven.javadoc.skip=true \
  --define skipTests=true install

FROM java:8-jre-alpine

ARG VERSION

COPY --from=BUILDER /app/target/auth-proxy-${VERSION}.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]