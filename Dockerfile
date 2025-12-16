ARG VERSION='0.0.1-SNAPSHOT'

FROM eclipse-temurin:21-jdk-alpine-3.22 as build

WORKDIR /app

ARG VERSION='0.0.1-SNAPSHOT'

COPY ./src ./src
COPY ./gradl[e] ./gradle

COPY ./build.gradle \
     ./settings.gradle \
     ./gradlew \
     ./gradlew.bat \
     ./.env \
     ./.env.version \
     ./.env.loca[l] \
     ./

# uncomment for faster build
# COPY ./build ./build

RUN sed -i 's/VERSION/'${VERSION}'/' ./build.gradle
     
# comment out for faster build, dev only
# make gradle wrapper executable
RUN chmod +x ./gradlew
RUN ./gradlew clean build -Pci


FROM eclipse-temurin:21-jdk-alpine-3.22

WORKDIR /app

ARG VERSION='0.0.1-SNAPSHOT'
ENV VERSION=${VERSION}
ARG JAVA_RUNTIME_ARGS
ENV JAVA_RUNTIME_ARGS=${JAVA_RUNTIME_ARGS}
ENV JAR_FILE_NAME=code_notes_backend-${VERSION}.jar

COPY --from=build /app/build/libs/${JAR_FILE_NAME} \
     /app/.env \
     /app/.env.version \
     /app/.env.loca[l] \
     ./

ENTRYPOINT java -jar ${JAR_FILE_NAME} ${JAVA_RUNTIME_ARGS}