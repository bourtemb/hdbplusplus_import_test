//+======================================================================
// $Source$
//
// Project:      Tango Archiving Service
//
// Description:  Java source code for the class  VCExpressionHelpAction.
//						(Girardot Rapha�l) - 7 nov. 2006
//
// $Author$
//
// $Revision: 
//
// $Log$
// Revision 1.1  2007/01/11 14:05:46  ounsy
// Math Expressions Management (warning ! requires atk 2.7.0 or greater)
//
//
// copyleft :	Synchrotron SOLEIL
//					L'Orme des Merisiers
//					Saint-Aubin - BP 48
//					91192 GIF-sur-YVETTE CEDEX
//
//-======================================================================
package fr.soleil.mambo.actions.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

import fr.soleil.mambo.containers.view.dialogs.ExpressionTab;
import fr.soleil.mambo.tools.Messages;


public class VCExpressionAddAction extends AbstractAction
{
    private static VCExpressionAddAction instance = null;

    public static VCExpressionAddAction getInstance ()
    {
        if (instance == null)
        {
            instance = new VCExpressionAddAction();
        }
        return instance;
    }

    protected VCExpressionAddAction ( )
    {
        super();
        super.putValue( Action.NAME , Messages.getMessage("EXPRESSION_ADD") );
        super.putValue( Action.SHORT_DESCRIPTION , Messages.getMessage("EXPRESSION_ADD") );
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed ( ActionEvent actionEvent )
    {
        ExpressionTab.getInstance().addExpression();
    }

}