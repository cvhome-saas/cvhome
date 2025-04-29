FROM node:20-alpine

ENV NODE_ENV production


RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY /public ./public

# Set the correct permission for prerender cache
RUN mkdir .next
RUN chown nextjs:nodejs .next

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
COPY  --chown=nextjs:nodejs /.next/standalone ./
COPY  --chown=nextjs:nodejs /.next/static ./.next/static

USER nextjs

EXPOSE 8110

ENV PORT 8110
# set hostname to localhost
ENV HOSTNAME "0.0.0.0"

CMD ["node", "server.js"]