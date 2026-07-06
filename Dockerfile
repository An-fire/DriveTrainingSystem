# 第一阶段：Maven 打包环境
FROM maven:3.9.6-amazoncorretto-17 AS builder

WORKDIR /app

ENV MAVEN_OPTS="-Xmx256m -XX:MaxMetaspaceSize=128m"

COPY pom.xml .

RUN mvn -B -DskipTests dependency:go-offline

COPY . .

RUN mvn -B clean package -DskipTests


# 第二阶段：Tomcat 运行环境
FROM tomcat:10.1-jdk17-corretto

# 关闭 Tomcat shutdown 端口，避免 Render 健康检查触发 Invalid shutdown command
RUN sed -i 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml

# 删除默认首页
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# 复制打好的 WAR 包
COPY --from=builder /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]