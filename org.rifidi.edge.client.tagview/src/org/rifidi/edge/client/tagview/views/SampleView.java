package org.rifidi.edge.client.tagview.views;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.rifidi.edge.client.tagview.utils.AntennaCtabItem;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SampleView extends ViewPart {
	private CTabFolder ctf;
	private int numberOfAntennas;
	// private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private WritableList[] antennaTagLists;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	// class ViewContentProvider implements IStructuredContentProvider {
	// public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	// }
	//
	// public void dispose() {
	// }
	//
	// public Object[] getElements(Object parent) {
	// return new String[] { "One", "Two", "Three" };
	// }
	// }
	//
	// class ViewLabelProvider extends LabelProvider implements
	// ITableLabelProvider {
	// public String getColumnText(Object obj, int index) {
	// return getText(obj);
	// }
	//
	// public Image getColumnImage(Object obj, int index) {
	// return getImage(obj);
	// }
	//
	// public Image getImage(Object obj) {
	// return PlatformUI.getWorkbench().getSharedImages().getImage(
	// ISharedImages.IMG_OBJ_ELEMENT);
	// }
	// }
	//
	// class NameSorter extends ViewerSorter {
	// }
	/**
	 * The constructor.
	 */
	public SampleView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		ctf = new CTabFolder(parent, SWT.BOTTOM);
		// TODO:to be removed later on and called from outside with param
		initView();
		// to be removed

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	public void initView() {
		// Parameter RemoteReader <- get info from there
		this.setPartName("AlienReader1:1");
		this.numberOfAntennas = 3;

		if (ctf != null) {
			antennaTagLists = new WritableList[numberOfAntennas];
			for (int i = 0; i < numberOfAntennas; i++) {
				AntennaCtabItem item = new AntennaCtabItem(ctf, i);
				antennaTagLists[i] = new WritableList();
//				antennaTagLists[i].addListChangeListener(ContentProv)
//				antennaTagLists[i].addListChangeListener(LabelProv)

			}
			ctf.setSelection(0);
		}

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SampleView.this.fillContextMenu(manager);
			}
		});
		// Menu menu = menuMgr.createContextMenu(viewer.getControl());
		// viewer.getControl().setMenu(menu);
		// getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				// ISelection selection = viewer.getSelection();
				// Object obj =
				// ((IStructuredSelection)selection).getFirstElement();
				// showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		// viewer.addDoubleClickListener(new IDoubleClickListener() {
		// public void doubleClick(DoubleClickEvent event) {
		// doubleClickAction.run();
		// }
		// });
	}

	private void showMessage(String message) {
		// MessageDialog.openInformation(
		// viewer.getControl().getShell(),
		// "Sample View",
		// message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		ctf.setFocus();
	}
}