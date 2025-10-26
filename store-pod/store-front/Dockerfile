# Use a specific and lightweight Node.js image for the runtime
FROM gcr.io/distroless/nodejs20
ENV NODE_ENV=production

COPY package.json package.json

# Copy all dist directories from the dist folder
COPY dist ./dist/

# Copy all dist directories from the libs folder
COPY libs ./libs/

# Copy all .next directories from the templates folder
COPY templates ./templates/

COPY node_modules ./node_modules/

CMD ["npm", "start"]