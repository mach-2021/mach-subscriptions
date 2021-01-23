#!/bin/bash

gcloud auth list

# Start the primary process and put it in the background
echo "Setting up IAP"
./iap.sh &

echo "Sleeping for 10s..."
# wait 10 seconds for IAP
sleep 10

echo "Running Gradle"

# Start the build process
gradle sonarqube -Dsonar.projectKey=b2b-bff -Dsonar.host.url=http://localhost:9000/sonar/ -Dsonar.login=sonar_pass
