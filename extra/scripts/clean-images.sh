#!/bin/bash
docker compose down &>/dev/null
docker rmi $(docker images | grep '<none>' |awk '{print $3}') --force &>/dev/null
docker rmi $(docker images | grep 'store' |awk '{print $3}') --force &>/dev/null
