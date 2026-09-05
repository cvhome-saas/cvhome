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
    append "127.0.0.1 uaa.gateway.com" "$file"
    append "127.0.0.1 console-ui.gateway.com" "$file"
    append "127.0.0.1 spg-507f1f77.gateway.com" "$file"
    append "127.0.0.1 catalog.gateway.com" "$file"
    append "127.0.0.1 merchant.gateway.com" "$file"
    append "127.0.0.1 content.gateway.com" "$file"
    append "127.0.0.1 checkout.gateway.com" "$file"
    append "127.0.0.1 cua.gateway.com" "$file"
    append "127.0.0.1 payment.gateway.com" "$file"
    append "127.0.0.1 inventory.gateway.com" "$file"
    append "127.0.0.1 landing-ui.gateway.com" "$file"
    append "127.0.0.1 org1-store1.spg-507f1f77.gateway.com" "$file"
    append "127.0.0.1 org1-store2.spg-507f1f77.gateway.com" "$file"
    append "127.0.0.1 org2-store1.spg-507f1f77.gateway.com" "$file"
    append "127.0.0.1 org2-store2.spg-507f1f77.gateway.com" "$file"
    append "127.0.0.1 spg-org3.gateway.com" "$file"
    append "127.0.0.1 org3-store1.spg-507f1f77.gateway.com" "$file"
    append "127.0.0.1 k6-local.spg-507f1f77.gateway.com" "$file"
}

 run-append
