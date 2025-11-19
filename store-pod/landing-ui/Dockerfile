# Use a specific and lightweight Node.js image for the runtime
FROM node:20-alpine
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1



RUN mkdir -p deployment

RUN echo "--- Assembling Standalone Artifacts ---"

# 3. Copy the 'default' template standalone output as the base
# This provides the minimized node_modules, package.json, and server.js for the default template
COPY  templates/default/.next/standalone/* deployment/

# 4. Copy the 'blank' template standalone output
# We merge this into the deployment folder.
COPY   templates/blank/.next/standalone/templates/blank deployment/templates/

# 5. Copy Static Assets (Standalone does not include these by default)
# For Default Template
RUN mkdir -p deployment/templates/default/.next/
COPY templates/default/.next/static deployment/templates/default/.next/static
COPY templates/default/public deployment/templates/default/public

# For Blank Template
RUN mkdir -p deployment/templates/blank/.next/
COPY templates/blank/.next/static deployment/templates/blank/.next/static
COPY templates/blank/public deployment/templates/blank/public

# 6. Copy the Express App
RUN mkdir -p deployment/app
COPY app/dist deployment/app/dist

# 7. Install Express Dependency
# Next.js standalone only includes dependencies it sees in the Next.js app.
# Since 'express' is used by your custom server (outside Next.js), we must add it manually.
RUN echo "--- Installing Runtime Dependencies for Express ---"
WORKDIR /deployment
RUN npm init -y
RUN npm install express
npm install express next@16.0.0 react@19.2.0 react-dom@19.2.0 next-intl@4.4.0

EXPOSE 8110

CMD ["node", "app/dist/server.js"]