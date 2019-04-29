#!/usr/bin/env bash

kubectl delete rc/search-demo svc/search-demo --kubeconfig /Users/azim/Learning/aws/kube/config

kubectl apply -f deployment/search-demo-controller.json --kubeconfig /Users/azim/Learning/aws/kube/config

kubectl apply -f deployment/search-demo-service.json --kubeconfig /Users/azim/Learning/aws/kube/config

kubectl get services -o wide --kubeconfig /Users/azim/Learning/aws/kube/config