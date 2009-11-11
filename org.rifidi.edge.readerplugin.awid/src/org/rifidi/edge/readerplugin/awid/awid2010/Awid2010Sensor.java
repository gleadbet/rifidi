/*
 * Awid2010Sensor.java
 * 
 * Created:     Oct 20th, 2009
 * Project:       Rifidi Edge Server - A middleware platform for RFID applications
 *                    http://www.rifidi.org
 *                    http://rifidi.sourceforge.net
 * Copyright:   Pramari LLC and the Rifidi Project
 * License:     The software in this package is published under the terms of the EPL License
 *                   A copy of the license is included in this distribution under Rifidi-License.txt 
 */
package org.rifidi.edge.readerplugin.awid.awid2010;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.edge.api.rmi.dto.CommandDTO;
import org.rifidi.edge.api.rmi.dto.SessionDTO;
import org.rifidi.edge.core.configuration.annotations.JMXMBean;
import org.rifidi.edge.core.configuration.annotations.Property;
import org.rifidi.edge.core.configuration.annotations.PropertyType;
import org.rifidi.edge.core.configuration.mbeanstrategies.AnnotationMBeanInfoStrategy;
import org.rifidi.edge.core.exceptions.CannotCreateSessionException;
import org.rifidi.edge.core.sensors.SensorSession;
import org.rifidi.edge.core.sensors.base.AbstractSensor;
import org.rifidi.edge.core.sensors.commands.AbstractCommandConfiguration;
import org.rifidi.edge.core.sensors.exceptions.CannotDestroySensorException;
import org.rifidi.edge.core.services.notification.NotifierService;
import org.springframework.jms.core.JmsTemplate;

/**
 * The Awid2010 Sensor. It produces Awid2010Sessions.
 * 
 * @author Kyle Neumeier - kyle@pramari.com
 * 
 */
@JMXMBean
public class Awid2010Sensor extends AbstractSensor<Awid2010Session> {

	/** The logger for this class */
	private final static Log logger = LogFactory.getLog(Awid2010Sensor.class);
	/** The IP address of the Awid */
	private volatile String host = Awid2010DefaultValues.HOST;
	/** The port of the Awid */
	private volatile Integer port = Integer
			.parseInt(Awid2010DefaultValues.PORT);
	/** The maximum number of reconnection attempts before giving up */
	private volatile Integer maxNumConnectionAttempts = Integer
			.parseInt(Awid2010DefaultValues.MAX_NUM_RECON_ATTEMPS);
	/** The time to wait between successive reconnection attempts */
	private volatile Integer reconnectionInterval = Integer
			.parseInt(Awid2010DefaultValues.RECON_INTERVAL);
	/** The session, if it has been created */
	private final AtomicReference<Awid2010Session> session = new AtomicReference<Awid2010Session>();
	/** True if this sensor has been destroyed */
	private final AtomicBoolean destroyed = new AtomicBoolean(false);
	/** Spring JMS template */
	private volatile JmsTemplate template;
	/** The counter that keeps track of session IDs */
	private AtomicInteger sessionID = new AtomicInteger(0);
	/** service used to send notifications */
	private volatile NotifierService notifierService;
	/** Provided by spring. */
	private final Set<AbstractCommandConfiguration<?>> commands;
	/** Mbeaninfo for this class. */
	public static final MBeanInfo mbeaninfo;
	static {
		AnnotationMBeanInfoStrategy strategy = new AnnotationMBeanInfoStrategy();
		mbeaninfo = strategy.getMBeanInfo(Awid2010Sensor.class);
	}

	/**
	 * Constructor
	 * 
	 * @param commands
	 */
	public Awid2010Sensor(Set<AbstractCommandConfiguration<?>> commands) {
		super();
		this.commands = commands;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.core.configuration.RifidiService#getMBeanInfo()
	 */
	@Override
	public MBeanInfo getMBeanInfo() {
		return (MBeanInfo) mbeaninfo.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.core.sensors.base.AbstractSensor#getSensorSessions()
	 */
	@Override
	public Map<String, SensorSession> getSensorSessions() {
		Map<String, SensorSession> retVal = new HashMap<String, SensorSession>();
		Awid2010Session awidSession = session.get();
		if (awidSession != null) {
			retVal.put(awidSession.getID(), awidSession);
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.sensors.base.AbstractSensor#createSensorSession()
	 */
	@Override
	public String createSensorSession() throws CannotCreateSessionException {
		if (!destroyed.get() && session.get() == null) {
			Integer sessionID = this.sessionID.incrementAndGet();
			if (session.compareAndSet(null, new Awid2010Session(this, Integer
					.toString(sessionID), host, port, reconnectionInterval,
					maxNumConnectionAttempts, template, commands,
					notifierService))) {

				// TODO: remove this once we get AspectJ in here!
				notifierService.addSessionEvent(this.getID(), Integer
						.toString(sessionID));
				return sessionID.toString();
			}
		}
		throw new CannotCreateSessionException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.sensors.base.AbstractSensor#createSensorSession(
	 * org.rifidi.edge.api.rmi.dto.SessionDTO)
	 */
	@Override
	public String createSensorSession(SessionDTO sessionDTO)
			throws CannotCreateSessionException {
		if (!destroyed.get() && session.get() == null) {
			Integer sessionID = Integer.parseInt(sessionDTO.getID());
			if (session.compareAndSet(null, new Awid2010Session(this, Integer
					.toString(sessionID), host, port, reconnectionInterval,
					maxNumConnectionAttempts, template, commands,
					notifierService))) {

				for (CommandDTO command : sessionDTO.getCommands()) {
					session.get().submit(command.getCommandID(),
							command.getInterval(), command.getTimeUnit());
				}

				// TODO: remove this once we get AspectJ in here!
				notifierService.addSessionEvent(this.getID(), Integer
						.toString(sessionID));
				return sessionID.toString();
			}
		}
		throw new CannotCreateSessionException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.sensors.base.AbstractSensor#destroySensorSession
	 * (java.lang.String)
	 */
	@Override
	public void destroySensorSession(String id)
			throws CannotDestroySensorException {
		Awid2010Session awidSession = session.get();
		if (awidSession != null && awidSession.getID().equals(id)) {
			session.set(null);
			awidSession.killAllCommands();
			awidSession.disconnect();
			awidSession.destroy();
			// TODO: remove this once we get AspectJ in here!
			notifierService.removeSessionEvent(this.getID(), id);
		} else {
			String error = "Tried to delete a non existend session: " + id;
			throw new CannotDestroySensorException(error);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.core.sensors.base.AbstractSensor#destroy()
	 */
	@Override
	public void destroy() {
		if (destroyed.compareAndSet(false, true)) {
			super.destroy();
			Awid2010Session awidSession = session.get();
			if (awidSession != null) {
				try {
					destroySensorSession(awidSession.getID());
				} catch (CannotDestroySensorException e) {
					logger.warn("Cannot Destroy Session: ", e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.sensors.base.AbstractSensor#applyPropertyChanges()
	 */
	@Override
	public void applyPropertyChanges() {
		// Implement this method if we need to send any property changes to the
		// reader

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.sensors.base.AbstractSensor#unbindCommandConfiguration
	 * (org.rifidi.edge.core.sensors.commands.AbstractCommandConfiguration,
	 * java.util.Map)
	 */
	@Override
	public void unbindCommandConfiguration(
			AbstractCommandConfiguration<?> commandConfiguration,
			Map<?, ?> properties) {
		if (!destroyed.get()) {
			Awid2010Session awidSession = session.get();
			if (awidSession != null) {
				awidSession.suspendCommand(commandConfiguration.getID());
			}
		}

	}

	/**
	 * called by the sesnor factory
	 * 
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(JmsTemplate template) {
		this.template = template;
	}

	/**
	 * Called by the sesnor factory
	 * 
	 * @param wrapper
	 *            The JMS Notifier to set
	 */
	public void setNotifiyService(NotifierService wrapper) {
		this.notifierService = wrapper;
	}

	/**
	 * @return the host
	 */
	@Property(displayName = "IP Address", 
			  description = "IP Address of the Reader", 
			  writable = true, 
			  type = PropertyType.PT_STRING,
			  category = "connection", 
		      defaultValue = Awid2010DefaultValues.HOST, 
			  orderValue = 0)
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	@Property(displayName = "Port", 
			  description = "Port of the Reader", 
			  writable = false, 
		      type = PropertyType.PT_INTEGER, 
		      category = "connection", 
		      orderValue = 1, 
			  defaultValue = Awid2010DefaultValues.PORT,
			  minValue = "0",
			  maxValue = "65535")
	public Integer getPort() {
		return port;
	}
	
	public void setPort(Integer port){
		this.port = port;
	}

	/**
	 * @return the maxNumConnectionAttempts
	 */
	@Property(displayName = "Maximum Connection Attempts", 
			  description = "Upon connection failure, the number of times to attempt to recconnect before giving up. If set to '-1', then try forever", 
			  writable = true, 
			  type = PropertyType.PT_INTEGER, 
		      category = "connection", 
			  defaultValue = Awid2010DefaultValues.MAX_NUM_RECON_ATTEMPS, 
		      orderValue = 2, 
		      minValue = "-1")
	public Integer getMaxNumConnectionAttempts() {
		return maxNumConnectionAttempts;
	}

	/**
	 * @param maxNumConnectionAttempts
	 *            the maxNumConnectionAttempts to set
	 */
	public void setMaxNumConnectionAttempts(Integer maxNumConnectionAttempts) {
		this.maxNumConnectionAttempts = maxNumConnectionAttempts;
	}

	/**
	 * @return the reconnectionInterval
	 */
	@Property(displayName = "Reconnection Interval", 
			  description = "Upon connection failure, the time to wait between two connection attempts (ms)", 
			  writable = true, 
			  type = PropertyType.PT_INTEGER, 
			  category = "connection", 
			  defaultValue = Awid2010DefaultValues.RECON_INTERVAL, 
			  orderValue = 3, 
			  minValue = "0")
	public Integer getReconnectionInterval() {
		return reconnectionInterval;
	}

	/**
	 * @param reconnectionInterval
	 *            the reconnectionInterval to set
	 */
	public void setReconnectionInterval(Integer reconnectionInterval) {
		this.reconnectionInterval = reconnectionInterval;
	}
}
