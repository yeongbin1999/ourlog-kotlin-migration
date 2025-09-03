locals {
  ec2_user_data = <<-EOF
#!/bin/bash
# 가상 메모리
dd if=/dev/zero of=/swapfile bs=128M count=32
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo "/swapfile swap swap defaults 0 0" >> /etc/fstab

# Docker 설치
yum install docker -y
systemctl enable docker
systemctl start docker
docker network create common

# nginx-proxy-manager 컨테이너
docker run -d \
  --name npm \
  --restart unless-stopped \
  --network common \
  -p 80:80 -p 443:443 -p 81:81 \
  -e TZ=Asia/Seoul \
  -e INITIAL_ADMIN_EMAIL=admin@npm.com \
  -e INITIAL_ADMIN_PASSWORD=${var.default_password} \
  -v /dockerProjects/npm/volumes/data:/data \
  -v /dockerProjects/npm/volumes/etc/letsencrypt:/etc/letsencrypt \
  jc21/nginx-proxy-manager:latest

# Redis 컨테이너
docker run -d \
  --name redis \
  --restart unless-stopped \
  --network common \
  -p 6379:6379 \
  -e TZ=Asia/Seoul \
  -v /dockerProjects/redis/volumes/data:/data \
  redis --requirepass ${var.default_password}

# MySQL 컨테이너
docker run -d \
  --name mysql \
  --restart unless-stopped \
  --network common \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=${var.default_password} \
  -v /dockerProjects/mysql/volumes/var/lib/mysql:/var/lib/mysql \
  -v /dockerProjects/mysql/volumes/etc/mysql/conf.d:/etc/mysql/conf.d \
  mysql:latest

# MySQL 준비 대기
until docker exec mysql mysql -uroot -p${var.default_password} -e "SELECT 1" &> /dev/null; do
  sleep 5
done

# MySQL 사용자 생성
%{ for u in var.mysql_user_list ~}
docker exec mysql mysql -uroot -p${var.default_password} -e "
CREATE USER '${u.name}'@'${u.host}' IDENTIFIED WITH caching_sha2_password BY '${u.password}';
GRANT ${u.privileges} TO '${u.name}'@'${u.host}';
FLUSH PRIVILEGES;"
%{ endfor ~}

# 애플리케이션 DB 생성
docker exec mysql mysql -uroot -p${var.default_password} -e "CREATE DATABASE IF NOT EXISTS \`${var.app_db_name}\`;"

# GitHub Container Registry 로그인
echo "${var.github_token}" | docker login ghcr.io -u ${var.github_user} --password-stdin
EOF
}