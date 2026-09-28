FROM node:24-alpine AS build
WORKDIR /workspace
COPY package.json package-lock.json .npmrc ./
COPY web/package.json ./web/package.json
RUN npm ci
COPY web ./web
RUN npm --workspace web run build

FROM nginx:1.29-alpine
COPY web/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/web/dist /usr/share/nginx/html
EXPOSE 80
