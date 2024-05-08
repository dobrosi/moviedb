FROM eclipse-temurin:17-jdk-alpine
WORKDIR /
RUN apk update && \
    apk upgrade && \
    apk add git
RUN git clone https://github.com/dobrosi/moviedb.git && cd moviedb && ./mvnw -DskipTests package && mv target/*.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]