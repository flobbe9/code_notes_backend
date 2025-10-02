ARG VERSION

FROM eclipse-temurin:21-jdk-alpine-3.22

WORKDIR /app

ARG VERSION

COPY ./src ./src
COPY ./gradle ./gradle 
COPY ./build.gradle \
     ./settings.gradle \
     ./gradle ./gradle \
     ./gradlew \
     ./gradlew.bat \
     ./.env \
     ./.env.* \
     ./

# -i: case-sensitive, s: first occurrence
RUN sed -i 's/VERSION/'${VERSION}'/' ./build.gradle

RUN ./gradlew clean build


FROM eclipse-temurin:21-jdk-alpine-3.22

WORKDIR /app

ARG VERSION
ARG API_NAME
ENV JAR_FILE_NAME=${API_NAME}-${VERSION}.jar
ARG RUNTIME_ARGS=''
ENV RUNTIME_ARGS=${RUNTIME_ARGS}

COPY --from=0 /app/build/libs/${JAR_FILE_NAME} ./${JAR_FILE_NAME}
# NOTE: don't include .env.pipeline here since it would take precedence over docker-compose environment
COPY ./.env \
     ./.env.version \
     # just for local development
     ./.env.secret[s] \
     ./

ENTRYPOINT java -jar ${RUNTIME_ARGS} ${JAR_FILE_NAME}