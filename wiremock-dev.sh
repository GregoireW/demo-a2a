#!/bin/bash

# Start Wiremock server in a Docker container, mapping port 9999 and mounting the local 'mock' directory.

docker run --rm -d \
  -p 9999:9999 \
  -v "$(pwd)/mock:/home/wiremock" \
  --name wiremock \
  wiremock/wiremock:latest \
  --port 9999 \
  --root-dir /home/wiremock

echo "Wiremock listen port 9999."
