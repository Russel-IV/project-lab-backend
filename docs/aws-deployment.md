# AWS Backend Deployment

This document describes how to deploy the backend microservices to AWS. It explains the CI/CD process.

## 1. System Overview

The deployment process uses GitHub Actions, Amazon ECR, and one AWS EC2 instance.

1. You push code changes to GitHub.
2. GitHub Actions runs the tests.
3. GitHub Actions builds the Docker images for the changed services.
4. GitHub Actions pushes the images to Amazon ECR.
5. GitHub Actions connects to the EC2 instance using SSH.
6. The EC2 instance pulls the new images from ECR.
7. The EC2 instance restarts only the changed services.

## 2. Prerequisites

You must set up these items in your AWS account before you run the pipeline.

### AWS IAM OIDC Provider

You must use OIDC to connect GitHub to AWS. Do not use static AWS access keys.

1. Go to the AWS IAM console.
2. Select **Identity providers**.
3. Select **Add provider**.
4. Select **OpenID Connect**.
5. Set the Provider URL to `https://token.actions.githubusercontent.com`.
6. Set the Audience to `sts.amazonaws.com`.
7. Select **Add provider**.

### AWS IAM Role

Create an IAM role for GitHub Actions.

1. Go to the AWS IAM console.
2. Select **Roles** and then select **Create role**.
3. Select **Custom trust policy**.
4. Paste the trust policy below. Replace `<YOUR_AWS_ACCOUNT_ID>`, `<GITHUB_ORGANIZATION>`, and `<GITHUB_REPOSITORY>` with your values.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::<YOUR_AWS_ACCOUNT_ID>:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:<GITHUB_ORGANIZATION>/<GITHUB_REPOSITORY>:*"
        }
      }
    }
  ]
}
```

5. Select **Next**.
6. Select **one** of these options to add permissions:
   - **Option A (Easiest)**: Search for the policy name `AmazonEC2ContainerRegistryPowerUser` (AWS managed policy), check the box next to it, and select **Next**.
     * *Note: If you want to use the EC2 Control workflow, you must also attach an inline policy with `ec2:StartInstances`, `ec2:StopInstances`, and `ec2:DescribeInstances` permissions.*
   - **Option B (Most secure)**: Select the **Create inline policy** button in the top right corner. Select the **JSON** tab and paste this custom policy:
     ```json
     {
       "Version": "2012-10-17",
       "Statement": [
         {
           "Effect": "Allow",
           "Action": [
             "ecr:GetAuthorizationToken",
             "ecr:BatchCheckLayerAvailability",
             "ecr:GetDownloadUrlForLayer",
             "ecr:BatchGetImage",
             "ecr:InitiateLayerUpload",
             "ecr:UploadLayerPart",
             "ecr:CompleteLayerUpload",
             "ecr:PutImage"
           ],
           "Resource": "*"
         },
         {
           "Effect": "Allow",
           "Action": [
             "ec2:StartInstances",
             "ec2:StopInstances",
             "ec2:DescribeInstances"
           ],
           "Resource": "*"
         }
       ]
     }
     ```
     Select **Next**, name the policy (for example, `ECRPushAndEC2ControlPolicy`), and select **Create policy**.
7. Name the role `github-actions-ecr-role`.
8. Copy the Role ARN.

### Amazon ECR Repositories

You must create one ECR repository for each microservice. The current name of the repositories are:

- `project-lab/eureka-server`
- `project-lab/gateway`
- `project-lab/identity-service`
- `project-lab/inventory-service`
- `project-lab/booking-service`
- `project-lab/review-service`
- `project-lab/media-service`
- `project-lab/chatbot-service`

#### ECR Lifecycle Policy
To save storage costs, you should set up an ECR Lifecycle Policy on each repository to keep only the latest 3 images. 

Use this JSON policy:
```json
{
  "rules": [
    {
      "rulePriority": 1,
      "description": "Keep only the latest 3 images",
      "selection": {
        "tagStatus": "any",
        "countType": "imageCountMoreThan",
        "countNumber": 3
      },
      "action": {
        "type": "expire"
      }
    }
  ]
}
```

You can apply this policy in the AWS Console under **Lifecycle policies** for each repository, or apply it to all repositories using the AWS CLI.


### AWS EC2 Instance

You must set up one EC2 instance with these items installed:

1. **Docker and Docker Compose**:
   ```bash
   sudo apt update && sudo apt upgrade -y
   sudo apt install -y docker.io
   sudo systemctl enable --now docker
   sudo usermod -aG docker ubuntu
   
   # Install Docker Compose v2 plugin manually
   mkdir -p ~/.docker/cli-plugins
   curl -SL "https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-linux-$(uname -m)" -o ~/.docker/cli-plugins/docker-compose
   chmod +x ~/.docker/cli-plugins/docker-compose
   ```
   *(Note: Log out and log back in to apply the `docker` group membership change).*

2. **AWS CLI** (Required to log in to ECR on the host):
   * For AMD64 (Standard x86) instance:
     ```bash
     sudo apt install -y unzip
     curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
     unzip awscliv2.zip
     sudo ./aws/install
     rm -rf awscliv2.zip aws
     ```
   * For ARM64 (Graviton) instance:
     ```bash
     sudo apt install -y unzip
     curl "https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip" -o "awscliv2.zip"
     unzip awscliv2.zip
     sudo ./aws/install
     rm -rf awscliv2.zip aws
     ```

3. **IAM Instance Profile (ECR Pull Permissions)**:
   You must attach an IAM Role to the EC2 instance so it can authenticate with ECR without static keys:
   * Go to the **AWS IAM Console** -> **Roles** -> **Create role**.
   * Select **AWS service** and choose **EC2** as the use case.
   * Attach the AWS managed policy **`AmazonEC2ContainerRegistryReadOnly`**.
   * Name the role `ec2-ecr-read-role` and select **Create role**.
   * Go to the **EC2 Console** -> **Instances**, select your instance, and click **Actions** -> **Security** -> **Modify IAM role**.
   * Attach `ec2-ecr-read-role` and click **Update IAM role**.

Ensure the working directory `~/project-lab-backend` exists on the host.



## 3. GitHub Secrets

You must add these secrets to your GitHub repository settings. See the [.secrets-example](project-lab-backend/.secrets-example) file in the root directory for examples of these variables:

- `AWS_ROLE_ARN`: The ARN of your IAM role.
- `AWS_REGION`: Your AWS region (for example, `us-east-1`).
- `EC2_HOST`: The public IP address of your EC2 instance.
- `EC2_USER`: The SSH username (for example, `ubuntu`).
- `EC2_SSH_KEY`: The private SSH key for the EC2 instance.
- `EC2_INSTANCE_ID`: The ID of your EC2 instance (for example, `i-0123456789abcdef0`).
- `POSTGRES_USER`: The username for the database.
- `POSTGRES_PASSWORD`: The password for the database.
- `POSTGRES_DB`: The default database name.
- `JWT_SECRET`: The secret key for JWT tokens.
- `CORS_ALLOWED_ORIGINS`: The allowed origins for CORS.
- `SPRING_AI_OPENAI_API_KEY`: The API key for OpenAI.

## 4. CI/CD Pipeline Steps

The pipeline has four jobs.

### Job 1: Test

This job runs the Maven tests. It uses this command:
`./mvnw test`

### Job 2: Detect Changed Modules

This job checks which files changed.

- If you change a file inside a service folder, only that service will build.
- If you change a global file (like `pom.xml` or the workflow file), all services will build.

### Job 3: Build and Push

This job runs for each changed service.

1. It connects to AWS using the OIDC role.
2. It logs in to Amazon ECR.
3. It builds the Docker image for the service.
4. It pushes the image to its ECR repository.

### Job 4: Deploy

This job deploys the updated services to the EC2 instance.

1. It connects to AWS and logs in to ECR.
2. It copies the `docker-compose.prod.yml` file to the EC2 instance using SCP.
3. It connects to the EC2 instance using SSH.
4. It logs the EC2 instance in to ECR.
5. It pulls the new images for the changed services.
6. It restarts only the changed services.
   - When you restart a service, Docker Compose does not restart other healthy services.
   - If a service has dependencies that are not running, Docker Compose starts them.
7. It checks the health of the gateway if you updated the gateway.

## 5. Manual EC2 Instance Control

To save costs, you can turn off your EC2 instance when you do not use it. You can do this directly from GitHub Actions.

1. Go to your GitHub repository.
2. Select **Actions**.
3. Select the **EC2 Control (Start/Stop)** workflow on the left menu.
4. Select **Run workflow**.
5. Select **start** or **stop** from the dropdown menu.
6. Select the green **Run workflow** button.

The workflow will send the command to AWS and wait until the instance changes status.

## 6. Manual Stack Redeployment (No Rebuild)

If you start your EC2 instance after it was turned off, or if you want to pull the latest images from ECR and completely restart all microservices, you can run the redeployment workflow. This does not compile or build code from source, making it very fast.

1. Go to your GitHub repository.
2. Select **Actions**.
3. Select the **Redeploy All Services** workflow on the left menu.
4. Select **Run workflow**.
5. Enter the image tag you want to deploy (default is `latest`).
6. Select the green **Run workflow** button.

The workflow will stop all containers on the host, pull the specified image versions from ECR, start the containers, and verify the Gateway health.


