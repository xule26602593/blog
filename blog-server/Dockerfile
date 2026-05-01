# 后端 Dockerfile（带Maven自动构建）
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# 配置阿里云镜像源
RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd"> \
          <mirrors> \
            <mirror> \
              <id>aliyunmaven</id> \
              <mirrorOf>*</mirrorOf> \
              <name>阿里云公共仓库</name> \
              <url>https://maven.aliyun.com/repository/public</url> \
            </mirror> \
          </mirrors> \
        </settings>' > /root/.m2/settings.xml

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
