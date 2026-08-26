FROM node:20 AS builder
WORKDIR /app
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
COPY package.json ./
RUN corepack enable && pnpm install --frozen-lockfile=false
COPY . .
RUN pnpm build

FROM nginx:1.27
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
