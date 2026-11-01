FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm config set fetch-retry-maxtimeout 60000 
RUN npm config set fetch-retries 5 
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/Fitness/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=3s CMD wget -qO- http://localhost/ >/dev/null || exit 1