package com.ousmanetaram.springintegration.consummer.configurations;

import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;

public class MessageListener extends AbstractMessageListenerContainer {

	@Override
	protected void doInitialize() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doShutdown() {
		// TODO Auto-generated method stub

	}

}
