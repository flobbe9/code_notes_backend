FROM gradle:8.8-jdk17

WORKDIR /app

ARG VERSION
ARG GRADLE_BUILD_ARGS

COPY ./src ./src
COPY ./build.gradle \
     ./settings.gradle \
     ./.env \
     ./.env.* \
     ./

# -i: case-sensitive, s: first occurrence
RUN sed -i 's/VERSION/'${VERSION}'/' ./build.gradle

# for some reason tests don't work in docker container
RUN gradle clean build ${GRADLE_BUILD_ARGS} -x test


FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

ARG VERSION
ARG API_NAME
ENV JAR_FILE_NAME=${API_NAME}-${VERSION}.jar
ARG DB_HOST
ENV DB_HOST=${DB_HOST}

COPY --from=0 /app/build/libs/${JAR_FILE_NAME} ./${JAR_FILE_NAME}
# NOTE: don't include .env.secrets.pipeline here since it would take precedence over docker-compose environment
COPY ./.env \
     ./.env.version \
     ./.env.secret[s] \
     ./

ENTRYPOINT java -jar ${JAR_FILE_NAME}