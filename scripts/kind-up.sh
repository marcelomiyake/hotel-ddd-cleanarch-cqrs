#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLUSTER_NAME="${KIND_CLUSTER_NAME:-hotel-system}"
KUBE_CONTEXT="kind-${CLUSTER_NAME}"

if ! kind get clusters | rg -q "^${CLUSTER_NAME}$"; then
  kind create cluster --name "${CLUSTER_NAME}"
fi

docker build -f services/catalog-service/Dockerfile -t hotel-catalog-service:local "${ROOT_DIR}"
docker build -f services/reservation-service/Dockerfile -t hotel-reservation-service:local "${ROOT_DIR}"
docker build -f web/Dockerfile -t hotel-reservation-web:local "${ROOT_DIR}"

kind load docker-image hotel-catalog-service:local hotel-reservation-service:local hotel-reservation-web:local \
  --name "${CLUSTER_NAME}"

kubectl --context "${KUBE_CONTEXT}" apply -f "${ROOT_DIR}/k8s/namespace.yaml"
kubectl --context "${KUBE_CONTEXT}" apply -f "${ROOT_DIR}/k8s/postgres.yaml"
kubectl --context "${KUBE_CONTEXT}" -n hotel-reservation rollout status statefulset/postgres --timeout=180s
kubectl --context "${KUBE_CONTEXT}" apply -f "${ROOT_DIR}/k8s/catalog-service.yaml"
kubectl --context "${KUBE_CONTEXT}" apply -f "${ROOT_DIR}/k8s/reservation-service.yaml"
kubectl --context "${KUBE_CONTEXT}" apply -f "${ROOT_DIR}/k8s/web.yaml"
kubectl --context "${KUBE_CONTEXT}" -n hotel-reservation rollout restart \
  deployment/catalog-service deployment/reservation-service deployment/web
kubectl --context "${KUBE_CONTEXT}" -n hotel-reservation rollout status deployment/catalog-service --timeout=240s
kubectl --context "${KUBE_CONTEXT}" -n hotel-reservation rollout status deployment/reservation-service --timeout=240s
kubectl --context "${KUBE_CONTEXT}" -n hotel-reservation rollout status deployment/web --timeout=120s

printf 'Ready: kubectl --context %s -n hotel-reservation get pods\n' "${KUBE_CONTEXT}"
printf 'Local access: kubectl --context %s -n hotel-reservation port-forward svc/web 4173:80\n' "${KUBE_CONTEXT}"
