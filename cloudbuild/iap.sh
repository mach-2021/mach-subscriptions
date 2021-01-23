#!/bin/bash

gcloud beta compute start-iap-tunnel sonarqube 9000 --local-host-port=localhost:9000 --zone=us-central1-a
