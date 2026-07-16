#!/usr/bin/env bash
# ============================================
# PawMatch 本地开发启动脚本
# ============================================
# 用途：自动生成 JWT_SECRET 并启动 Spring Boot
# 用法：
#   1) 在 backend/ 下准备 .env（参考 .env.example）
#   2) source .env   或   export $(grep -v '^#' .env | xargs)
#   3) bash run.sh
# 备注：生产环境请用真实的密钥管理(Vault / K8s Secret / .env.production)
# ============================================

set -e

# 自动加载 backend/.env（如果存在）
if [ -f "$(dirname "$0")/.env" ]; then
    set -a
    # shellcheck disable=SC1091
    source "$(dirname "$0")/.env"
    set +a
    echo "[run.sh] 已加载 backend/.env"
fi

# 若用户已在外部设置 JWT_SECRET,沿用;否则生成新的
if [ -z "$JWT_SECRET" ]; then
    export JWT_SECRET=$(openssl rand -hex 32)
    echo "[run.sh] 已生成临时 JWT_SECRET(本次进程有效,长度=${#JWT_SECRET})"
fi

# OpenAI / DashScope key 必须从环境变量注入;若未提供则直接报错（避免静默使用占位符）
if [ -z "$OPENAI_API_KEY" ]; then
    echo "[run.sh] 缺少 OPENAI_API_KEY，请在 backend/.env 中配置" >&2
    exit 1
fi
if [ -z "$DASHSCOPE_API_KEY" ]; then
    echo "[run.sh] 缺少 DASHSCOPE_API_KEY，请在 backend/.env 中配置" >&2
    exit 1
fi

echo "[run.sh] 启动 Spring Boot ..."
mvn spring-boot:run
