FROM openjdk:17
WORKDIR /
RUN microdnf install git
RUN git clone https://github.com/dobrosi/moviedb.git && cd moviedb && ./mvnw -DskipTests package && mv target/*.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]