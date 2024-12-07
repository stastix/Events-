# Use a base Java image
FROM openjdk:17-jdk-slim

# Label the image
LABEL authors="Achraf"

# Set the working directory in the container
WORKDIR /app

# Build argument for the JAR file
ARG JAR_FILE=app.jar

# Copy the JAR file into the container
COPY ${JAR_FILE} app.jar

# Expose the port your application listens on
EXPOSE 8082

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]