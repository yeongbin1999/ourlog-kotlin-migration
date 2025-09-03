variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "리소스 이름 접두사"
  type        = string
  default     = "terra"
}

variable "my_ip" {
  description = "SSH 허용 IP"
  type        = string
  default     = "0.0.0.0/0"
}

variable "default_password" {
  description = "초기/공통 비밀번호"
  type        = string
  sensitive   = true
}

variable "mysql_user_list" {
  type = list(object({
    name       = string
    host       = string
    password   = string
    privileges = string
  }))
  default = [
    { name="local", host="127.0.0.1", password="local", privileges="ALL PRIVILEGES ON *.*" },
    { name="local", host="172.18.%.%", password="local", privileges="ALL PRIVILEGES ON *.*" },
    { name="ourlog", host="%", password="qwerty", privileges="ALL PRIVILEGES ON *.*" }
  ]
}

variable "app_db_name" {
  description = "애플리케이션 DB 이름"
  type        = string
  default     = "appdb"
}

variable "github_token" {
  description = "GitHub Container Registry 토큰"
  type        = string
  sensitive   = true
}

variable "github_user" {
  description = "GitHub 사용자/소유자 이름"
  type        = string
}