#!/bin/bash
if [ $(id -u) -ne 0 ]
  then echo Please run this script as root or using sudo!
  exit
fi

function append() {
    text=$1
    file=$2
    if grep  "$text" "$file" &> /dev/null; then
      echo "exists" &> /dev/null
    else
      echo -e  "\n$text" | sudo tee -a "$file" &> /dev/null
    fi

}


function run-append() {
    file="/etc/hosts"
    append "127.0.0.1 gateway.com" "$file"
    append "127.0.0.1 www.gateway.com" "$file"
    append "127.0.0.1 core-auth.gateway.com" "$file"
    append "127.0.0.1 store-ui.gateway.com" "$file"
    append "127.0.0.1 welcome-ui.gateway.com" "$file"
    append "127.0.0.1 store-pod-1.gateway.com" "$file"
    append "127.0.0.1 merchant-ui.gateway.com" "$file"
    append "127.0.0.1 pod-auth.gateway.com" "$file"
    append "127.0.0.1 org1-store1.store-pod-saas-gateway-1.gateway.com" "$file"
    append "127.0.0.1 org1-store2.store-pod-saas-gateway-1.gateway.com" "$file"
    append "127.0.0.1 org2-store1.store-pod-saas-gateway-1.gateway.com" "$file"
    append "127.0.0.1 org2-store2.store-pod-saas-gateway-1.gateway.com" "$file"
}

 run-append