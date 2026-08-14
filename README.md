# Retail App

Spring Boot REST API for a retail catalog covering four departments:
**Vegetables, Clothes, Electronics, Alcohol**.

## Stack
- Java 17, Spring Boot 3.3 (Web, Data JPA, Validation, Actuator)
- H2 in-memory DB for local/dev (swap the datasource for RDS/MySQL/Postgres in prod — edit `application.properties`)
- Maven build

## API

| Method | Path                                   | Description                     |
|--------|-----------------------------------------|----------------------------------|
| GET    | `/api/v1/products`                      | List all products                |
| GET    | `/api/v1/products?category=ELECTRONICS` | Filter by category               |
| GET    | `/api/v1/products/{id}`                 | Get one product                  |
| POST   | `/api/v1/products`                      | Create a product                 |
| PUT    | `/api/v1/products/{id}`                 | Update a product                 |
| DELETE | `/api/v1/products/{id}`                 | Delete a product                 |
| GET    | `/actuator/health`                      | Health check (used by k8s probes)|

Valid `category` values: `VEGETABLES`, `CLOTHES`, `ELECTRONICS`, `ALCOHOL`.

Sample create request:
```bash
curl -X POST localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Apple","category":"VEGETABLES","unit":"kg","price":120.00,"stockQuantity":50,"ageRestricted":false}'
```

## Run locally
```bash
mvn spring-boot:run
# or
mvn clean package && java -jar target/retail-app.jar
```

## Build & run with Docker
```bash
docker build -t retail-app:local .
docker run -p 8080:8080 retail-app:local
```

## End-to-end path to production (what the Jenkinsfile automates)

1. **Push to GitHub** — commit this project and push to your repo.
2. **Jenkins pipeline** (`Jenkinsfile` in the repo root):
   - `Checkout` — pulls source from GitHub (needs a `github-creds` credential in Jenkins).
   - `Build & Unit Test` — `mvn clean verify`, publishes JUnit results.
   - `Build Docker Image` — builds the image from the multi-stage `Dockerfile`.
   - `Push to ECR` — authenticates with `aws ecr get-login-password` and pushes the tagged image (needs an `aws-jenkins-creds` AWS credential and an `aws-account-id` secret text credential in Jenkins).
   - `Deploy to Dev K8s` — applies `k8s/deployment.yaml` + `k8s/service.yaml` to a dev/staging cluster context and waits for rollout.
   - `Approval: Promote to EKS` — manual gate; only proceeds once someone confirms the dev deployment looks healthy.
   - `Deploy to EKS` — points `kubectl` at the EKS cluster via `aws eks update-kubeconfig` and applies the same manifests.

### One-time setup you'll need before the pipeline works
- An ECR repository named `retail-app` in your target AWS region.
- An EKS cluster (name it in the `EKS_CLUSTER_NAME` env var in the Jenkinsfile, or edit to match yours).
- Jenkins plugins: Pipeline, Git, Docker Pipeline, AWS Credentials, Kubernetes CLI.
- Jenkins credentials:
  - `github-creds` — GitHub PAT or SSH key
  - `aws-jenkins-creds` — AWS access key/secret with ECR + EKS permissions
  - `aws-account-id` — secret text with your 12-digit AWS account ID
- `kubectl` and `aws` CLI installed on the Jenkins agent.
- Apply `k8s/namespace.yaml` once (`kubectl apply -f k8s/namespace.yaml`) before the first deploy.

## Repo layout
```
retail-app/
├── pom.xml
├── Dockerfile
├── Jenkinsfile
├── k8s/
│   ├── namespace.yaml
│   ├── deployment.yaml
│   └── service.yaml
└── src/
    ├── main/java/com/retail/app/...
    └── test/java/com/retail/app/...
```
