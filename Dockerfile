FROM gradle:17 as BUILD
WORKDIR /build
COPY --chown=gradle:gradle src /build/src
COPY --chown=gradle:gradle build.gradle settings.gradle /build
RUN gradle --no-daemon shadowJar

FROM openjdk:17-slim
WORKDIR /app
COPY --from=BUILD /build/build/libs/beeapi.jar beeapi.jar
ENTRYPOINT java -jar beeapi.jar
EXPOSE 5050
