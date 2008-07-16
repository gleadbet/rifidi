package org.rifidi.edge.testing;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;
import org.rifidi.edge.common.utilities.thread.AbstractThread;
import org.rifidi.edge.testing.service.TestingService;



/**
 * @author Jerry Maine - jerry@pramari.com
 *
 */
public class TestingServiceImpl extends AbstractThread implements TestingService {
	private static final Log logger = LogFactory.getLog(TestingService.class);
	LinkedBlockingQueue<Class<?>[]> junitLists = new LinkedBlockingQueue<Class<?>[]>();
		
	public TestingServiceImpl(String threadName) {
		super(threadName);		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		JUnitCore junit = new JUnitCore();
		RunListener listener = new JUnitListener();
		junit.addListener(listener);
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e1) {
//
//		}
		try {
			while(running){
				Class<?>[] klasses = junitLists.take();
				junit.run(klasses);
			}
		} catch (InterruptedException e) {
			//ignore this exception
		}	
		junit.removeListener(listener);
		
	}

	@Override
	public void addJunitTests(Class<?>... classes) {
		junitLists.add(classes);
	}

}
