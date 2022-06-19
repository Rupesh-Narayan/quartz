FROM maven:3.5.2-jdk-8 AS build

ADD settings.xml /root/.m2/settings.xml
COPY src /app/src
COPY pom.xml /app
COPY datadog/dd-java-agent.jar /app
COPY src/main/resources/run.sh /app
RUN ls /app/
RUN mvn -f /app/pom.xml clean install

FROM openjdk:8-jdk-alpine
RUN apk add --no-cache bash
WORKDIR /app
COPY --from=build /app/target/quartz-*.jar /app/quartz.jar
COPY --from=build /app/target/classes/application.yml /app/quartz-app.yml
COPY --from=build /app/run.sh /app
COPY --from=build /app/dd-java-agent.jar /app


RUN ls /app/
RUN chmod +x /app/run.sh
#ENV TZ=Asia/Calcutta

ENTRYPOINT ["/app/run.sh"]