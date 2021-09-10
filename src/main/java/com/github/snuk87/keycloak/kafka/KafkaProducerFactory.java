package com.github.snuk87.keycloak.kafka;

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public final class KafkaProducerFactory {

	private KafkaProducerFactory() {

	}

	public static Producer<String, String> createProducer(String clientId, String bootstrapServer,
	    Map<String, Object> optionalProperties) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		String securityProtocol = System.getenv("KAFKA_SECURITY_PROTOCOL");
		props.put("security.protocol", securityProtocol);
		if (securityProtocol.equals("SSL")) {
			props.put("ssl.truststore.location", System.getenv("KAFKA_SSL_TRUSTSTORE_FILENAME"));
			props.put("ssl.truststore.password", System.getenv("KAFKA_SSL_TRUSTSTORE_PASSWORD"));
			props.put("ssl.keystore.location", System.getenv("KAFKA_SSL_KEYSTORE_FILENAME"));
			props.put("ssl.keystore.password", System.getenv("KAFKA_SSL_KEYSTORE_PASSWORD"));
			props.put("ssl.key.password", System.getenv("KAFKA_SSL_KEY_PASSWORD"));
		}

		props.putAll(optionalProperties);

		// fix Class org.apache.kafka.common.serialization.StringSerializer could not be
		// found. see https://stackoverflow.com/a/50981469
		Thread.currentThread().setContextClassLoader(null);

		return new KafkaProducer<>(props);
	}
}
