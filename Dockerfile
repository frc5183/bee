FROM openjdk:20-slim
WORKDIR /app
COPY . .
RUN ./gradlew build
RUN mkdir work
COPY ./build/libs/beeapi.jar /app
WORKDIR /app/work
CMD ["java", "-jar", "../beeapi.jar"]
EXPOSE 5050