variable "prefix" {
  description = "Prefix for all resources"
  default     = "dev"
}

variable "region" {
  description = "AWS region"
  default     = "ap-northeast-2"
}

variable "nickname" {
  description = "사용자 닉네임"
  default     = "jueunk617"
}

variable "password_1" {
  description = "Docker Redis 및 기타 기본 비밀번호"
  type        = string
  sensitive   = true
}

variable "db_username" {
  description = "RDS MySQL username"
  default     = "admin"
}

variable "db_password" {
  description = "RDS MySQL password"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "RDS MySQL DB name"
  default     = "ourlog_db"
}
