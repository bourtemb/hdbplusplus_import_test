/*
 * Created on Jun 28, 2006
 *
 */
package fr.soleil.mambo.actions.archiving;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import fr.soleil.mambo.components.archiving.ACAttributesRecapTree;
import fr.soleil.mambo.containers.sub.dialogs.WaitingDialog;
import fr.soleil.mambo.tools.Messages;

/**
 * @author GIRARDOT
 */
public class ACAttributesRecapTreeExpandAllAction extends AbstractAction {

	public ACAttributesRecapTreeExpandAllAction() {
		super();
		this.putValue(Action.NAME, Messages
				.getMessage("ARCHIVING_ACTION_EXPAND_ALL_RECAP_BUTTON"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		WaitingDialog.openInstance();
		try {
			ACAttributesRecapTree.getInstance().expandAll(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		WaitingDialog.closeInstance();
	}

}