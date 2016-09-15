docker pull appdynamics/ecommerce-activemq
docker pull appdynamics/ecommerce-oracle
docker pull mysql
docker run -v /etc/localtime:/etc/localtime:ro --name oracle-db -d -p 1521:1521 appdynamics/ecommerce-oracle
docker run -v /etc/localtime:/etc/localtime:ro --name db -e MYSQL_ROOT_PASSWORD=singcontroller -p 3306:3306 -d mysql
docker run  -v /etc/localtime:/etc/localtime:ro --name jms -d appdynamics/ecommerce-activemq
