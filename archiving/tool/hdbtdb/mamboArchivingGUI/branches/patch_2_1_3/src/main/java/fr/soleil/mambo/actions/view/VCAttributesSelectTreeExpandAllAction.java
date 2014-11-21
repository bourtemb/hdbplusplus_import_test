/*
 * Created on Jun 28, 2006
 */
package fr.soleil.mambo.actions.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import fr.soleil.mambo.components.view.VCAttributesSelectTree;
import fr.soleil.mambo.containers.sub.dialogs.WaitingDialog;
import fr.soleil.mambo.tools.Messages;

/**
 * @author GIRARDOT
 */
public class VCAttributesSelectTreeExpandAllAction extends AbstractAction {

	private static final long serialVersionUID = -3491576551476472995L;
	private VCAttributesSelectTree vcAttributesSelectTree;

	public VCAttributesSelectTreeExpandAllAction(
			VCAttributesSelectTree vcAttributesSelectTree) {
		super();
		this.putValue(Action.NAME, Messages
				.getMessage("VIEW_ACTION_EXPAND_ALL_SELECT_BUTTON"));
		this.vcAttributesSelectTree = vcAttributesSelectTree;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		WaitingDialog.openInstance();
		try {
			vcAttributesSelectTree.expandAll(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		WaitingDialog.closeInstance();
	}

}