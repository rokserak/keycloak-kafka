FROM quay.io/keycloak/keycloak:15.0.2

WORKDIR /code/
COPY . .

# root needed to install maven
USER 0

RUN microdnf update && microdnf install maven
RUN mvn clean package

RUN mv ./target/keycloak-kafka-1.1.1-jar-with-dependencies.jar /opt/jboss/keycloak/standalone/deployments/
RUN mkdir /opt/jboss/startup-scripts/
RUN cp ./add-kafka-config.cli /opt/jboss/startup-scripts/

# to prevent 'java.nio.file.DirectoryNotEmptyException: /opt/jboss/keycloak/standalone/configuration/standalone_xml_history/current' on startup
RUN rm -rf /opt/jboss/keycloak/standalone/configuration/standalone_xml_history

# from base image
USER 1000

EXPOSE 8080
EXPOSE 8443

ENV KAFKA_TOPIC=keycloak_event
ENV KAFKA_CLIENT_ID=keycloak_producer
# comma seperated in case you run a cluster
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:29092
ENV KAFKA_EVENTS=LOGIN,REGISTER,LOGOUT,CODE_TO_TOKEN,REFRESH_TOKEN,SOCIAL_LINK,REMOVE_SOCIAL_LINK,UPDATE_EMAIL,UPDATE_PROFILE,SEND_PASSWORD_RESET,UPDATE_TOTP,REMOVE_TOTP,SEND_VERIFT_EMAIL,VERIFY_EMAIL
ENV KAFKA_ADMIN_TOPIC=keycloak_admin_event

# default user
ENV KEYCLOAK_USER=keycloak_admin
ENV KEYCLOAK_PASSWORD=test1234

ENV DEBUG=ALL
ENV JAVA_TOOLS_OPTS="-Djboss.as.management.blocking.timeout=300"

# for development just use default H2 in-memory DB instance

# for production setup Postgres instance or whatever DB you prefer
#ENV DB_VENDOR=POSTGRES
#ENV DB_ADDR=
#ENV DB_USER=
#ENV DB_SCHEMA=
#ENV DB_PASSWORD=
#ENV PROXY_ADDRESS_FORWARDING=true

ENTRYPOINT [ "/opt/jboss/tools/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0"]
