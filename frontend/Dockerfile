# Multi-stage build for React/Vite frontend
# Stage 1: build with Node
FROM node:22-alpine AS build
WORKDIR /app

# Copy manifest first for layer caching.
# No package-lock.json committed; npm install resolves from package.json.
COPY package.json ./
RUN npm install --silent

COPY . .

# VITE_API_BASE_URL is baked in at build time.
# Defaults to /api so nginx can proxy it; override via --build-arg for other environments.
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

# Stage 2: serve with nginx
FROM nginx:alpine AS runtime
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
