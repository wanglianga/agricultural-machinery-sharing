FROM node:18-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM maven:3.8.6-openjdk-17-slim AS backend-builder
WORKDIR /backend
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=backend-builder /backend/target/*.jar app.jar
COPY --from=frontend-builder /frontend/dist /app/static

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
