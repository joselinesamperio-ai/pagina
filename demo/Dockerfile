# ---- Etapa 1: compilar ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
# Por si gradlew llegó con saltos de línea de Windows (CRLF)
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# ---- Etapa 2: ejecutar ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar
# En producción la consola H2 se apaga; la base se guarda en /data (monta un disco aquí si quieres conservarla)
ENV H2_CONSOLE=false DB_PATH=/data/catalogo
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
