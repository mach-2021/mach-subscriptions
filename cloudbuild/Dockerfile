FROM gradle:6.7.0-jdk11 as builder
WORKDIR /home/gradle/project
COPY mach-subscriptions/. .
RUN ls -l
RUN gradle --parallel --build-cache --no-daemon bootJar

FROM azul/zulu-openjdk-alpine:11
RUN apk add -u musl
RUN adduser -D appuser
USER appuser
WORKDIR /home/appuser
COPY --from=builder --chown=101:101 /home/gradle/project/subscriptions/build/libs/*.jar /home/appuser/app.jar
