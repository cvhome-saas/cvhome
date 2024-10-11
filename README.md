# cvhome

## Not just e-commerce its a SaaS e-commerce

1. want to host many e-commerce websites for your business, or you want to provide e-commerce as SaaS solution to any
   client around the world
2. every client will have its special subdomain like store1.cvhome.com or custom domain like example.com with automated
   https certificate
3. every store can be served under many domains like example1.com , example2.net
4. with all e-commerce features (Catalog ,Shopping cart ,Checkout ,Merchant ,Order ,Customer ,User)
5. seo friendly with next-js server side rendering for your store
6. every store data is totally separated and not shared to other stores
7. support many user level access  ( SUPER_ADMIN , ORG_ADMIN , STORE_ADMIN , STORE_MODERATOR , CUSTOMER )

### Technologies Used

1. java 21
2. spring boot 3.3
3. spring cloud 2023.0.2
4. caddy
5. postgres
6. keycloak
7. aws s3 | minio
8. angular 17
9. next-js
10. gradle
11. buildPacks using spring
12. docker compose (deployment options for dev|local mode)
13. AWS Fargate (deployment options)
14. AWS EKS (deployment options)
15. Terraform (iac for AWS & AWS EKS deployment options)

### Structure of project

1. this project is a mono repo which is kind for splitting small microservice in one repo
2. we are using gradle subprojects
3. we are using gradle for every thing for example we are building docker images for keycloak and angular and next-js
   and all java modules using gradle
4. in `settings.gradle` every project that end with `-service` is a java microservice and `auth` microservice
5. in `settings.gradle` every project that end with `-ui` is a frontend microservice

### Clusters

so we split the project to `3 main clusters` that can scale independently

so you can have :

1. one `store-core-cluster` hold all system operation like auth, domains, register org, creating n stores
2. One or Many `store-pod-cluster` hold all e-commerce functions like Catalog, Shopping Cart , .... you can have one or
   many of this cluster type based on your need for example if you have special store and want to deploy it in a
   separate cluster with different memory cpu spec and isolated from all stores in the systems , or just one for all
   stores
3. Zero or One Or Many`saas-pod-cluster`  handle https traffic and automate https certificate and renewal then forward
   traffic to `store-pod-cluster` you can have zero or many based on your need so you can ignore it and forward traffic
   from `lb` to `store-pod-cluster` directly if you own the domains and can prove ownership on them so no need to this
   to automate https if not you can use it to automate https you can deploy many clusters also based on your need if you
   have client that need totally isolated gateway from other stores in the system

#### note -->

you still can scale every cluster services horizontal autoscale as normal by scaling number of instances,pods,services

### Microservices

##### --- store-core-cluster --

1. `store-core:welcome-ui` welcome page for the Saas that display provider info , contact, pricing , register
2. `store-core:store-ui` dashboard build using angular to manage org and stores
3. `store-core:auth` identity provider using keycloak which provide secure access to any microservice or any
   service-to-service call
4. `store-core:manager:manager-service` core for creating org , store and registering domain and domain ownership
   validation
5. `store-core:gateway:gateway-service` gateway that provide access to all `store core cluster` microservices

##### --- store-pod-cluster --

1. `store-pod:store:store-service`   responsible for all e-commerce functions like  (Catalog ,Shopping cart ,Checkout
   ,Merchant ,Order ,Customer)
2. `store-pod:landing-ui` server side rending for every store that serve products , landing page , categories , contact
   info for store
3. `store-pod:gateway:gateway-service`  gateway that provide access to all `store pod cluster` microservices specially
   it map every domain like example.com to its corresponding store-id so example.com>>point-to-store-id>>store-id

##### --- store-pod-cluster --

1. `saas-pod:gateway:gateway-service-v2` caddy server responsible for handling https and renewing certificates for every
   domain then forward every request to `store-pod:gateway:gateway-service`

### Installation

1. we require you hava `java 21` , `gradle-8.9` , `node v20` , `docker && docker compose`

### Demo and Deployment

there are 4 options to run cvhome saas project

1. AWS ECS FARGATE --> check here
2. AWS EKS --> check here
3. DOCKER COMPOSE --> check here
4. KIND K8S --> check here

##### but before we start to deploy keep in mind we are populating with some test data (org, stores , users , catalog)

1. keycloak system user sys-admin@mail.com admin

2.

| org                                         | store                                                                                    | username                       | pass  | role            |
|---------------------------------------------|------------------------------------------------------------------------------------------|--------------------------------|-------|-----------------|
| all                                         | all                                                                                      | super-admin@mail.com           | admin | SUPER_ADMIN     |
| ---org1---                                  |                                                                                          | org1-admin@mail.com            | admin | ORG_ADMIN       |
| `id = d1952c95-312e-4bb9-9a2d-b703d031276f` | ---store1---<br/>`id = 65f023632bc46470c104b76f`<br/>`domain = org1-store1.gateway.com`  | org1-store1-admin@mail.com     | admin | STORE_ADMIN     |
|                                             |                                                                                          | org1-store1-moderator@mail.com | admin | STORE_MODERATOR |
|                                             | ---store2--- <br/>`id = 65f023632bc46470c104b75f`<br/>`domain = org1-store2.gateway.com` | org1-store2-admin@mail.com     | admin | STORE_ADMIN     |
|                                             |                                                                                          | org1-store2-moderator@mail.com | admin | STORE_MODERATOR |
| ---org2---                                  |                                                                                          | org2-admin@mail.com            | admin | ORG_ADMIN       |
| `id = d1952c95-312e-4bb6-9a2d-b703d031276f` | ---store1---<br/>`id = 65f020632bc46470c104b76f`<br/>`domain = org2-store1.gateway.com`  | org2-store1-admin@mail.com     | admin | STORE_ADMIN     |
|                                             |                                                                                          | org2-store1-moderator@mail.com | admin | STORE_MODERATOR |
|                                             | ---store2---<br/>`id = 65f023632bc26470c104b75f`<br/>`domain = org2-store2.gateway.com`  | org2-store2-admin@mail.com     | admin | STORE_ADMIN     |
|                                             |                                                                                          | org2-store2-moderator@mail.com | admin | STORE_MODERATOR |

### DOCKER COMPOSE deployment options

#### Steps to run on linux

1. configure hosts in `/etc/hosts` by running `bash scripts/configure-domain.sh`
2. run `docker compose -f docker-compose.yml up`
3. access the application
    - http://gateway.com:7000    access the welcome page
    - http://store-ui.gateway.com:7000    access the dashboard
    - http://org1-store1.gateway.com:7100    access org1-store1 store
    - http://org1-store2.gateway.com:7100    access org1-store2 store
    - http://org2-store1.gateway.com:7100    access org2-store1 store
    - http://org2-store2.gateway.com:7100    access org2-store2 store
