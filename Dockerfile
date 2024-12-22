FROM gradle:8.8-jdk17

WORKDIR /app

ARG VERSION
# skip tests by default because they need db. Set to '' in order to run tests
ARG GRADLE_BUILD_ARGS='-x test'
ENV DEFAULT_ADMIN_EMAIL=
ENV DEFAULT_ADMIN_PASSWORD=

COPY ./src ./src
COPY ./build.gradle ./build.gradle
COPY ./settings.gradle ./settings.gradle
COPY ./.env ./.env

# -i: case-sensitive, s: first occurrence
RUN sed -i 's/VERSION/'${VERSION}'/' ./build.gradle

RUN gradle clean build ${GRADLE_BUILD_ARGS}


FROM openjdk:17-alpine

WORKDIR /app

ARG VERSION
ARG API_NAME
ENV JAR_FILE_NAME=${API_NAME}-${VERSION}.jar

COPY --from=0 /app/build/libs/${JAR_FILE_NAME} ./${JAR_FILE_NAME}
COPY ./.env ./.env
COPY ./.env.version ./.env.version
COPY ./.env.secrets ./.env.secrets

ENTRYPOINT java -jar ${JAR_FILE_NAME}