package com.ousmanetaram.springbatchintegration.channels;

import java.util.concurrent.SynchronousQueue;

import org.springframework.integration.channel.QueueChannel;

public class RendezVousChannel extends QueueChannel {

	public RendezVousChannel() {
		super();
	}
	/**
	 * 
	 * @param fair
	 *            : if true, waiting threads contend in FIFO order for access;
	 *            otherwise the order is unspecified.
	 */
	public RendezVousChannel(boolean fair) {
		super(new SynchronousQueue<>(fair));
	}

}
