# Build stage
FROM eclipse-temurin:26-jdk-noble AS build
WORKDIR /app

# Copy the maven wrapper and pom file first
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# ADD THIS LINE TO FIX THE PERMISSION DENIED ERROR
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

# Copy source code and build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:26-jre-noble
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
