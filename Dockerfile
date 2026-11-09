# ---------- 1) Compilar el frontend Angular ----------
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---------- 2) Compilar el backend Spring Boot (con el frontend dentro) ----------
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -q dependency:go-offline
COPY backend/src ./src
# Spring Boot sirve automáticamente lo que haya en resources/static
COPY --from=frontend /app/dist/frontend/browser ./src/main/resources/static
RUN mvn -q package -DskipTests

# ---------- 3) Imagen final, solo con el JRE ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/api-1.0.0.jar app.jar
# Los servicios de hosting suelen indicar el puerto con la variable PORT
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
