# 后端 Dockerfile（带Maven自动构建）
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# 工作目录
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -DskipTests

# 把项目代码复制到镜像里
COPY . .

# 🚀 自动执行 Maven 打包（跳过测试）
RUN mvn clean package -DskipTests

# 后端 Dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 复制构建产物
COPY --from=build /app/target/blog-server-1.0.0.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
