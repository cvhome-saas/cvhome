FROM public.ecr.aws/b2i4h4k9/node:20.15.0-alpine
ADD /dist/console-ui/ /usr/app
WORKDIR /usr/app

CMD node server/server.mjs
EXPOSE 8011
