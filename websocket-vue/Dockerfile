FROM node:18-alpine

WORKDIR /frontend

EXPOSE 7000

COPY package.json package-lock.json ./

RUN npm ci

COPY . ./

CMD ["npm", "run", "dev", "--", "--host"]