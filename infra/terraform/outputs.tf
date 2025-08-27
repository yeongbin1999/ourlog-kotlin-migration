output "ec2_public_ip" {
  description = "EC2 인스턴스의 공개 IP"
  value       = aws_instance.ec2_1.public_ip
}

output "ec2_public_dns" {
  description = "EC2 인스턴스의 공개 DNS"
  value       = aws_instance.ec2_1.public_dns
}

output "rds_endpoint" {
  description = "RDS MySQL 엔드포인트"
  value       = aws_db_instance.rds_1.endpoint
  sensitive   = true
}

output "rds_port" {
  description = "RDS MySQL 포트"
  value       = aws_db_instance.rds_1.port
}

output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.vpc_1.id
}

output "nginx_proxy_manager_url" {
  description = "Nginx Proxy Manager 관리 URL"
  value       = "http://${aws_instance.ec2_1.public_ip}:81"
}

output "ssh_connection_command" {
  description = "SSH 연결 명령어"
  value       = "ssh -i ~/.ssh/id_rsa ec2-user@${aws_instance.ec2_1.public_ip}"
}

# 환경 변수로 사용할 정보들
output "environment_variables" {
  description = "애플리케이션에서 사용할 환경 변수들"
  value = {
    DB_HOST     = aws_db_instance.rds_1.endpoint
    DB_PORT     = aws_db_instance.rds_1.port
    DB_NAME     = aws_db_instance.rds_1.db_name
    DB_USERNAME = aws_db_instance.rds_1.username
    REDIS_HOST  = "redis_1" # Docker 네트워크에서의 컨테이너 이름
    REDIS_PORT  = "6379"
  }
  sensitive = true
}