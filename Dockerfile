FROM amazoncorretto:21-alpine-jdk

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} greedy-bot.jar

ENV TZ=Asia/Seoul

ENTRYPOINT ["java", "-jar", "/greedy-bot.jar"]
