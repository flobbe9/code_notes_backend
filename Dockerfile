FROM eclipse-temurin:21-jdk-alpine-3.22

WORKDIR /app

ARG JAVA_RUNTIME_ARGS
ENV JAVA_RUNTIME_ARGS=${JAVA_RUNTIME_ARGS}

COPY ./src ./src
COPY ./gradle ./gradle 

# uncomment for faster build, comment out in .dockerignore, dev only
COPY ./build ./build

COPY ./build.gradle \
     ./settings.gradle \
     ./gradlew \
     ./gradlew.bat \
     ./.env \
     ./.env.version \
     ./.env.loca[l] \
     ./

# make gradle wrapper executable
# comment out for faster build, dev only
# RUN chmod +x ./gradlew
# RUN ./gradlew clean build -Pci

# switch for faster build, dev only
# ENTRYPOINT ./gradlew bootRun -PjavaRuntimeArgs="${JAVA_RUNTIME_ARGS}"
ENTRYPOINT java -jar build/libs/code_notes_backend-VERSION.jar