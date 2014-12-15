/*	Synchrotron Soleil 
 *  
 *   File          :  ACRecapCopyAction.java
 *  
 *   Project       :  mambo
 *  
 *   Description   :  
 *  
 *   Author        :  SOLEIL
 *  
 *   Original      :  8 d�c. 2005 
 *  
 *   Revision:  					Author:  
 *   Date: 							State:  
 *  
 *   Log: ACRecapCopyAction.java,v 
 *
 */
package fr.soleil.mambo.actions.archiving;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import fr.soleil.mambo.containers.archiving.ACRecapPanel;
import fr.soleil.mambo.tools.GUIUtilities;

/**
 * 
 * @author SOLEIL
 */
public class ACRecapCopyAction extends AbstractAction {
	private static ACRecapCopyAction instance = null;

	/**
	 * @return
	 */
	public static ACRecapCopyAction getInstance(String name) {
		if (instance == null) {
			instance = new ACRecapCopyAction(name);
		}

		return instance;
	}

	public static ACRecapCopyAction getInstance() {
		return instance;
	}

	/**
	 * @param name
	 */
	private ACRecapCopyAction(String name) {
		super.putValue(Action.NAME, name);
		super.putValue(Action.SHORT_DESCRIPTION, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		StringSelection stringSelection = new StringSelection(ACRecapPanel
				.getInstance(false).selectedToString().replaceAll("\n",
						GUIUtilities.CRLF));
		clipboard.setContents(stringSelection, stringSelection);
	}

}