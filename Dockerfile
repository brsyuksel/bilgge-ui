FROM node:14-alpine AS builder

RUN apk add --update openjdk8 curl bash && \
    cd /root && \
    curl -L https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein --output lein && \
    mv lein /usr/local/bin && \
    chmod a+x /usr/local/bin/lein

RUN mkdir /source
WORKDIR /source

COPY . .

ARG api_url=https://api.bilgge.com
ENV API_BASE_URL=$api_url
RUN lein release

FROM nginx:1.19-alpine
COPY --from=builder /source/resources/public /public
COPY .docker/nginx-default.conf /etc/nginx/conf.d/default.conf
