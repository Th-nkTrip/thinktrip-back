FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/*.jar

# 작업 디렉토리 지정 (선택사항)
WORKDIR /app

# JAR 복사
COPY ${JAR_FILE} app.jar

# uploads 디렉토리 미리 생성 (컨테이너 내에 경로 필요하므로)
RUN mkdir -p /app/uploads

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
