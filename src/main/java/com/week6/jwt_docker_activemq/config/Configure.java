package com.week6.jwt_docker_activemq.config;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Conventions;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@Configuration
@EnableJms
public class Configure {
	@Value("${spring.activemq.broker-url}")
	String BROKER_URL;
	@Value("${spring.activemq.user}")
	String BROKER_USERNAME;
	
	@Value("${spring.activemq.password}")
	String BROKER_PASSWORK;
	
	@Bean
	public ActiveMQConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		
		connectionFactory.setTrustAllPackages(true);
		connectionFactory.setBrokerURL(BROKER_URL);
		connectionFactory.setUserName(BROKER_USERNAME);
		connectionFactory.setPassword(BROKER_PASSWORK);
		return connectionFactory;
	}
	@Bean
	public ObjectMapper objecMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
		return mapper;
	}
	
	@Bean
	public MessageConverter messageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setObjectMapper(objecMapper());
		return converter;
	}
	
	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate template = new JmsTemplate();
		
		template.setConnectionFactory(connectionFactory());
		template.setMessageConverter(messageConverter());
		template.setPubSubDomain(true);
		template.setDestinationResolver(destinationResolver());
		template.setDeliveryPersistent(true);
		return template;
	}
	
	@Bean
	public DestinationResolver destinationResolver() {
		
		return new DynamicDestinationResolver() { 
			@Override
			public Destination resolveDestinationName(Session session, String destinationName, boolean pubSubDomain) throws JMSException {
				if (destinationName.endsWith("Topic")) {
					pubSubDomain = true;
				}else {
					pubSubDomain = false;
				}
				return super.resolveDestinationName(session, destinationName, pubSubDomain);				
			}
		};
	}
	
}
