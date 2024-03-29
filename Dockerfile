FROM openjdk:11
WORKDIR /usr/src/app

ENV PORT=4147
ENV NEO4J_USER=neo4j
ENV NEO4J_PASS=neo4jnew
ENV NEO4J_URI=http://140.121.197.130:7474
ENV ELK_CLUSTER_NAME=docker-cluster
ENV ELK_CLUSTER_NODES=140.121.197.130:4142
ENV ZIPKIN=http://stanley-server.ddns.net:9411/zipkin/
ENV MSABOT_ROOM=C9PF9PKTL
ENV MSABOT_MQIP=140.121.197.130
ENV MSABOT_MQPORT=5502

COPY . .
ENTRYPOINT ["java", "-Dspring.profiles.active=prod","-jar","app.jar"]
