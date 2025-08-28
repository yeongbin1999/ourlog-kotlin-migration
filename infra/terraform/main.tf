terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }

  backend "s3" {
    bucket         = "ourlog-terraform-state"
    key            = "ourlog/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.region
}

# SSH 키 페어 생성
resource "aws_key_pair" "key_pair_1" {
  key_name   = "${var.prefix}-${var.nickname}-key-pair"
  public_key = file(pathexpand("~/.ssh/id_rsa_ourlog.pub"))

  tags = {
    Name = "${var.prefix}-${var.nickname}-key-pair"
  }
}

resource "aws_vpc" "vpc_1" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.prefix}-vpc-1"
  }
}

resource "aws_subnet" "subnet_1" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-1"
  }
}

resource "aws_subnet" "subnet_2" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "${var.region}b"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-2"
  }
}

resource "aws_subnet" "subnet_3" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.3.0/24"
  availability_zone       = "${var.region}c"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-3"
  }
}

resource "aws_subnet" "subnet_4" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.4.0/24"
  availability_zone       = "${var.region}d"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-4"
  }
}

resource "aws_internet_gateway" "igw_1" {
  vpc_id = aws_vpc.vpc_1.id

  tags = {
    Name = "${var.prefix}-igw-1"
  }
}

resource "aws_route_table" "rt_1" {
  vpc_id = aws_vpc.vpc_1.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw_1.id
  }

  tags = {
    Name = "${var.prefix}-rt-1"
  }
}

resource "aws_route_table_association" "association_1" {
  subnet_id      = aws_subnet.subnet_1.id
  route_table_id = aws_route_table.rt_1.id
}

resource "aws_route_table_association" "association_2" {
  subnet_id      = aws_subnet.subnet_2.id
  route_table_id = aws_route_table.rt_1.id
}

resource "aws_route_table_association" "association_3" {
  subnet_id      = aws_subnet.subnet_3.id
  route_table_id = aws_route_table.rt_1.id
}

resource "aws_route_table_association" "association_4" {
  subnet_id      = aws_subnet.subnet_4.id
  route_table_id = aws_route_table.rt_1.id
}

# EC2용 보안 그룹
resource "aws_security_group" "ec2_sg" {
  name   = "${var.prefix}-ec2-sg"
  vpc_id = aws_vpc.vpc_1.id

  # HTTP
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # 블루/그린 앱 포트 (8080~8090): VPC 내부 통신만 허용
  ingress {
    from_port   = 8080
    to_port     = 8090
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  ingress {
    from_port   = 81
    to_port     = 81
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Redis는 컨테이너 네트워크만 사용 → SG 열 필요 없음

  # 모든 아웃바운드 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.prefix}-ec2-sg" }
}

# RDS용 보안 그룹
resource "aws_security_group" "rds_sg" {
  name   = "${var.prefix}-rds-sg"
  vpc_id = aws_vpc.vpc_1.id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.prefix}-rds-sg" }
}

resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "${var.prefix}-db-subnet-group"
  subnet_ids = [aws_subnet.subnet_1.id, aws_subnet.subnet_2.id, aws_subnet.subnet_3.id, aws_subnet.subnet_4.id]
  tags       = { Name = "${var.prefix}-db-subnet-group" }
}

resource "aws_db_instance" "rds_1" {
  identifier              = "${var.prefix}-rds-1"
  allocated_storage       = 20
  engine                  = "mysql"
  engine_version          = "8.0"
  instance_class          = "db.t3.micro"
  db_name                 = var.db_name
  username                = var.db_username
  password                = var.db_password
  skip_final_snapshot     = true
  publicly_accessible     = true
  db_subnet_group_name    = aws_db_subnet_group.db_subnet_group.name
  vpc_security_group_ids  = [aws_security_group.rds_sg.id]
  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "sun:04:00-sun:05:00"
  tags                    = { Name = "${var.prefix}-rds-1" }
}

resource "aws_iam_role" "ec2_role_1" {
  name = "${var.prefix}-ec2-role-1"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect    = "Allow",
      Principal = { Service = "ec2.amazonaws.com" },
      Action    = "sts:AssumeRole"
    }]
  })
}

# SSM 접속용 필수 권한
resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "instance_profile_1" {
  name = "${var.prefix}-instance-profile-1"
  role = aws_iam_role.ec2_role_1.name
}

locals {
  ec2_user_data_base = <<-EOF
#!/bin/bash
set -e
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
echo "User data started: $(date) - force update"

# 기본 유틸 + 방화벽
dnf install -y curl ca-certificates firewalld || true
systemctl enable --now firewalld

# 방화벽 설정
firewall-cmd --permanent --add-port=81/tcp
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --reload

# Docker Engine 설치
curl -fsSL https://get.docker.com -o /root/get-docker.sh
sh /root/get-docker.sh
systemctl enable --now docker
usermod -a -G docker ec2-user

# Compose v2 플러그인 설치
dnf install -y docker-compose-plugin || true

# Docker 데몬 준비 대기
for i in $(seq 1 12); do
  if systemctl is-active --quiet docker; then
    echo "Docker is active"; break
  fi
  echo "Waiting for docker... ($i/12)"
  systemctl start docker || true
  sleep 5
done

# 스왑 (4GB)
dd if=/dev/zero of=/swapfile bs=128M count=32
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile swap swap defaults 0 0' >> /etc/fstab

# 디렉토리
mkdir -p /dockerProjects/npm_1/volumes/data
mkdir -p /dockerProjects/npm_1/volumes/etc/letsencrypt

# Docker 네트워크
docker network create common || true

# Nginx Proxy Manager
docker rm -f npm_1 2>/dev/null || true
docker run -d \
  --name npm_1 \
  --restart unless-stopped \
  --network common \
  -p 80:80 \
  -p 443:443 \
  -p 81:81 \
  -e TZ=Asia/Seoul \
  -v /dockerProjects/npm_1/volumes/data:/data \
  -v /dockerProjects/npm_1/volumes/etc/letsencrypt:/etc/letsencrypt \
  jc21/nginx-proxy-manager:latest

# Redis: 외부 노출 제거
docker rm -f redis_1 2>/dev/null || true
docker run -d \
  --name redis_1 \
  --restart unless-stopped \
  --network common \
  -e TZ=Asia/Seoul \
  redis --requirepass ${var.password_1}

# GHCR 로그인 스크립트
cat > /home/ec2-user/ghcr-login.sh << GHCR_EOF
#!/bin/bash
# Usage: ./ghcr-login.sh <github-token>
if [ -z "\$1" ]; then
  echo "Usage: \$0 <github-token>"
  exit 1
fi
echo \$1 | docker login ghcr.io -u ${var.nickname} --password-stdin
GHCR_EOF
chmod +x /home/ec2-user/ghcr-login.sh
chown ec2-user:ec2-user /home/ec2-user/ghcr-login.sh

echo "User data finished: $(date)"
EOF
}

data "aws_ami" "latest_amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
}

resource "aws_instance" "ec2_1" {
  ami                         = data.aws_ami.latest_amazon_linux.id
  instance_type               = "t3.micro"
  subnet_id                   = aws_subnet.subnet_4.id
  vpc_security_group_ids      = [aws_security_group.ec2_sg.id]
  associate_public_ip_address = true
  iam_instance_profile        = aws_iam_instance_profile.instance_profile_1.name
  key_name                    = aws_key_pair.key_pair_1.key_name

  root_block_device {
    volume_type = "gp3"
    volume_size = 20 # 12GB → 20GB로 증가
    encrypted   = true
  }

  tags = {
    Name = "${var.prefix}-ec2-1"
  }

  user_data_base64 = base64encode(local.ec2_user_data_base)

  # 인스턴스가 준비될 때까지 대기
  depends_on = [
    aws_internet_gateway.igw_1,
    aws_route_table_association.association_4
  ]
}