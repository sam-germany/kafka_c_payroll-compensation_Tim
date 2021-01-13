package com.course.microservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

	@Autowired
	private KafkaProperties kafkaProperties;

	private ConsumerFactory<Object, Object> consumerFactory22() {
		var properties = kafkaProperties.buildConsumerProperties();
		return new DefaultKafkaConsumerFactory<>(properties);
	}

@Bean(value = "deadLetterContainerFactory")
public ConcurrentKafkaListenerContainerFactory<Object, Object> deadLetterContainerFactory(
			          ConcurrentKafkaListenerContainerFactoryConfigurer configurer, KafkaTemplate<Object, Object> template) {

		var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
		configurer.configure(factory, consumerFactory22());

	System.out.println("------------------1111");
		var recoverer = new DeadLetterPublishingRecoverer(template);

		factory.getContainerProperties().setAckOnError(false); // we are setting as false, as the message we want to consume
	// is still in the pending process as Consumer has not consume it and with setting this we are saying that we are not
	// sending

                                                          		// retry 4 times   with 3 sec interval
		var errorHandler = new SeekToCurrentErrorHandler(recoverer, new FixedBackOff(3000, 1));
		factory.setErrorHandler(errorHandler);

		return factory;
	}

}
