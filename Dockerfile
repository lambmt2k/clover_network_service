FROM adoptopenjdk:11-jre-hotspot
VOLUME /tmp
COPY target/*.jar clover_network-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/clover_network-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080
