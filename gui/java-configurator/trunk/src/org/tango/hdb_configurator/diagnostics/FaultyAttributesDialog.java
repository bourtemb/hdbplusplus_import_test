//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision:  $
//
// $Log:  $
//
//-======================================================================

package org.tango.hdb_configurator.diagnostics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class FaultyAttributesDialog extends JDialog {

    private JDialog thisDialog;
    private ArrayList<FaultyAttribute> filteredFaultyAttributes;
    private ArrayList<FaultyAttribute> faultyAttributes;
    private JTable table;
    private DataTableModel model;
    private TablePopupMenu popupMenu = new TablePopupMenu();
    private Subscriber subscriber;
    private SubscriberMap subscriberMap;
    private ImageIcon selectedIcon;
    private ImageIcon unselectedIcon;
    private int selectedRow    = -1;
    private static List<String> defaultTangoHosts;


    private static final int columnWidth[] = { 400, 500 };
    private static final  String[] columnNames = {
            "Attribute Names", "Fault description" };

    private static final int ATTRIBUTE_NAME    = 0;
    private static final int FAULT_DESCRIPTION = 1;

    private static final Color selectionBackground   = new Color(0xe0e0ff);
    private static final Color firstColumnBackground = new Color(0xe0e0e0);
	//===============================================================
	/**
	 *	Creates new form FaultyAttributesDialog for several subscribers
	 */
	//===============================================================
    public FaultyAttributesDialog(JFrame parent, SubscriberMap subscriberMap) throws DevFailed {
        super(parent, false);
        this.subscriberMap = subscriberMap;
        initDialog();
    }
	//===============================================================
	/**
	 *	Creates new form FaultyAttributesDialog for several subscribers
	 */
	//===============================================================
    public FaultyAttributesDialog(JFrame parent, Subscriber subscriber) throws DevFailed {
        super(parent, false);
        this.subscriber = subscriber;
        initDialog();
    }
	//===============================================================
	/**
	 *	Creates new form FaultyAttributesDialog for several subscribers
	 */
	//===============================================================
    public FaultyAttributesDialog(JDialog parent, Subscriber subscriber) throws DevFailed {
        super(parent, true);
        this.subscriber = subscriber;
        initDialog();
    }
	//===============================================================
	/**
	 *	Creates new form FaultyAttributesDialog for several subscribers
	 */
	//===============================================================
    public void initDialog() throws DevFailed {
        thisDialog = this;
        SplashUtils.getInstance().startSplash();
        try {
            selectedIcon   = Utils.getInstance().getIcon("selected.gif", 0.75);
            unselectedIcon = Utils.getInstance().getIcon("unselected.gif", 0.75);
            defaultTangoHosts = TangoUtils.getDefaultTangoHostList();
            initComponents();
            buildRecords();
            buildTableComponent();

            //  Display title depending on one/all subscribers
            if (subscriber!=null) {
                setTitle(subscriber.getLabel());
                titleLabel.setText(subscriber.getLabel());
            }
            else {
                setTitle("Attributes on Error");
                titleLabel.setText("Attributes on Error");
            }
            pack();
            ATKGraphicsUtils.centerDialog(this);
        }
        catch (DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            throw e;
        }
        SplashUtils.getInstance().stopSplash();
    }
    //===============================================================
    //===============================================================
    private void buildRecords() throws DevFailed {
        faultyAttributes = new ArrayList<FaultyAttribute>();

        //  Build records depending on one/all subscribers
        if (subscriber!=null) {
            buildRecords(subscriber);
        }
        else {
            //  Do it for all subscribers
            List<Subscriber> subscriberList = subscriberMap.getSubscriberList();
            String errorMessage = "";
            for (Subscriber subscriber : subscriberList) {
                try {
                    buildRecords(subscriber);
                }
                catch (DevFailed e) {
                    errorMessage += e.errors[0].desc;
                }
            }
            if (!errorMessage.isEmpty()) {
                Utils.popupError(this, errorMessage);
            }
        }


            //  Copy to filtered (no filter at start up)
        filteredFaultyAttributes = new ArrayList<FaultyAttribute>();
        for (FaultyAttribute faultyAttribute : faultyAttributes) {
            filteredFaultyAttributes.add(faultyAttribute);
        }
        Collections.sort(filteredFaultyAttributes, new AttributeComparator());
    }
    //===============================================================
    //===============================================================
    private void buildRecords(Subscriber subscriber) throws DevFailed {
        //  Attribute end error lists
        String[]    attributeNames =  {
                "AttributeList",        //  Full list
                "AttributeErrorList",   //  error list
                "AttributeStoppedList",
        };
        List<String[]> list = ArchiverUtils.readStringAttributes(subscriber, attributeNames);
        String[]    attributeList = list.get(0);
        String[]    errorList     = list.get(1);
        String[]    stoppedList   = list.get(2);

        //  And check faulty ones.
        for (int i=0 ; i<attributeList.length && i<errorList.length ; i++) {
            if (!errorList[i].isEmpty()) {
                faultyAttributes.add(
                        new FaultyAttribute(subscriber, attributeList[i], errorList[i], stoppedList));
            }
        }

    }
    //===============================================================
    //===============================================================
    private void buildTableComponent() throws DevFailed {
        try {
            model = new DataTableModel();

            // Create the table
            table = new JTable(model);
            table.setRowSelectionAllowed(true);
            table.setColumnSelectionAllowed(true);
            table.setDragEnabled(false);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
            table.setDefaultRenderer(String.class, new LabelCellRenderer());
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    tableActionPerformed(evt);
                }
            });

            //  Add a subscriber to sort by column
            table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    tableHeaderActionPerformed(evt);
                }
            });

            //	Put it in scrolled pane
            JScrollPane scrollPane = new JScrollPane(table);
            getContentPane().add(scrollPane, BorderLayout.CENTER);

            //  Set column width
            final Enumeration columnEnum = table.getColumnModel().getColumns();
            int i = 0;
            int width = 0;
            TableColumn tableColumn;
            while (columnEnum.hasMoreElements()) {
                width += columnWidth[i];
                tableColumn = (TableColumn) columnEnum.nextElement();
                tableColumn.setPreferredWidth(columnWidth[i++]);
            }

            //  Compute size to display
            pack();
            int height = table.getHeight();
            if (height>800) height = 800;
            scrollPane.setPreferredSize(new Dimension(width, height+20));
        }
        catch (Exception e) {
            e.printStackTrace();
            Except.throw_exception("INIT_ERROR", e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private int selectedColumn = 0;
    private void tableHeaderActionPerformed(MouseEvent event) {
        JTableHeader    tableHeader = (JTableHeader) event.getSource();
        selectedColumn = tableHeader.columnAtPoint(event.getPoint());
        Collections.sort(filteredFaultyAttributes, new AttributeComparator());
        model.fireTableDataChanged();
    }
    //===============================================================
    //===============================================================
    private void tableActionPerformed(MouseEvent event) {

        //	get selected signal
        Point clickedPoint = new Point(event.getX(), event.getY());
        int row = table.rowAtPoint(clickedPoint);
        selectedRow = row;
        table.repaint();

        if (event.getClickCount() == 2) {
            //JOptionPane.showMessageDialog(this, filteredFaultyAttributes.get(row).getInfo());
        }
        else {
            int mask = event.getModifiers();

            //  Check button clicked
            if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
                popupMenu.showMenu(event, filteredFaultyAttributes.get(row));
            }
        }
    }
	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        javax.swing.JButton applyButton = new javax.swing.JButton();
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem updateItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem dismissItem = new javax.swing.JMenuItem();
        javax.swing.JMenu viewMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem summaryItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        jLabel2.setText("            ");
        topPanel.add(jLabel2);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jLabel1.setText("Filter:   ");
        topPanel.add(jLabel1);

        filterTextField.setColumns(25);
        filterTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterTextFieldActionPerformed(evt);
            }
        });
        topPanel.add(filterTextField);

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        topPanel.add(applyButton);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        fileMenu.setText("File");

        updateItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        updateItem.setText("Update");
        updateItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateItemActionPerformed(evt);
            }
        });
        fileMenu.add(updateItem);

        dismissItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        dismissItem.setText("Dismiss");
        dismissItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dismissItemActionPerformed(evt);
            }
        });
        fileMenu.add(dismissItem);

        jMenuBar1.add(fileMenu);

        viewMenu.setText("View");

        summaryItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        summaryItem.setText("Summary");
        summaryItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryItemActionPerformed(evt);
            }
        });
        viewMenu.add(summaryItem);

        jMenuBar1.add(viewMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
    private void dismissItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissItemActionPerformed
        doClose();
    }//GEN-LAST:event_dismissItemActionPerformed
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        applyFilter();
    }//GEN-LAST:event_applyButtonActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void filterTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterTextFieldActionPerformed
        applyFilter();
    }//GEN-LAST:event_filterTextFieldActionPerformed

    //===============================================================
    //===============================================================
    private class AttributeErrorMessage {
        private String message;
        private int    counter = 0;
        private AttributeErrorMessage(String message) {
            this.message = message;
        }
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void summaryItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryItemActionPerformed
        //  Sort attributes by error type (message)
        ArrayList<AttributeErrorMessage>    errorMessages = new ArrayList<AttributeErrorMessage>();
        for (FaultyAttribute faultyAttribute : faultyAttributes) {
            AttributeErrorMessage   attributeErrorMessage = null;
            for (AttributeErrorMessage errorMessage : errorMessages) {
                if (errorMessage.message.equals(faultyAttribute.shortFaultDescription)) {
                    attributeErrorMessage = errorMessage;
                }
            }
            if (attributeErrorMessage==null) {
                attributeErrorMessage = new AttributeErrorMessage(faultyAttribute.shortFaultDescription);
                errorMessages.add(attributeErrorMessage);
            }
            attributeErrorMessage.counter++;
        }
        Collections.sort(errorMessages, new ErrorComparator());

        //  Then build a string and display
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(faultyAttributes.size())).append(" Attributes on error:\n\n");
        for (AttributeErrorMessage errorMessage : errorMessages) {
            sb.append(Integer.toString(errorMessage.counter)).append(":  ")
                    .append(errorMessage.message).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(),
                "Attribute Errors", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_summaryItemActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void updateItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateItemActionPerformed
        try {
            buildRecords();
            model.fireTableDataChanged();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, "Update", e);
        }
    }//GEN-LAST:event_updateItemActionPerformed
	//===============================================================
	//===============================================================
    private void applyFilter() {
        String  filter = filterTextField.getText();
        filteredFaultyAttributes = new ArrayList<FaultyAttribute>();
        for (FaultyAttribute faultyAttribute : faultyAttributes) {
            if (faultyAttribute.attributeName.contains(filter)) {
                filteredFaultyAttributes.add(faultyAttribute);
            }
        }
        model.fireTableDataChanged();
    }
	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose() {
        setVisible(false);
        dispose();
	}
    //===============================================================
    //===============================================================

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filterTextField;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================






    //===============================================================
    //===============================================================
    private class FaultyAttribute {
        String attributeName;
        String faultDescription;
        String shortFaultDescription;
        boolean stopped;
        Subscriber subscriber;
        /** if use another one, cannot configure with Jive */
        private boolean useDefaultTangoHost = false;
        //===========================================================
        FaultyAttribute(Subscriber subscriber,
                String attributeName, String faultDescription, String[] stoppedList) {
            this.subscriber = subscriber;
            this.attributeName = attributeName;
            this.faultDescription = faultDescription;
            stopped = isStopped(attributeName, stoppedList);
            String tangoHost = TangoUtils.getOnlyTangoHost(attributeName);
            for (String defaultTangoHost : defaultTangoHosts) {
                if (tangoHost.equals(defaultTangoHost))
                    useDefaultTangoHost = true;
            }
            //  Check if fault description could be shorter
            //  without attribute name (only attribute field name)
            String attName = attributeName.substring(attributeName.lastIndexOf('/')+1);
            int start = faultDescription.toLowerCase().indexOf(attName.toLowerCase());
            if (start>0) {
                int end = start+attName.length();
                shortFaultDescription = faultDescription.substring(0, start) +
                        " XXX " + faultDescription.substring(end);
            }
            else {
                shortFaultDescription = faultDescription;
            }
        }
        //===========================================================
        private boolean isStopped(String attributeName, String[] stoppedList) {
            for (String stopped : stoppedList)
                if (attributeName.equalsIgnoreCase(stopped))
                    return true;
            return false;
        }
        //===========================================================
        private void configureEvent() {
            String  deviceName = TangoUtils.getOnlyDeviceName(attributeName);
            deviceName = deviceName.substring(0, deviceName.lastIndexOf('/'));
            Utils.startJiveForDevice(deviceName);
        }
        //===========================================================
        private void stopStorage() throws DevFailed {
            // TODO add your handling code here:
            DeviceData  argIn = new DeviceData();
            argIn.insert(attributeName);
            subscriber.command_inout("AttributeStop", argIn);
        }
    }
    //=========================================================================
    //=========================================================================





    //=========================================================================
    /**
     * The Table model
     */
    //=========================================================================
    public class DataTableModel extends AbstractTableModel {
        //==========================================================
        public int getColumnCount() {
            return columnNames.length;
        }

        //==========================================================
        public int getRowCount() {
            return filteredFaultyAttributes.size();
        }

        //==========================================================
        public String getColumnName(int columnIndex) {
            String title;
            if (columnIndex >= getColumnCount())
                title = columnNames[getColumnCount()-1];
            else
                title = columnNames[columnIndex];

            // remove tango host if any
            if (title.startsWith("tango://")) {
                int index = title.indexOf('/', "tango://".length());
                title = title.substring(index+1);
            }

            return title;
        }

        //==========================================================
        public Object getValueAt(int row, int column) {
            //  Value to display is returned by
            // LabelCellRenderer.getTableCellRendererComponent()
            return "";
        }
        //==========================================================
        /**
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         *
         * @param  column the specified co;umn number
         * @return the cell class at first row for specified column.
         */
        //==========================================================
        public Class getColumnClass(int column) {
            if (isVisible())
                return getValueAt(0, column).getClass();
            else
                return null;
        }
        //==========================================================
        //==========================================================
    }
    //=========================================================================
    //=========================================================================



    //=========================================================================
    /**
     * Renderer to set cell color
     */
    //=========================================================================
    public class LabelCellRenderer extends JLabel implements TableCellRenderer {

        //==========================================================
        public LabelCellRenderer() {
            //setFont(new Font("Dialog", Font.BOLD, 11));
            setOpaque(true); //MUST do this for background to show up.
        }

        //==========================================================
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            setBackground(getBackground(row, column));
            FaultyAttribute attribute = filteredFaultyAttributes.get(row);
            switch (column) {
                case ATTRIBUTE_NAME:
                    setText(attribute.attributeName);
                    setToolTipText(null);
                    setIcon(null);
                    break;
                case FAULT_DESCRIPTION:
                    setText(attribute.faultDescription);
                    if (attribute.stopped)
                        setIcon(unselectedIcon);
                    else
                        setIcon(selectedIcon);
                    setToolTipText(attribute.faultDescription);
                    break;
            }
            return this;
        }
        //==========================================================
        private Color getBackground(int row, int column) {
            switch (column) {
                case 0:
                    return firstColumnBackground;

                default:
                    if (selectedRow>=0) {
                        if (row==selectedRow)
                            return selectionBackground;
                    }
                    return Color.white;
            }
        }
        //==========================================================
    }
    //=========================================================================
    //=========================================================================


    //======================================================
    /**
     * Popup menu class
     */
    //======================================================
    private static final int STOP_STORAGE = 0;
    private static final int CONFIGURE    = 1;
    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Stop Archiving",
            "Configure Polling/Events",
    };
    //=======================================================
    //=======================================================
    private class TablePopupMenu extends JPopupMenu {
        private JLabel title;
        private FaultyAttribute selectedAttribute;
        //======================================================
        private TablePopupMenu() {
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                if (menuLabel == null)
                    add(new Separator());
                else {
                    JMenuItem btn = new JMenuItem(menuLabel);
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            menuActionPerformed(evt);
                        }
                    });
                    add(btn);
                }
            }
        }
        //======================================================
        //======================================================
        private void showMenu(MouseEvent event, FaultyAttribute faultyAttribute) {
            title.setText(faultyAttribute.attributeName);
            selectedAttribute = faultyAttribute;

            //noinspection PointlessArithmeticExpression
            getComponent(OFFSET + STOP_STORAGE).setEnabled(!faultyAttribute.stopped);
            getComponent(OFFSET + CONFIGURE).setEnabled(faultyAttribute.useDefaultTangoHost);
            show(table, event.getX(), event.getY());
        }
        //======================================================
        private void menuActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = -1;
            for (int i=0; i<menuLabels.length; i++)
                if (getComponent(OFFSET + i) == obj)
                    itemIndex = i;
            switch (itemIndex){
                case STOP_STORAGE:
                    try {
                        selectedAttribute.stopStorage();
                        //  and then update
                        buildRecords();
                        model.fireTableDataChanged();
                    }
                    catch (DevFailed e) {
                        ErrorPane.showErrorMessage(thisDialog, e.toString(), e);
                    }
                    break;
                case CONFIGURE:
                    selectedAttribute.configureEvent();
                    break;
            }
        }
    }
    //===============================================================
    //===============================================================



    //=========================================================================
    /**
     * Comparator to sort attribute list
     */
    //=========================================================================
    private class AttributeComparator implements Comparator<FaultyAttribute> {
        //======================================================
        public int compare(FaultyAttribute attribute1, FaultyAttribute attribute2) {
            if (selectedColumn==FAULT_DESCRIPTION) {
                return alphabeticalSort(attribute1.faultDescription, attribute2.faultDescription);
            }
            else {
                return alphabeticalSort(attribute1.attributeName, attribute2.attributeName);
            }
        }
        //======================================================
        private int alphabeticalSort(String s1, String s2) {
            if (s1==null)      return 1;
            else if (s2==null) return -1;
            else return s1.compareTo(s2);
        }
        //======================================================
    }
    //===============================================================
    //===============================================================
    private class ErrorComparator implements Comparator<AttributeErrorMessage> {
        //======================================================
        public int compare(AttributeErrorMessage attribute1, AttributeErrorMessage attribute2) {
            if (attribute1.counter==attribute2.counter)
                return 0;

            return  (attribute1.counter>attribute2.counter) ? -1 : 1;
        }
    }
    //===============================================================
    //===============================================================
}
