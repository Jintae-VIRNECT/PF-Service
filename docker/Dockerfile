# Build Stage
FROM gradle:6.0.1-jdk8 AS builder
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

# Only copy dependency-related files
COPY build.gradle settings.gradle ./

# Copy Sourcefile
COPY virnect.pfx .
COPY src src

# build soruce file with dependnecies
RUN gradle cleanQuerydslSourcesDir
RUN gradle build -x test --stacktrace

# Runtime Stage
FROM openjdk:8-jre-alpine

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
ARG buildDir=$APP_HOME/build/unpack

COPY --from=builder ${APP_HOME}/virnect.pfx .
COPY --from=builder ${buildDir}/lib BOOT-INF/lib
COPY --from=builder ${buildDir}/app .

EXPOSE 8632
CMD ["java", "-Xms400M", "-Xmx400M", "org.springframework.boot.loader.JarLauncher"]