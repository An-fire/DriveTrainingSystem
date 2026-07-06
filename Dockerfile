# 第一阶段：Maven打包环境
FROM maven:3.9.6-amazoncorretto-17 AS builder
WORKDIR /app
# 先拷贝pom缓存依赖
COPY pom.xml .
RUN mvn dependency:go-offline
# 拷贝全部源码
COPY . .
# JVM参数放命令最开头，不会报错
RUN mvn -Xmx256m clean package -DskipTests

# 第二阶段：Tomcat运行环境
FROM tomcat:10.1-jdk17-corretto
# 删除默认首页
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# 复制打好的war包
COPY --from=builder /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]