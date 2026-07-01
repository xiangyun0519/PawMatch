# PawMatch 部署文档（Cloud Native）

> 本目录包含 PawMatch 的 Kubernetes 部署资源：Helm Chart、KEDA 弹性扩缩、Istio 流量切分、ArgoCD GitOps。

## 目录结构

```
deploy/
├── helm/                            # Helm Chart（一键部署全套）
│   ├── Chart.yaml
│   ├── values.yaml                  # 参数化配置
│   └── templates/
│       ├── deployment.yaml          # 主 Deployment（带健康检查）
│       ├── service.yaml
│       ├── secret.yaml               # 密码/API Key 占位
│       ├── serviceaccount.yaml
│       ├── hpa.yaml                  # 标准 HPA（CPU）
│       ├── keda-scaledobject.yaml    # KEDA ScaledObject（按队列）
│       ├── istio-ab-test.yaml        # A/B 测试（v1/v2 切分）
│       ├── ingress.yaml
│       └── _helpers.tpl
└── k8s/                             # 独立部署模板（直接 apply）
    ├── keda-scaledobject.yaml
    ├── istio-ab-test.yaml
    └── argocd-application.yaml       # GitOps Application
```

## 快速部署（需 K8s 集群）

### 前置条件
- K8s 1.24+
- Helm 3.x
- 已安装 NGINX Ingress Controller（或调整 `ingress.className`）
- PG/Redis/RabbitMQ 已在同 namespace 部署（参考 `backend/docker-compose.yml` 的服务清单）

### 一键安装

```bash
# 1. 创建 namespace
kubectl create namespace pawmatch

# 2. 创建 Secret 真实值（覆盖默认占位）
kubectl create secret generic pawmatch-secret \
  -n pawmatch \
  --from-literal=db-password='YOUR_DB_PASS' \
  --from-literal=redis-password='YOUR_REDIS_PASS' \
  --from-literal=rabbitmq-password='YOUR_MQ_PASS' \
  --from-literal=openai-api-key='sk-...' \
  --from-literal=dashscope-api-key='sk-...'

# 3. 安装 Chart
helm install pawmatch ./helm -n pawmatch \
  --set image.repository=harbor.your.com/pawmatch/backend \
  --set image.tag=1.0.0
```

### 启用 KEDA（按 RabbitMQ 队列长度弹性扩缩）

```bash
# 1. 安装 KEDA
helm repo add kedacore https://kedacore.github.io/charts
helm install keda kedacore/keda --namespace keda --create-namespace

# 2. 启用 KEDA 触发器
helm upgrade pawmatch ./helm -n pawmatch \
  --set keda.enabled=true \
  --set autoscaling.minReplicas=2 \
  --set autoscaling.maxReplicas=20
```

队列长度阈值在 `values.yaml` 的 `keda.triggers[0].metadata.value` 中配置（默认 100）。

### 启用 Istio A/B 测试

```bash
# 1. 给 v2 Pod 打 label（修改 helm chart 或 kubectl label）
#    kubectl label pod ... version=v2 embedding-model=bge-m3

# 2. 应用 A/B 规则
kubectl apply -f k8s/istio-ab-test.yaml

# 3. 测试：默认 80/20 切分
curl http://pawmatch/...

# 4. 测试：强制走 v2
curl -H "x-embedding-experiment: v2" http://pawmatch/...
```

### 启用 ArgoCD GitOps

```bash
# 1. 安装 ArgoCD
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# 2. 修改 argocd-application.yaml 里的 repoURL 为你的 Git 地址
# 3. 应用
kubectl apply -f k8s/argocd-application.yaml

# 4. 之后：改 helm/values.yaml → git push → ArgoCD 自动同步
```

## 关键架构

```
┌─────────────────────────────────────────────────────┐
│ K8s Cluster                                          │
│  ┌────────────────────────────────────────────────┐ │
│  │ pawmatch Deployment (2 replicas, auto-scaled)   │ │
│  │  └─ JWT Filter → Matching → Embedding → DB     │ │
│  └────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────┐ │
│  │ Postgres + pgvector (StatefulSet)              │ │
│  │ Redis (Deployment)                             │ │
│  │ RabbitMQ (StatefulSet)                         │ │
│  └────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────┐ │
│  │ KEDA → watches matching_queue → scales pods    │ │
│  └────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────┐ │
│  │ Istio VirtualService → 80% v1 / 20% v2          │ │
│  └────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

## 验证清单

部署后逐项验证：

- [ ] `kubectl get pods -n pawmatch` 全部 Running
- [ ] `kubectl logs -n pawmatch <pod>` 看到 "Started PetAdoptApplication"
- [ ] Flyway 迁移成功（启动日志含 "Successfully applied 2 migrations"）
- [ ] `curl http://<svc>:8080/actuator/health` 返回 200
- [ ] Swagger UI 可访问：`http://<ingress>/swagger-ui.html`
- [ ] 注册一个测试用户 + 登录拿 token
- [ ] `POST /api/match/recommend` 返回 taskId
- [ ] 轮询 `GET /api/match/history/<taskId>` 状态为 SUCCESS
- [ ] KEDA：发送 1000 个匹配请求 → pod 副本数自动扩容
- [ ] Istio：50% 流量 header 切分生效
- [ ] Grafana：embedding-model=v1 vs v2 的匹配分数对比看板