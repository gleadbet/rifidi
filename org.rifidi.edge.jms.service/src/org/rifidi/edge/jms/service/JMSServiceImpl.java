package org.rifidi.edge.jms.service;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.rifidi.edge.core.connection.IReaderConnection;
import org.rifidi.services.annotations.Inject;
import org.rifidi.services.registry.ServiceRegistry;

/**
 * @author jerry
 *
 */
public class JMSServiceImpl implements JMSService {
	
	public JMSServiceImpl(){
		ServiceRegistry.getInstance().service(this);
	}

	Map<IReaderConnection, JMSMessageSender> map = new HashMap<IReaderConnection, JMSMessageSender>();
	private ConnectionFactory connectionFactory;
	
	/* (non-Javadoc)
	 * @see org.rifidi.edge.jms.service.JMSService#register(org.rifidi.edge.core.connection.IReaderConnection)
	 */
	@Override
	public boolean register(IReaderConnection connection) {
		JMSHelper jmsHelper = new JMSHelper();
		
		jmsHelper.initializeJMSQueue(connectionFactory, Integer.toString(connection.getSessionID()));
		
		JMSMessageSender jmsThread = new JMSMessageSender(connection.getSessionID(), connection.getAdapter() , jmsHelper);
		
		boolean retVal = jmsThread.start();
		
		if (retVal)
			map.put(connection, jmsThread);
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.rifidi.edge.jms.service.JMSService#unregister(org.rifidi.edge.core.connection.IReaderConnection)
	 */
	@Override
	public void unregister(IReaderConnection connection) {
		JMSMessageSender jmsThread = map.get(connection);
		
		if (jmsThread != null) 
			jmsThread.stop();
		
		map.remove(connection);
	}

	/**
	 * A method helper for the services injection framework.
	 * @param connectionFactory 
	 */
	@Inject
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
}
