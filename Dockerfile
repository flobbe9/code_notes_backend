FROM gradle:8.8-jdk17

WORKDIR /app

ARG VERSION
ARG GRADLE_BUILD_ARGS

COPY ./src ./src
COPY ./build.gradle \
     ./settings.gradle \
     ./.env \
     ./.env.secrets.pipeline \
     ./

# -i: case-sensitive, s: first occurrence
RUN sed -i 's/VERSION/'${VERSION}'/' ./build.gradle

# for some reason tests don't load jdbc driver when run in docker container
RUN gradle clean build ${GRADLE_BUILD_ARGS} -x test


FROM openjdk:17-alpine

WORKDIR /app

ARG VERSION
ARG API_NAME
ENV JAR_FILE_NAME=${API_NAME}-${VERSION}.jar
ARG DB_HOST
ENV DB_HOST=${DB_HOST}

COPY --from=0 /app/build/libs/${JAR_FILE_NAME} ./${JAR_FILE_NAME}
COPY ./.env \
     ./.env.* \
     ./

ENTRYPOINT java -jar ${JAR_FILE_NAME}