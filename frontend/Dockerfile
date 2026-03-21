# ==============================
# Stage 1: dependencies
# ==============================
FROM node:22-alpine AS dependencies

WORKDIR /frontend

COPY package.json package-lock.json ./

RUN npm ci


# ==============================
# Stage 2: build
# ==============================
FROM dependencies AS build

WORKDIR /frontend

COPY . .

RUN npm run build


# ==============================
# Stage 3: runtime
# ==============================
FROM nginx:1.27-alpine AS runtime

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /frontend/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]