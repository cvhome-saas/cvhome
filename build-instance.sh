curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
export NVM_DIR="$([ -z "${XDG_CONFIG_HOME-}" ] && printf %s "${HOME}/.nvm" || printf %s "${XDG_CONFIG_HOME}/nvm")"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" # This loads nvm

nvm install 20

curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.3-amzn
sdk install gradle 8.6

sudo yum install -y docker
sudo service docker start
sudo chmod 666 /var/run/docker.sock

sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose





wget "https://cvhome-storage.s3.eu-central-1.amazonaws.com/cvhome-master.zip?response-content-disposition=inline&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEIL%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDGV1LWNlbnRyYWwtMSJHMEUCIQCKgdDJdf1sjUD3C%2FYZUZswgCNu%2BBL7LZJ3QKsvfvt3KwIgIm%2BITz0qfeBRyqH%2BUV1885bTfLPEH3SuzkWuq5q6KX4qigMI7P%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FARADGgw4MjQ1OTE0MzgxMjEiDCdQIBxAYtr8Tr7vGSreAqm8gCyCRPBICzIMpmEs1WGB3l9NKVKwRNkKhsyudCcUvrh9Re59qt5zqGWUM3IaI01RZ4O0eLmS0WZXYjG9%2FNfXW80qAUTeoR%2FpyY95BrACEl2VeboULZqpCn4Yb6eFgvbZVmO58v1Ls702mi3wEXnWiJ4icJdIUWStODzh3IVGnZMfRvxN6hbxO89Ua5kKb%2B3NCi3Z1RvNv8rMbIwks6cwFPCF5q6os1y4IoHwBRZj5SCnwgKN0pRQYm8G71LDRammYPYOyP80n1cEjhAI1eZUhZFoTwJwQ%2FBGWTMAQpLEruzP4oIAMToFplBRuAuZKEK71wHcUzdvDw3zUSFuwYnhQsF7CUB0Esagsjny6d40otfV7NIfYgmtCynIkuFheIihe6zwZvyCHgecnE2stOxrC9IYxYhxxe0t9fz4Un7lH5X%2Fh5mdwxZS5Bq1ndq0zg22w00oTmV3cv%2FAAAZNMPLJoLIGOrMC1VL0fKvAdgGONyUSAS9h2l2K%2B3rh9iy2S0aX1jEWNQAN%2Fwi91f70JNX7%2FODKrRVmSQWI98k%2F8fJ7dDevqW1kUXqR08L0iDAIdGAEqJyeMOaW%2FUgXmy7WchqyuqsYvlMOwbfuiKhTqfdPTkASkecNLkzArbaFp2b6nZTUg1gH3H7l7KFNcp8OTlpOn8g3INYFc9ZHiOGrNelLZEIxylLSLkYR%2BbDHPFgI4%2BpOfIBeuAeWyAIvsQd0bbe7YN%2FNYI1WFi3YB7l8a4Ea3WQ%2BjYpSd3oCIi%2B%2Blz32EZccSmq%2BHBMLtlWgwIxiCln%2Bc8Ez1LjcIFmpGiTSvqjf%2Bc0vtaQhjicnxMnfYcI9AGvE%2Bew6tU6%2FvlQ8tUynHJqQ5LfZlQ55Cw7S3p2HyrIUf9VYd1%2BYnlbO1Q%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240518T102628Z&X-Amz-SignedHeaders=host&X-Amz-Expires=36000&X-Amz-Credential=ASIA377L22EU4GXZDHU2%2F20240518%2Feu-central-1%2Fs3%2Faws4_request&X-Amz-Signature=d99e294baf32193d8c784ba9428576b1e5e5eebcbb1f216215208b7f7abe2cf0"
mv cvhome-master.zip cvhome.zip
unzip cvhome.zip
cd cvhome-master/

chmod +x ./gradlew
bash build.sh

bash configure-domain.sh
docker-compose up
