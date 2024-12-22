FROM gradle:8.8-jdk17

WORKDIR /app

ARG VERSION
ARG GRADLE_BUILD_ARGS=
# these are missing in .env file but need to be present for application startup. Empty values are fine
ENV DEFAULT_ADMIN_EMAIL=
ENV DEFAULT_ADMIN_PASSWORD=

COPY ./src ./src
COPY ./build.gradle \
     ./settings.gradle \
     ./.env \
     ./

# -i: case-sensitive, s: first occurrence
RUN sed -i 's/VERSION/'${VERSION}'/' ./build.gradle

# for some reason tests don't work in docker container
RUN gradle clean build ${GRADLE_BUILD_ARGS} -x test


FROM openjdk:17-alpine

WORKDIR /app

ARG VERSION
ARG API_NAME
ENV JAR_FILE_NAME=${API_NAME}-${VERSION}.jar

COPY --from=0 /app/build/libs/${JAR_FILE_NAME} ./${JAR_FILE_NAME}
COPY ./.env \
     ./.env.* \
     ./

ENTRYPOINT java -jar ${JAR_FILE_NAME}