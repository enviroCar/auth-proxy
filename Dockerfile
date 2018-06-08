FROM java:8
MAINTAINER Arne de Wall <a.dewall@52north.org>

ARG JAR_FILE

ADD target/${JAR_FILE} app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]