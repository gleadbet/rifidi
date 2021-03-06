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
package org.rifidi.edge.adapter.thingmagic;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.edge.api.SessionStatus;
import org.rifidi.edge.notification.NotifierService;
import org.rifidi.edge.sensors.AbstractCommandConfiguration;
import org.rifidi.edge.sensors.AbstractSensor;
import org.rifidi.edge.sensors.ByteMessage;
import org.rifidi.edge.sensors.Command;
import org.rifidi.edge.sensors.sessions.AbstractPollIPSensorSession;
import org.rifidi.edge.sensors.sessions.MessageParsingStrategyFactory;

/**
 * 
 * 
 * @author Matthew Dean
 */
public class ThingmagicReaderSession extends AbstractPollIPSensorSession {

	/** Logger for this class. */
	private static final Log logger = LogFactory
			.getLog(ThingmagicReaderSession.class);
	/** Service used to send out notifications */
	private volatile NotifierService notifierService;
	/** The FACTORY_ID of the reader this session belongs to */
	private final String readerID;

	/**
	 * Constructor
	 * 
	 * @param sensor
	 * @param id
	 *            The FACTORY_ID of the session
	 * @param host
	 *            The IP to connect to
	 * @param port
	 *            The port to connect to
	 * @param reconnectionInterval
	 *            The wait time between reconnect attempts
	 * @param maxConAttempts
	 *            The maximum number of times to try to connect
	 * @param destination
	 *            The JMS destination for tags
	 * @param template
	 *            The JSM template for tags
	 * @param notifierService
	 *            The service for sending client notifications
	 * @param readerID
	 *            The FACTORY_ID of the reader that created this session
	 * @param commands
	 *            A thread safe set containing all available commands
	 */
	public ThingmagicReaderSession(AbstractSensor<?> sensor, String id,
			String host, int port, int reconnectionInterval,
			int maxConAttempts, NotifierService notifierService,
			String readerID, Set<AbstractCommandConfiguration<?>> commands) {
		super(sensor, id, host, port, reconnectionInterval, maxConAttempts,
				commands);
		this.notifierService = notifierService;
		this.readerID = readerID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rifidi.edge.core.sensors.base.AbstractIPSensorSession#
	 * getMessageParsingStrategyFactory()
	 */
	@Override
	public MessageParsingStrategyFactory getMessageParsingStrategyFactory() {
		return new ThingmagicMessageParsingStrategyFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.readers.impl.AbstractReaderSession#setStatus(org
	 * .rifidi.edge.core.api.SessionStatus)
	 */
	@Override
	protected void setStatus(SessionStatus status) {
		super.setStatus(status);

		if (logger.isDebugEnabled()) {
			logger.debug(this.getID() + " is " + status);
		}

		// TODO: Remove this once we have aspectJ
		notifierService.sessionStatusChanged(this.readerID, this.getID(),
				status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.base.AbstractIPSensorSession#onConnect()
	 */
	@Override
	public boolean onConnect() throws IOException {

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.sensors.SensorSession#getResetCommand()
	 */
	@Override
	protected Command getResetCommand() {
		return new Command("ThingMagicReset") {
			@Override
			public void run() {
				// TODO: Anything to do here?
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.sensors.SensorSession#submit(java.lang.String,
	 * long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public Integer submit(String commandID, long interval, TimeUnit unit) {
		Integer retVal = super.submit(commandID, interval, unit);
		// TODO: Remove this once we have aspectJ
		try {
			notifierService.jobSubmitted(this.readerID, this.getID(), retVal,
					commandID, (interval > 0));
		} catch (Exception e) {
			// make sure the notification doesn't cause this method to exit
			// under any circumstances
			logger.error(e);
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.readers.impl.AbstractReaderSession#killComand(java
	 * .lang.Integer)
	 */
	@Override
	public void killComand(Integer id) {
		super.killComand(id);

		// TODO: Remove this once we have aspectJ
		notifierService.jobDeleted(this.readerID, this.getID(), id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.sessions.AbstractIPSensorSession#sendMessage
	 * (org.rifidi.edge.sensors.messages.ByteMessage)
	 */
	@Override
	public void sendMessage(ByteMessage message) throws IOException {
		super.sendMessage(message);
	}

}
