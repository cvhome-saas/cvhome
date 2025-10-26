#!/bin/bash
npm i ; npm run build;npm prune --omit=dev;
docker build -t store-front-app .
docker run -p 8110:8110 store-front-app
docker images | grep store-front-app