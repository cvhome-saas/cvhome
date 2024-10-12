### missing for releasing 0.0.1
1. finish welcome-ui design , pricing, registration , login pages
2. release docker image to public aws ecr
3. customize login page
4. prevent click on create button if org don't have any store
5. remove separated create page for categories ...

### missing for releasing 0.0.2
1. customize ui based on login role
2. update user profile in store-ui
3. iac for deploy in AWS Fargate Cluster
4. fix product price issue in store-ui

### missing for releasing 0.0.3
1. add support role
2. create admin page for listing (org,stores,users) to impersonate && remove old impersonate functions
3. log impersonate sessions and events
4. iac for deploy in K8S Kind Cluster

### missing for releasing 0.0.4
1. iac for deploy in K8S EKS Cluster
2. add communication service
3. central session store (redis) for store-core-gateway for effective session scale 

### missing for releasing 0.0.5
1. subscription plan using stripe
2. measure subscription plan usage
