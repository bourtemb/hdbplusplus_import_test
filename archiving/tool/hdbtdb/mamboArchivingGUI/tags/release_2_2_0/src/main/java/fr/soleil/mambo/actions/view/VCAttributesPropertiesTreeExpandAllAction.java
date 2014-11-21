/*
 * Created on Jun 28, 2006
 */
package fr.soleil.mambo.actions.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import fr.soleil.mambo.components.view.VCAttributesPropertiesTree;
import fr.soleil.mambo.containers.sub.dialogs.WaitingDialog;
import fr.soleil.mambo.tools.Messages;

/**
 * @author GIRARDOT
 */
public class VCAttributesPropertiesTreeExpandAllAction extends AbstractAction {

	private static final long serialVersionUID = -7022044755086668111L;
	private VCAttributesPropertiesTree vcAttributesPropertiesTree;

	public VCAttributesPropertiesTreeExpandAllAction(
			VCAttributesPropertiesTree vcAttributesPropertiesTree) {
		super();
		this.putValue(Action.NAME, Messages
				.getMessage("VIEW_ACTION_EXPAND_ALL_PROPERTIES_BUTTON"));
		this.vcAttributesPropertiesTree = vcAttributesPropertiesTree;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		WaitingDialog.openInstance();
		try {
			vcAttributesPropertiesTree.expandAll(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		WaitingDialog.closeInstance();
	}

}