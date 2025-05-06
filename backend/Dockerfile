# Build stage - Sử dụng image Maven phù hợp
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy chỉ pom.xml trước để tận dụng layer caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - Sử dụng image JRE thay vì JDK để giảm kích thước
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy file JAR từ stage build
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Thiết lập các biến môi trường cần thiết
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

# Mở port ứng dụng và socket.io (nếu cần)
EXPOSE 8080 9099

# Entrypoint tối ưu cho container
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]