FROM openjdk
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} search-demo.jar
ENTRYPOINT ["java","-jar","/search-demo.jar"]