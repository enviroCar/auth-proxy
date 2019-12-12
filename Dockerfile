FROM maven:3-jdk-8-alpine AS BUILDER

COPY . /app

WORKDIR /app

RUN mvn --batch-mode --errors --fail-fast \
  --define maven.javadoc.skip=true \
  --define skipTests=true install

FROM java:8-jre-alpine

ARG VERSION=0.0.1-SNAPSHOT

COPY --from=BUILDER /app/target/auth-proxy-${VERSION}.jar /app.jar

EXPOSE 9999

HEALTHCHECK --interval=10s --timeout=5s --retries=5 CMD \
  wget http://localhost:${SERVER_PORT:-9999}${AUTH_PROXY_CONTEXT_PATH:-/}/login -q -O - > /dev/null 2>&1
  
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
