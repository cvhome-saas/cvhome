FROM nginx:1.25

# Copy the build output to replace the default nginx contents.
ADD  /dist/welcome-ui/ /usr/share/nginx/html
COPY nginx.conf  /etc/nginx/conf.d/default.conf
EXPOSE 80
