package com.ousmanetaram.springintegration.consummer.configurations;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BatchIntegrationConfiguration {

	public static final String EXCHANGE_NAME = "rainfall.exchange";

	public static final String QUEUE_NAME = "rainfall.queue";

	public static final String ROUTING_KEY = "routing.key";

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	private ConnectionFactory rabbitConnectionFactory;

	@Value("${elasticsearch.rainfall.index:http://localhost:9200/raincell/rainfall}")
	private String searchDBHost;

	@Bean
	public AmqpInboundGateway amqpInboundChannel() {
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(
				rabbitConnectionFactory);
		simpleMessageListenerContainer.addQueues(queue());
		return new AmqpInboundGateway(simpleMessageListenerContainer);

	}


	@Bean
	public IntegrationFlow flow() {
		return IntegrationFlows	.from(amqpInboundChannel())
								.handle(messageConsumer())
								.get();
	}
	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}

	@Bean
	public Queue queue() {
		return new Queue(QUEUE_NAME);
	}

	@Bean
	public MessageHandler messageConsumer() {
		return new org.springframework.messaging.MessageHandler() {
			@Override
			public void handleMessage(Message<?> arg0) throws MessagingException {
				restTemplate().postForEntity(searchDBHost,
						arg0.getPayload()
							.toString(),
						String.class);
			}
		};
	}

	@Bean
	public Binding binding() {
		return BindingBuilder	.bind(queue())
								.to(exchange())
								.with(ROUTING_KEY);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
