# spring-ecs-employee（社員管理REST API）のコンテナイメージ定義
#
# 教材としての簡潔さを優先し、マルチステージビルドは使わず
# Gradleでビルド済みのbootJarをコピーする方式とする
#
# 事前準備（リポジトリルートで実行）:
#   ./gradlew :master:spring-ecs-employee:bootJar
#   cp projects/master/03_ecs/spring-ecs-employee/backend/build/libs/*.jar \
#      projects/master/03_ecs/spring-ecs-employee/backend/app.jar
#
# イメージのビルド（リポジトリルートで実行）:
#   docker build -t spring-ecs-employee projects/master/03_ecs/spring-ecs-employee/backend
FROM eclipse-temurin:25-jre

WORKDIR /app

# ECSタスク定義のヘルスチェック（curl）用にcurlをインストールする
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# ビルド済みのbootJarをコピーする
COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
