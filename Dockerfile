FROM eclipse-temurin:21-jdk-alpine-3.22

WORKDIR /app

ARG API_NAME
ARG JAVA_RUNTIME_ARGS
ENV JAVA_RUNTIME_ARGS=${JAVA_RUNTIME_ARGS}

COPY ./src ./src
COPY ./gradle ./gradle 
COPY ./build.gradle \
     ./settings.gradle \
     ./gradle ./gradle \
     ./gradlew \
     ./gradlew.bat \
     ./.env \
     ./.env.version \
     # just for local development
     ./.env.secret[s] \
     ./

# make gradle wrapper executable
RUN chmod +x ./gradlew
RUN ./gradlew clean build -Pci

ENTRYPOINT ./gradlew bootRun -PjavaRuntimeArgs="${JAVA_RUNTIME_ARGS}"