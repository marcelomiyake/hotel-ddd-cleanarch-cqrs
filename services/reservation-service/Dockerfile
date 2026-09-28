FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY services ./services
RUN mvn -B -f services/pom.xml -pl reservation-service -am package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
COPY --from=build /workspace/services/reservation-service/target/reservation-service-1.0.0.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
