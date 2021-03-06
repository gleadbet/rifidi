/*******************************************************************************
 * Copyright (c) 2014 Transcends, LLC.
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation; either version 2 of the 
 * License, or (at your option) any later version. This program is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You 
 * should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, 
 * USA. 
 * http://www.gnu.org/licenses/gpl-2.0.html
 *******************************************************************************/
package org.rifidi.edge.sensors.sessions;

import java.io.IOException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.rifidi.edge.sensors.AbstractCommandConfiguration;
import org.rifidi.edge.sensors.AbstractSensor;
import org.rifidi.edge.sensors.ByteMessage;

/**
 * An implementation of IPSensorSession that uses polling semantics.
 * 
 * @author Kyle Neumeier - kyle@pramari.com
 * 
 */
public abstract class AbstractPollIPSensorSession extends
		AbstractIPSensorSession {

	/** Queue for reading messages. */
	private final LinkedBlockingQueue<ByteMessage> readQueue;
	/** The factory to produce MessageProcessingStrategy objects */
	private final QueueingMessageProcessingStrategyFactory qmpsf;

	/**
	 * 
	 * @param sensor
	 * @param ID
	 * @param host
	 * @param port
	 * @param reconnectionInterval
	 * @param maxConAttempts
	 * @param commandConfigurations
	 */
	public AbstractPollIPSensorSession(AbstractSensor<?> sensor, String ID,
			String host, int port, int reconnectionInterval,
			int maxConAttempts,
			Set<AbstractCommandConfiguration<?>> commandConfigurations) {
		super(sensor, ID, host, port, reconnectionInterval, maxConAttempts,
				commandConfigurations);
		readQueue = new LinkedBlockingQueue<ByteMessage>();
		qmpsf = new QueueingMessageProcessingStrategyFactory(readQueue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rifidi.edge.core.sensors.base.AbstractIPSensorSession#
	 * getMessageProcessingStrategyFactory()
	 */
	@Override
	public MessageProcessingStrategyFactory getMessageProcessingStrategyFactory() {
		return qmpsf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rifidi.edge.core.sensors.base.AbstractIPSensorSession#
	 * clearUndelieverdMessages()
	 */
	@Override
	protected void clearUndelieverdMessages() {
		// TODO: Not Thread safe!
		this.readQueue.clear();
	}

	/**
	 * Check if a new message is available.
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean isMessageAvailable() throws IOException {
		return readQueue.peek() != null;
	}

	/**
	 * Receive a message. This method blocks for the amount of time determined
	 * by the org.rifidi.edge.sessions.timeout variable.
	 * 
	 * @return
	 * @throws IOException
	 * @throws if the timeout has expired while waiting.
	 */
	public ByteMessage receiveMessage() throws IOException, TimeoutException {
		return receiveMessage(getTimeout());
	}

	/**
	 * Receive a message. This method blocks until the message is received or
	 * until the given amount of time has expired.
	 * 
	 * @param timeout
	 *            the time to wait for a response in milliseconds
	 * @return The next message from the reader
	 * @throws IOException
	 * @throws TimeoutException
	 *             if the timeout has expired while waiting.
	 */
	public ByteMessage receiveMessage(long timeout) throws IOException,
			TimeoutException {
		try {
			ByteMessage response = readQueue.poll(timeout,
					TimeUnit.MILLISECONDS);
			if (response != null)
				return response;
			else
				throw new TimeoutException(
						"Timed out while waiting for a response");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException();
		}
	}

	/**
	 * A factory that produces new QueueingMessageProcessingStrategy objects
	 * 
	 * @author Kyle Neumeier - kyle@pramari.com
	 * 
	 */
	private class QueueingMessageProcessingStrategyFactory implements
			MessageProcessingStrategyFactory {

		/** The queue to put new objects on */
		private final Queue<ByteMessage> queue;

		public QueueingMessageProcessingStrategyFactory(Queue<ByteMessage> queue) {
			this.queue = queue;
		}

		@Override
		public MessageProcessingStrategy createMessageProcessor() {
			return new QueueingMessageProcessingStrategy(queue);
		}

	}

}
