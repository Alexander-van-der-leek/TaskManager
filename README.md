# Task Management System

A full-stack application for agile task and sprint management with CLI

## Project Overview

This project is a comprehensive task management system that supports:
- Task creation, assignment, and tracking
- Epic management
- Sprint planning and execution
- User role-based permissions
- Google OAuth authentication

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.1.5** - Core application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access
- **PostgreSQL** - Relational database for data storage
- **Flyway** - Database migration
- **JWT** - Token-based authentication

### Frontend
- **CLI Client** - Command-line interface built with Spring Shell

### DevOps & Infrastructure
- **Docker** - Containerization
- **AWS** - Cloud infrastructure
  - ECS (Elastic Container Service) - Container orchestration
  - ECR (Elastic Container Registry) - Docker image repository
  - RDS (Relational Database Service) - PostgreSQL database
  - ALB (Application Load Balancer) - HTTP request routing
  - S3 - CLI distribution
- **Terraform** - Infrastructure as Code
- **GitHub Actions** - CI/CD pipeline

## Project Structure

```
task-management/
├── .github/workflows/    # GitHub Actions CI/CD pipelines
├── server/               # Spring Boot backend application
├── cli/                  # Command-line interface client
└── infrastructure/       # Terraform IaC for AWS deployment
```

## CI/CD Pipeline Setup

The project uses GitHub Actions for continuous integration and deployment to AWS:

### PR Test Workflow
- **Trigger**: Pull requests to main branch
- **Steps**:
  1. Set up Java 17
  2. Run all tests

### Deployment Workflow
- **Trigger**: Manual workflow dispatch
- **Steps**:
  1. Set up Java 17 and AWS credentials
  2. Set up Terraform
  3. Apply Terraform to provision/update infrastructure
  4. Build application with Gradle
  5. Run Flyway database migrations
  6. Build and push Docker image to ECR
  7. Upload CLI client to S3
  8. Force new ECS deployment

## Running Locally

### Prerequisites
- Java 17
- PostgreSQL
- Gradle

### Server Setup
1. Configure database connection in `server/src/main/resources/application.yml`
2. Run the server with Gradle:
   ```
   ./gradlew server:bootRun
   ```

### CLI Setup
1. Configure API connection in `cli/src/main/resources/application.yml`
2. Run the CLI with Gradle:
   ```
   ./gradlew cli:bootRun
   ```

## Deployment

The system is designed to be deployed to AWS using Terraform and GitHub Actions. The infrastructure includes:

- VPC with public subnets
- PostgreSQL RDS instance
- ECS cluster for running the Spring Boot server
- ECR repository for Docker images
- S3 bucket for CLI distribution
- Load balancer for HTTP traffic

To deploy manually:
1. Set up AWS credentials
2. Navigate to infrastructure/terraform
3. Initialize Terraform: `terraform init`
4. Apply the configuration: `terraform apply`
5. Build and push the Docker image
6. Upload the CLI to S3
