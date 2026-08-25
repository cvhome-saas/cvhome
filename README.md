# cvhome

[![CI Build](https://github.com/cvhome-saas/cvhome/actions/workflows/code-build-check.yml/badge.svg)](https://github.com/cvhome-saas/cvhome/actions/workflows/code-build-check.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/cvhome-saas/cvhome)](https://github.com/cvhome-saas/cvhome/releases)
[![Documentation](https://img.shields.io/badge/docs-cvhome.io-success)](https://cvhome-saas.github.io)

Welcome to **cvhome**, an open-source, multi-tenant e-commerce platform built for scalability and flexibility. It
provides a modern foundation of Java/Spring Boot microservices and Next.js/React/Angular frontends to power
sophisticated online retail operations.

**Key Features:**

* 🚀 **True Multi-Tenancy:** Manage multiple independent stores with custom domains on shared or isolated infrastructure.
* 🔧 **Microservices:** Scalable architecture (Auth, Tenant Mgmt, Store Core) for independent development and deployment.
* ☁️ **Cloud-Native:** Built for AWS (ECS Fargate, RDS) and containerized with Docker.
* 💡 **SaaS Ready:** Essential building blocks for tenant isolation, centralized management, and subscriptions.
* 🔓 **Open Source:** Licensed under Apache 2.0 for transparency and easy customization.

## History

`cvhome` evolves the single-instance model of **[Shopizer](https://github.com/shopizer-ecommerce/shopizer)** into a
scalable, multi-tenant SaaS platform. We've significantly refactored the architecture to introduce robust tenant
isolation, cloud-native optimization, and a modernized technology stack.

---

**➡️ Full Documentation:** **https://cvhome-saas.github.io**

---

## Table of Contents

* [Technology Stack](#technology-stack)
* [Building the Application](#building-the-application)
* [Running Locally](#running-locally)
* [Contributing](#contributing)
* [License](#license)
* [Support](#support)

## Technology Stack

**Backend:**

* Java (JDK 25)
* Spring Boot 4.0.1
* Spring Boot & Spring Cloud
* Spring Data JPA / Hibernate 7.2.0
* Postgres SQL
* Gradle

**Frontend:**

* Npm (v10)
* Node.js (v20)
* TypeScript
* Next.js 16
* React 19
* Angular 20

**Cloud:**

* AWS
* Terraform

**Integration:**

* Stripe

**Other:**

* Caddy
* Minio
* Docker & Docker Compose

## Building the Application

This project uses the Gradle wrapper (`gradlew`) for building. You do not need to install Gradle separately.

To build all microservices and frontend applications:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/cvhome-saas/cvhome.git
   ```
2. **Run the build command:**
   ### On Linux/macOS
    ```bash
   ./gradlew clean build -x test
   ```
   ### On Windows
    ```bash
   gradlew.bat clean build -x test
   ``` 

## Running Locally

### Quick Start (Docker Compose)

* **➡️ For a faster way to get the essential services running using Docker Compose:**
  [Quick Start with Docker Compose Instructions](https://cvhome-saas.github.io/development/local-setup.html#quick-start-with-docker-compose)

### Full Development Setup

Setting up the complete local development environment manually involves multiple steps including dependencies,
configuration, and service startup. This provides more control for detailed development and debugging across services.

* **➡️ Please follow the comprehensive guide available in the official documentation:**

  [Full Development Setup Instructions](https://cvhome-saas.github.io/development/local-setup.html#full-development-setup)

## License

This project is licensed under the **Apache License 2.0**. See the `LICENSE` file in the root of this repository for
details.

## Support

* **Bugs & Feature Requests:** Please use the GitHub Issues tracker for this repository.
* **General Questions & Discussion:** Please check the [Full Documentation](https://cvhome-saas.github.io) first, or
  use [GitHub Discussions](https://github.com/cvhome-saas/cvhome/discussions).

## Contributing

Contributions are welcome! We appreciate help with bug fixes, feature development, testing, and documentation
improvements.

Please read our **[Contributing Guide](https://cvhome-saas.github.io/development/contributing)** before submitting
pull requests. This guide covers:

* Finding issues to work on
* Development workflow (forking, branching)
* Coding style guidelines
* Commit message conventions
* Pull request process    