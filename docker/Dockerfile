# Build Stage
FROM gradle:6.8-jdk8 AS builder
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

# Only copy dependency-related files
COPY build.gradle settings.gradle ./

# Copy Sourcefile
COPY virnect.pfx .

# Donwload dependencies. docker layer caching
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

COPY src src

# build soruce file with dependnecies
RUN gradle clean build -x test --console=plain

RUN java -Djarmode=layertools -jar build/libs/*.jar extract

# Runtime Stage
FROM openjdk:8-jre-alpine

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

COPY --from=builder ${APP_HOME}/virnect.pfx ./
COPY --from=builder ${APP_HOME}/dependencies/ ./
COPY --from=builder ${APP_HOME}/spring-boot-loader/ ./
COPY --from=builder ${APP_HOME}/snapshot-dependencies/ ./
COPY --from=builder ${APP_HOME}/application/ ./

EXPOSE 8632
CMD ["java", "-Xms400M", "-Xmx400M", "org.springframework.boot.loader.JarLauncher"]