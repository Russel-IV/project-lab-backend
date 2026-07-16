# AWS EC2 & ECR Backend Deployment Documentation

> **Superseded** (docs/adr/0014, migration Phase 7): production hosting moved from
> AWS EC2/RDS to Oracle Cloud's Always Free Ampere A1 tier — the single-JVM,
> 1GB-RAM EC2 box this document describes cannot run the microservices topology
> (docs/adr/0001 and on) at all, regardless of code changes. See
> [`docs/oracle-cloud-deployment.md`](oracle-cloud-deployment.md) for the current
> setup. Kept here for historical reference, not as active documentation — do not
> follow these steps for a new deployment.

This document describes the infrastructure and CI/CD workflow setup for deploying the Kotlin Spring Boot backend application to AWS using Amazon ECR (Elastic Container Registry) and Amazon EC2 (Elastic Compute Cloud).

---

## Architecture Overview

```mermaid
graph TD
    Developer[Developer] -- Git Push --> GitHub[GitHub Actions CI/CD]
    GitHub -- 1. Run Tests & Lint --> GitHub
    GitHub -- 2. Build & Push Image --> ECR[Amazon ECR Repository]
    GitHub -- 3. Trigger SSH Deploy --> EC2[Amazon EC2 Instance]
    EC2 -- 4. Authenticate & Pull Image --> ECR
    EC2 -- 5. Run App Container --> Container[Docker: spring-backend]
```

To optimize cost and avoid running out of memory (OOM) on resource-constrained instances, the build processes (compiling Kotlin, downloading dependencies, building Docker image layers) are completely offloaded to GitHub Actions. The EC2 instance simply pulls the pre-built image from ECR and runs it.

---

## 1. Amazon ECR (Elastic Container Registry)

ECR is used to securely host the Docker images. The repository details are configured as follows:

- **Repository Name:** `project-lab-backend`
- **AWS Region:** `us-east-2`
- **Repository URI:** `378780514580.dkr.ecr.us-east-2.amazonaws.com/project-lab-backend`
- **Vulnerability Scanning:** Enabled on push (`scanOnPush=true`)
- **Encryption at Rest:** Enabled using AES-256 (`encryptionType=AES256`)

### Management CLI Commands

- **Create the Repository:**
  ```bash
  aws ecr create-repository \
      --repository-name project-lab-backend \
      --region us-east-2 \
      --image-scanning-configuration scanOnPush=true \
      --encryption-configuration encryptionType=AES256
  ```
- **Delete the Repository (Forces deletion of images inside):**
  ```bash
  aws ecr delete-repository \
      --repository-name project-lab-backend \
      --region us-east-2 \
      --force
  ```
- **Verify/Describe Repositories:**
  ```bash
  aws ecr describe-repositories --region us-east-2
  ```

---

## 2. Amazon EC2 (Elastic Compute Cloud)

The backend runs on an Ubuntu-based virtual machine on EC2.

### Host Machine Specifications

- **Instance Type:** `t3.micro` (1 GB RAM, 2 vCPUs)
- **Operating System:** Ubuntu 24.04 LTS
- **Public IP:** `3.134.108.227`
- **SSH User:** `ubuntu`

### Security Group (`project-lab-backend-sg`)

The firewall rules permit the following inbound traffic:

- **Port 22 (SSH):** Allowed from anywhere (`0.0.0.0/0`) for GitHub Actions deployment and manual administration.
- **Port 8080 (App Traffic):** Allowed from anywhere (`0.0.0.0/0`) to receive API traffic.

### ECR Access Permissions (IAM Role)

Instead of storing AWS API keys on the virtual machine, ECR pull permissions are granted via an IAM Role attached directly to the instance:

- **Role Name:** `EC2-ECR-Pull-Role`
- **Instance Profile:** `EC2-ECR-Pull-Profile`
- **Permissions Policy:** `AmazonEC2ContainerRegistryReadOnly`

---

## 3. Amazon RDS (Relational Database Service)

An external managed PostgreSQL instance is used for data persistence. This keeps the database separate from the EC2 application host.

- **Engine:** PostgreSQL 16
- **Instance Class:** `db.t3.micro` (1 GB RAM, 1 vCPU - Free-Tier eligible)
- **Allocated Storage:** 20 GB GP2/GP3
- **Default Database Name:** `mi_base_datos`
- **Master Username:** `postgres`
- **Public Accessibility:** Disabled (`no-publicly-accessible`)

### Security Group (`project-lab-rds-sg`)

The firewall rules for the database restrict access strictly:

- **Port 5432 (PostgreSQL):** Allowed _only_ from the EC2 instance's security group (`project-lab-backend-sg`).

### Management CLI Commands

- **Authorize Traffic from EC2 Security Group:**

  ```bash
  aws ec2 authorize-security-group-ingress \
      --group-id <RDS_SG_ID> \
      --protocol tcp \
      --port 5432 \
      --source-group <EC2_SG_ID> \
      --region us-east-2
  ```

- **Create the RDS Instance:**

  ```bash
  aws rds create-db-instance \
      --db-instance-identifier project-lab-db \
      --db-instance-class db.t3.micro \
      --engine postgres \
      --engine-version 16 \
      --allocated-storage 20 \
      --master-username postgres \
      --master-user-password <YOUR_PASSWORD> \
      --db-name mi_base_datos \
      --vpc-security-group-ids <RDS_SG_ID> \
      --no-publicly-accessible \
      --region us-east-2
  ```

- **Retrieve the Connection Endpoint:**
  ```bash
  aws rds describe-db-instances \
      --db-instance-identifier project-lab-db \
      --query "DBInstances[0].[DBInstanceStatus, Endpoint.Address]" \
      --output table \
      --region us-east-2
  ```

---

## 4. Host System Tuning & Prerequisites

To ensure stability on a 1 GB machine running the Spring Boot app, the following configurations were applied to the EC2 host:

### A. Swap Space Configuration

1.  Created a **2 GB Swap file** to act as virtual memory, preventing Out-Of-Memory (OOM) kernel crashes when memory spikes:
    ```bash
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    ```

### B. Installing Docker & AWS CLI

1.  Installed Docker Engine to host the containerized application:
    ```bash
    sudo apt update && sudo apt upgrade -y
    sudo apt install -y docker.io awscli
    sudo systemctl enable --now docker
    ```
2.  Configured permission to run docker without `sudo` as the `ubuntu` user:
    ```bash
    sudo usermod -aG docker ubuntu
    newgrp docker
    ```

---

## 5. GitHub Actions CI/CD Pipeline

The workflow is located in [.github/workflows/deploy.yml](file:///Users/russel69jjjas/Desktop/softserve/project-lab-backend/.github/workflows/deploy.yml)

### Workflow Triggers

The pipeline automatically runs whenever code is pushed to `main` and modifications are made to:

- `src/**` (Source files)
- `pom.xml` (Maven dependencies)
- `Dockerfile` (Image build rules)
- `.github/workflows/deploy.yml` (Pipeline settings)

### Pipeline Stages

1.  **Test & Lint:**
    - Sets up JDK 21 and caches Maven dependencies to speed up future runs.
    - Runs code formatting audits via Spotless (`mvn spotless:check`).
    - Runs the suite of unit tests (`mvn test`).
2.  **Build & ECR Push:**
    - Logs in to Amazon ECR.
    - Compiles and builds the production Docker image locally on the GitHub runner.
    - Pushes the built image tagged with the Git commit SHA (`${{ github.sha }}`) to ECR.
3.  **SSH Deploy to EC2:**
    - Connects to the EC2 instance using the repository secrets.
    - Logs into ECR from the host machine.
    - Stops and removes the previous `spring-backend` container.
    - Launches the new container with memory restrictions applied.
    - Performs a cleanup of unused Docker images (`docker system prune -af`).
    - Queries `http://localhost:8080/actuator/health` to confirm successful application boot.

### Required GitHub Actions Secrets

The following secrets must be set in your GitHub Repository under **Settings > Secrets and variables > Actions**:

| Secret Name             | Value Example                                      | Description                                                     |
| :---------------------- | :------------------------------------------------- | :-------------------------------------------------------------- |
| `AWS_ACCESS_KEY_ID`     | `AKIAIOSFODNN7EXAMPLE`                             | IAM access key credential.                                      |
| `AWS_SECRET_ACCESS_KEY` | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYzEXAMPLEKEY`        | IAM secret access key credential.                               |
| `EC2_HOST`              | `3.134.108.227`                                    | The public IP/DNS of the EC2 target instance.                   |
| `EC2_SSH_KEY`           | `-----BEGIN RSA PRIVATE KEY-----...`               | Private key matching the registered `project-lab-backend-key`.  |
| `DB_HOST`               | `project-lab-db.cxxxx.us-east-2.rds.amazonaws.com` | The connection hostname (endpoint address) of the RDS database. |
| `DB_PASSWORD`           | `your_secure_db_password`                          | The database password provided to the container environment.    |

### JVM Memory Management

To run safely inside the 1 GB EC2 environment, the Java Virtual Machine is constrained using:

```bash
-e JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70.0"
```

This restricts Java to a maximum memory usage of 70% of the container space (approx 700MB), ensuring the remainder is free for the host OS and other active processes.
