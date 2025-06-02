FROM eclipse-temurin:17
EXPOSE 8080
ADD target/*.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=prod","/app.jar"]
