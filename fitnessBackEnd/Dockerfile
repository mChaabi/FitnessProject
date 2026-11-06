# ---- compilar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Solo el pom: esta capa se cachea mientras el pom no cambie
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    for i in 1 2 3 4 5; do \
      mvn -B -q dependency:go-offline -Dmaven.wagon.http.retryHandler.count=5 && break \
      || (echo "Reintento $i..." && sleep 10); \
    done

# 2. El código: cambiarlo no vuelve a descargar nada
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests -o

# ---- ejecutar ----
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd -r app && useradd -r -g app app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Duser.timezone=Africa/Casablanca"
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fs http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]