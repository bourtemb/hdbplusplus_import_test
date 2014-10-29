// +======================================================================
// $Source:
// /cvsroot/tango-cs/archiving/tool/hdbtdb/mamboArchivingGUI/src/main/java/fr/soleil/mambo/actions/view/VCHideAction.java,v
// $
//
// Project: Tango Archiving Service
//
// Description: Java source code for the class VCViewAction.
// (Claisse Laurent) - 5 juil. 2005
//
// $Author$
//
// copyleft : Synchrotron SOLEIL
// L'Orme des Merisiers
// Saint-Aubin - BP 48
// 91192 GIF-sur-YVETTE CEDEX
//
// -======================================================================
package fr.soleil.mambo.actions.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import fr.soleil.mambo.containers.view.ViewAttributesPanel;
import fr.soleil.mambo.tools.Messages;

public class VCHideAction extends AbstractAction {

    private static final long   serialVersionUID = -292176503443738880L;
    private boolean             visible          = true;
    private String              name;
    private ViewAttributesPanel viewAttributesPanel;

    public VCHideAction(String name, ViewAttributesPanel viewAttributesPanel) {
        super();
        this.name = name;
        putValue(Action.NAME, this.name);
        this.viewAttributesPanel = viewAttributesPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (visible == true) {
                visible = false;
                viewAttributesPanel.hideTreePanel();
                name = Messages.getMessage("VIEW_ACTION_SHOW_TREE_VIEW");
                putValue(Action.NAME, name);
            }
            else {
                visible = true;
                viewAttributesPanel.showTreePanel();
                name = Messages.getMessage("VIEW_ACTION_HIDE_TREE_VIEW");
                putValue(Action.NAME, name);
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public boolean getVisible() {
        return visible;
    }

}