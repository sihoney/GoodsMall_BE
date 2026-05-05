#!/bin/bash
set -e

kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f prometheus.yaml
kubectl apply -f influxdb.yaml
kubectl apply -f grafana.yaml

echo ""
echo "배포 완료. Pod 상태 확인:"
kubectl get pods -n monitoring
