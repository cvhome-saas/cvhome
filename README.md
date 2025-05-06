# cvhome

[![CI Build](https://github.com/cvhome-saas/cvhome/actions/workflows/code-build-check.yml/badge.svg)](https://github.com/cvhome-saas/cvhome/actions/workflows/code-build-check.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/cvhome-saas/cvhome)](https://github.com/cvhome-saas/cvhome/releases)
[![Documentation](https://img.shields.io/badge/docs-cvhome.io-success)](https://cvhome-saas.github.io)

Welcome to **cvhome**, a robust, open-source platform engineered for building scalable, multi-tenant e-commerce
solutions. Designed with flexibility and modern architecture in mind, `cvhome` provides the foundational backend
microservices (Java/Spring Boot) and frontend applications (Next.js/Angular) necessary to power sophisticated online
retail operations.

Whether you're an entrepreneur aiming to launch a niche e-commerce SaaS, an agency managing multiple client stores, or a
developer seeking a powerful e-commerce framework, `cvhome` offers the tools to succeed.

**Key Features & What cvhome Provides:**

* 🚀 **True Multi-Tenancy:** Host and manage numerous independent stores, each with potentially distinct branding,
  products, and configurations, all running on a shared or isolated, optimized infrastructure. **Crucially, each store
  will be mapped
  to its own custom domain (e.g., `mystore.com`, `www.mystore.com`) and have its subdomain (e.g., `mystore.yourplatform.com`) for a
  fully branded
  presence.**
* 🔧 **Microservice Architecture:** Built with scalable and resilient Java/Spring Boot microservices (API Gateway, Auth,
  Tenant Management, Store Core, etc.), allowing for independent development, deployment, and scaling of different
  platform components.
* ☁️ **Cloud-Native Design:** Architected for deployment on cloud platforms like AWS, leveraging services like ECS
  Fargate for container orchestration and RDS for databases (see full documentation for deployment guides).
* 💡 **Foundation for SaaS:** Provides the essential building blocks – tenant isolation, centralized
  management,subscription management, scalable infrastructure patterns – required to build and operate your own
  e-commerce Software-as-a-Service.
* 🔓 **Open Source:** Fully open-source under the Apache 2.0 License, offering transparency, community collaboration, and
  the freedom to customize and extend the platform to meet specific needs.

## History

`cvhome` builds upon the foundation laid by the excellent open-source e-commerce project, **[Shopizer](https://github.com/shopizer-ecommerce/shopizer)**.

While leveraging core e-commerce concepts and potentially some code structures inspired by Shopizer,
`cvhome` significantly enhances and refactors the architecture to introduce robust **Software-as-a-Service (SaaS)**
capabilities.
Key enhancements include:

* **True Multi-Tenancy:** Designed from the ground up to support multiple isolated tenant stores.
* **Tenant Isolation:** Mechanisms to ensure data and configuration separation between tenants.
* **Cloud-Native Architecture:** Optimized for deployment and scaling on cloud platforms like AWS using
  containerization (Docker/Fargate).
* **Subscription Management:** Integrated capabilities for managing tenant subscriptions.
* **Modernized Stack:** Incorporates updated frameworks and technologies (e.g., specific versions of Spring Boot,
  Next.js, Angular).

Essentially,
`cvhome` takes the single-instance e-commerce model and evolves it into a scalable, multi-tenant platform suitable for SaaS providers.
---

**➡️ Full Documentation:** For comprehensive guides on architecture, deployment, concepts, and usage, please visit the
main documentation site: **https://cvhome-saas.github.io**

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

* Java (JDK 23)
* Spring Boot 3.4
* Spring Boot & Spring Cloud
* Spring Data JPA / Hibernate
* Postgres SQL
* Gradle
* Keycloak

**Frontend:**

* Node.js (v18+)
* TypeScript
* Next.js
* Angular

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