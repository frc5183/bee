FROM openjdk:17-slim
RUN mkdir /app
COPY . /app
WORKDIR /app
RUN ./gradlew shadowJar
RUN mkdir /app/work
COPY ./build/libs/beeapi.jar /app/work
WORKDIR /app/work
CMD ["java", "-jar", "./beeapi.jar"]
EXPOSE 5050
