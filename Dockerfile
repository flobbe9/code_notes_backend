ARG VERSION
ARG API_NAME


### Build ###
FROM gradle:8.8-jdk17

WORKDIR /app

ARG VERSION

COPY ./src ./src
COPY ./build.gradle ./build.gradle
COPY ./settings.gradle ./settings.gradle

# -i: case-sensitive, s: first occurrence
RUN sed -i 's/VERSION/'${VERSION}'/' ./build.gradle

# skip tests because db is not running at build time
RUN gradle clean build -x test


### Run ###
FROM openjdk:17-alpine

WORKDIR /app

ARG VERSION
ARG API_NAME
ENV TZ='Europe/Berlin'
ENV JAR_FILE_NAME=${API_NAME}-${VERSION}.jar

COPY --from=0 /app/build/libs/${JAR_FILE_NAME} ./${JAR_FILE_NAME}
COPY ./.env ./.env
COPY ./.env.version ./.env.version
COPY ./.env.secrets ./.env.secrets

ENTRYPOINT java -jar ${JAR_FILE_NAME}