FROM openjdk:21-jdk-slim AS build
WORKDIR /app

COPY gradle/ gradle/
COPY gradle.properties build.gradle.kts settings.gradle gradlew ./
COPY backend/src backend/src
COPY backend/build.gradle.kts backend/
COPY lib/src lib/src
COPY lib/build.gradle.kts lib/

RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon :backend:bootJar

# Now create a minimal build with only the bootJar
FROM openjdk:21-jdk-slim
WORKDIR /app
RUN mkdir osm
RUN mkdir graph-cache
COPY --from=build /app/backend/build/libs/timestamp.jar /app/timestamp.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/timestamp.jar"]
