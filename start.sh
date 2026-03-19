#!/bin/bash

mkdir -p /home/runner/workspace/data

echo "Building application..."
cd /home/runner/workspace/backend-java
mvn package -DskipTests -q

echo "Starting Spring Boot application..."
java -jar /home/runner/workspace/backend-java/target/payroll-2.0.0.jar
