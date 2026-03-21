# ==============================
# Stage 1: dependencies
# ==============================
FROM maven:3.9.9-eclipse-temurin-21 AS dependencies

WORKDIR /backend

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B


# ==============================
# Stage 2: build
# ==============================
FROM dependencies AS build

WORKDIR /backend

COPY src/ src/

RUN ./mvnw clean package -DskipTests


# ==============================
# Stage 3: runtime
# ==============================
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /backend

COPY --from=build /backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]