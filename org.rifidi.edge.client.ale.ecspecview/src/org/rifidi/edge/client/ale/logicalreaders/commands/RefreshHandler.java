
package org.rifidi.edge.client.ale.logicalreaders.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.rifidi.edge.client.ale.ecspecview.Activator;

/**
 * TODO: Method level comment.  
 * 
 * @author Jochen Mader - jochen@pramari.com
 */
public class RefreshHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator.getDefault().getAleLrService().reload();
		return null;
	}

}