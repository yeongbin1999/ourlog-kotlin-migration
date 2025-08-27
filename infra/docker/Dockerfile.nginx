FROM nginx:stable

COPY backend.conf /etc/nginx/conf.d/backend.conf

# SSL 인증서 경로 (EC2에서는 호스트 경로 마운트)
# docker run 시 -v /etc/ssl/certs:/etc/ssl/certs -v /etc/ssl/private:/etc/ssl/private
EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
