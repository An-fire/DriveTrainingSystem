# 第一阶段：Maven打包
FROM maven:3.9.6-amazoncorretto-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
# 修正参数：-Xmx256m 带减号
RUN mvn clean package -DskipTests -Xmx256m

# 第二阶段：Tomcat运行
FROM tomcat:10.1-jdk17-corretto
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=builder /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]