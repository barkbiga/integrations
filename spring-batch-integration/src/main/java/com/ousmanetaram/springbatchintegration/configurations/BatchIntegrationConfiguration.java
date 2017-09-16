package com.ousmanetaram.springbatchintegration.configurations;

import java.io.File;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import com.ousmanetaram.springbatchintegration.channels.RendezVousChannel;
import com.ousmanetaram.springbatchintegration.transformers.FileToRainItemTransformer;

@Configuration
public class BatchIntegrationConfiguration {

	public static final String EXCHANGE_NAME = "rainfall.exchange";

	public static final String ROUTING_KEY = "routing.key";

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Value("${ftp.host:192.168.56.1}")
	String ftpHost;

	@Value("${ftp.port:21}")
	Integer ftpPort;

	@Value("${ftp.username:anonymous}")
	String ftpUsername;

	@Value("${ftp.password:anonymous}")
	String ftpPassword;

	@Value("${ftp.localdirectory: 'D:\\temp\\'")
	String localDirectory;

	@Bean
	public SessionFactory<FTPFile> sessionFactory() {
		DefaultFtpSessionFactory sessionFactory = new DefaultFtpSessionFactory();
		sessionFactory.setHost(ftpHost);
		sessionFactory.setPort(ftpPort);
		sessionFactory.setUsername(ftpUsername);
		sessionFactory.setPassword(ftpPassword);
		return sessionFactory;
	}

	@Bean
	FtpInboundFileSynchronizer synchronizer() {
		FtpInboundFileSynchronizer ftpInboundFileSynchronizer = new FtpInboundFileSynchronizer(
				sessionFactory());
		ftpInboundFileSynchronizer.setRemoteDirectory("/");
		ftpInboundFileSynchronizer.setDeleteRemoteFiles(true);
		return ftpInboundFileSynchronizer;
	}

	@Bean
	@InboundChannelAdapter(	value = "first.inboundChannel",
							poller = @Poller(fixedRate = "500"))
	public MessageSource<File> ftpInboundChannel() {
		FtpInboundFileSynchronizingMessageSource ftpInbound = new FtpInboundFileSynchronizingMessageSource(
				synchronizer());
		ftpInbound.setLocalDirectory(
				new File(localDirectory));
		ftpInbound.setAutoCreateLocalDirectory(true);
		ftpInbound.start();
		return ftpInbound;
	}


	@Bean
	public IntegrationFlow  firstFlow() {
		return IntegrationFlows	.from("first.inboundChannel")
								.transform(fileToRainItemTransformer())
								.channel(rendezVousChannel())
								.channel("mainRequestChannel")
								// .handle(messageHandler())
								.handle(amqpOutboundEndpoint())
								.get();
	}

	@Bean
	@Transformer(	inputChannel = "first.inboundChannel",
					outputChannel = "rendezVousChannel")
	public FileToRainItemTransformer fileToRainItemTransformer() {
		return new FileToRainItemTransformer();
	}

	@Bean
	@ServiceActivator(inputChannel = "rendezVousChannel")
	public MessageHandler firstBridge() {
		BridgeHandler handler = new BridgeHandler();
		handler.setOutputChannelName("mainRequestChannel");
		return handler;
	}

	@Bean
	@ServiceActivator(inputChannel = "mainRequestChannel")
	public AmqpOutboundEndpoint amqpOutboundEndpoint() {
		AmqpOutboundEndpoint amqpOutboundEndpoint = new AmqpOutboundEndpoint(
				rabbitTemplate);
		amqpOutboundEndpoint.setExchangeName(EXCHANGE_NAME);
		amqpOutboundEndpoint.setRoutingKey(ROUTING_KEY);
		return amqpOutboundEndpoint;
	}

	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}
	@Bean
	public MessageChannel rendezVousChannel() {
		return new RendezVousChannel(true);
	}

	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata poller() {
		return Pollers	.fixedRate(500)
						.get();
	}


}
