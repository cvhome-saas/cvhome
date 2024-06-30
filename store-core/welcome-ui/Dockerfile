FROM node:20.15.0-alpine
ADD  /dist/welcome-ui/ /usr/app
WORKDIR /usr/app

ENV COM_ASREVO_CVHOME_APP_SIGNIN_DOMAIN=https://store-ui.asrevo.com
CMD node server/server.mjs
EXPOSE 4300
