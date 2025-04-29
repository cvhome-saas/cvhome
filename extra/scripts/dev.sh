docker stop $(docker ps -a |grep 'welcome-ui' | awk '{print $1}')


gradle bootBuildImage -x test -Dorg.gradle.project.version=4.0.2


docker run -p 4300:4300 store-core/welcome-ui:4.0.2

docker stop $(docker ps -a |grep 'welcome-ui' | awk '{print $1}')
