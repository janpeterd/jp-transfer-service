#!/bin/bash

openssl genrsa -out ./src/main/resources/jwt_key.pem 2048
openssl rsa -in ./src/main/resources/jwt_key.pem -outform PEM -pubout -out ./src/main/resources/jwt_key.pem.pub
