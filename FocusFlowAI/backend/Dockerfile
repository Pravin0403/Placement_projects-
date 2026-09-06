FROM node:20-alpine
WORKDIR /app
COPY package.json package-lock.json* ./
COPY prisma ./prisma
RUN npm ci || npm install
COPY tsconfig.json ./
COPY src ./src
RUN npx prisma generate && npm run build
EXPOSE 8080
CMD ["sh", "-c", "npx prisma migrate deploy && node dist/index.js"]
