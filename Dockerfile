FROM eclipse-temurin:11-jdk
WORKDIR /app
COPY ./src/main/resources/serviceAccountKey.json /app/src/main/resources/serviceAccountKey.json
COPY target/clover_network-0.0.1-SNAPSHOT.jar clover_network-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-jar", "clover_network-0.0.1-SNAPSHOT.jar"]