FROM openjdk:8-jre-alpine

ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME

COPY ./build/libs/PF-License-1.0.0.jar ./PF-License-1.0.0.jar

EXPOSE 8632

## Command executed when container is start up.
CMD ["java", "-Xmx200M", "-jar","PF-License-1.0.0.jar"]
