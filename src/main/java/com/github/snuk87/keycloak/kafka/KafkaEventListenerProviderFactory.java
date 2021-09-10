package com.github.snuk87.keycloak.kafka;

import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerProviderFactory.class);
	private static final String ID = "kafka";

	private KafkaEventListenerProvider instance;

	private String bootstrapServers;
	private String topicEvents;
	private String topicAdminEvents;
	private String clientId;
	private String[] events;
	private Map<String, Object> kafkaProducerProperties;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (instance == null) {
			instance = new KafkaEventListenerProvider(bootstrapServers, clientId, topicEvents, events, topicAdminEvents,
			    kafkaProducerProperties);
		}

		return instance;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(Scope config) {
		LOG.info("Init kafka module ...");
		topicEvents = config.get("topicEvents", System.getenv("KAFKA_TOPIC"));
		clientId = config.get("clientId", System.getenv("KAFKA_CLIENT_ID"));
		bootstrapServers = config.get("bootstrapServers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
		topicAdminEvents = config.get("topicAdminEvents", System.getenv("KAFKA_ADMIN_TOPIC"));

		String eventsString = config.get("events", System.getenv("KAFKA_EVENTS"));

		if (eventsString != null) {
			events = eventsString.split(",");
		}

		if (topicEvents == null) {
			throw new NullPointerException("topic must not be null.");
		}

		if (clientId == null) {
			throw new NullPointerException("clientId must not be null.");
		}

		if (bootstrapServers == null) {
			throw new NullPointerException("bootstrapServers must not be null");
		}

		if (events == null || events.length == 0) {
			events = new String[1];
			events[0] = "REGISTER";
		}

		String securityProtocol = config.get("security.protocol", System.getenv("KAFKA_SECURITY_PROTOCOL"));
		String sslTruststoreFilename = config.get("ssl.truststore.location", System.getenv("KAFKA_SSL_TRUSTSTORE_FILENAME"));
		String sslTruststorePassword = config.get("ssl.truststore.password", System.getenv("KAFKA_SSL_TRUSTSTORE_PASSWORD"));
		String sslKeystoreFilename = config.get("ssl.keystore.location", System.getenv("KAFKA_SSL_KEYSTORE_FILENAME"));
		String sslKeystorePassword = config.get("ssl.keystore.password", System.getenv("KAFKA_SSL_KEYSTORE_PASSWORD"));
		String sslKeyPassword = config.get("ssl.key.password", System.getenv("KAFKA_SSL_KEY_PASSWORD"));

		if (securityProtocol == null) {
			throw new NullPointerException("security.protocol must be set, set it to PLAINTEXT if you want no encryption");
		}

		if (securityProtocol.equals("SSL")) {
			if (sslTruststoreFilename == null) {
				throw new NullPointerException("ssl.truststore.filename must be set when security.protocol == SSL");
			}

			if (sslTruststorePassword == null) {
				throw new NullPointerException("ssl.truststore.password must be set when security.protocol == SSL");
			}

			if (sslKeystoreFilename == null) {
				throw new NullPointerException("ssl.keystore.filename must be set when security.protocol == SSL");
			}

			if (sslKeystorePassword == null) {
				throw new NullPointerException("ssl.keystore.password must be set when security.protocol == SSL");
			}

			if (sslKeyPassword == null) {
				throw new NullPointerException("ssl.key.password must be set when security.protocol == SSL");
			}
		}

		kafkaProducerProperties = KafkaProducerConfig.init(config);
	}

	@Override
	public void postInit(KeycloakSessionFactory arg0) {
		// ignore
	}

	@Override
	public void close() {
		// ignore
	}
}
