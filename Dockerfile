# Java 21 포함된 공식 이미지 사용
FROM eclipse-temurin:21-jdk

WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# Gradle wrapper 실행 권한 추가
RUN chmod +x ./gradlew

# 테스트 제외하고 bootJar 생성
RUN ./gradlew bootJar -x test

# Render가 열어주는 내부 포트 (기본값)
EXPOSE 8080

# 빌드된 JAR 실행
CMD ["java", "-jar", "build/libs/*.jar"]
