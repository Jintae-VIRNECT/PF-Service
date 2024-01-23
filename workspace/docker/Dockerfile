# Build Stage
FROM openjdk:8 AS builder

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
COPY virnect.pfx .

RUN chmod +x ./gradlew
RUN ./gradlew cleanQuerydslSourcesDir
RUN ./gradlew build -x test

# Runtime Stage
FROM openjdk:8-jre-alpine

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
ARG buildDir=build/unpack

RUN mkdir -p metadata
ADD metadata /usr/app/metadata

COPY --from=builder virnect.pfx .
COPY --from=builder ${buildDir}/lib BOOT-INF/lib
COPY --from=builder ${buildDir}/app .

EXPOSE 8082
CMD ["java", "-Xms400M", "-Xmx400M", "org.springframework.boot.loader.JarLauncher"]