FROM nginx:1.25-alpine-slim
ADD  /dist/merchant-ui /usr/share/nginx/html
COPY nginx.conf  /etc/nginx/conf.d/default.conf
EXPOSE 8111
