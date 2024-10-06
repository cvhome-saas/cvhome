#!/bin/bash
docker compose down
docker rmi $(docker images | grep 'store' |awk '{print $3}') --force
docker rmi $(docker images | grep 'saas' |awk '{print $3}') --force
bash build.sh
docker compose up