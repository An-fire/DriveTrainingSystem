# 分多阶段构建：第一阶段maven打包，第二阶段tomcat运行
FROM maven:3.9-jdk-17 AS builder
WORKDIR /app
# 复制pom先下载依赖缓存
COPY pom.xml .
RUN mvn dependency:go-offline
# 复制全部源码
COPY . .
# 打包生成ROOT.war，跳过测试
RUN mvn clean package -DskipTests

# 第二阶段：Tomcat运行环境
FROM tomcat:10.1-jdk17-corretto
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# 从构建阶段复制打好的war
COPY --from=builder /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]