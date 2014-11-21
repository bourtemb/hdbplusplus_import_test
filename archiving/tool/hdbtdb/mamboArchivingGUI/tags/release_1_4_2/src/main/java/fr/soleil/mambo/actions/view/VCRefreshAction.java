//+======================================================================
// $Source$
//
// Project:      Tango Archiving Service
//
// Description:  Java source code for the class  VCViewAction.
//						(Claisse Laurent) - 5 juil. 2005
//
// $Author$
//
// copyleft :	Synchrotron SOLEIL
//					L'Orme des Merisiers
//					Saint-Aubin - BP 48
//					91192 GIF-sur-YVETTE CEDEX
//
//-======================================================================
package fr.soleil.mambo.actions.view;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTabbedPane;

import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.ArchivingException;
import fr.soleil.mambo.comparators.ViewDataComparator;
import fr.soleil.mambo.containers.sub.dialogs.WaitingDialog;
import fr.soleil.mambo.containers.view.ViewActionPanel;
import fr.soleil.mambo.containers.view.ViewAttributesPanel;
import fr.soleil.mambo.containers.view.ViewAttributesTreePanel;
import fr.soleil.mambo.containers.view.dialogs.DateRangeBox;
import fr.soleil.mambo.containers.view.dialogs.GeneralTab;
import fr.soleil.mambo.containers.view.dialogs.GeneralTabbedPane;
import fr.soleil.mambo.containers.view.dialogs.ViewAttributesGraphePanel;
import fr.soleil.mambo.data.view.ViewConfiguration;
import fr.soleil.mambo.data.view.ViewConfigurationData;
import fr.soleil.mambo.data.view.plot.GeneralChartProperties;
import fr.soleil.mambo.data.view.plot.YAxis;
import fr.soleil.mambo.datasources.db.archiving.ArchivingManagerFactory;
import fr.soleil.mambo.datasources.db.archiving.IArchivingManager;
import fr.soleil.mambo.logs.ILogger;
import fr.soleil.mambo.logs.LoggerFactory;
import fr.soleil.mambo.options.Options;
import fr.soleil.mambo.options.sub.VCOptions;
import fr.soleil.mambo.tools.Messages;


public class VCRefreshAction extends AbstractAction
{
	private static VCRefreshAction instance;
	
	public static VCRefreshAction getInstance( String name ){
		if ( instance == null )
        {
            instance = new VCRefreshAction( name );
        }

        return instance;
	}
	
	public static VCRefreshAction getInstance(){

        return instance;
	}
	
	
    /**
     * @param name
     */
    public VCRefreshAction ( String name )
    {
        putValue( Action.NAME , name );
        putValue( Action.SHORT_DESCRIPTION , name );
    }

//    public VCRefreshAction() {
//		super();
//	}

	/* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed ( ActionEvent actionEvent )
    {
    	this.refreshGraphe();
    }

    private void prepareLegends ()
    {
        ViewConfiguration selectedVC = ViewConfiguration.getSelectedViewConfiguration();
        selectedVC.refreshContent ();
    }
    
    public void refreshGraphe(){
    	
        System.gc();
        ViewConfiguration selectedVC = ViewConfiguration.getSelectedViewConfiguration();
        if (selectedVC == null )
        {
            return;
        }
        WaitingDialog.openInstance();
        try
        {
            
            //Options options = Options.
            VCOptions vcOptions = Options.getInstance ().getVcOptions ();
            boolean doForceTdbExport = vcOptions.isDoForceTdbExport (); 
            ILogger logger = LoggerFactory.getCurrentImpl();

            if ( selectedVC.getData() != null
                    && !selectedVC.getData().isHistoric() )
            {
                if ( doForceTdbExport )
                {
                    if ( selectedVC.getAttributes() != null )
                    {
                        logger.trace( ILogger.LEVEL_INFO, Messages.getMessage( "VIEW_ACTION_EXPORT" ) );
                        TreeMap attrs = selectedVC.getAttributes().getAttributes();
                        Set keySet = attrs.keySet();
                        Iterator keyIterator = keySet.iterator();
                        while (keyIterator.hasNext())
                        {
                            Object obj = keyIterator.next();
                            if ( obj instanceof String )
                            {
                                String attributeName = (String) obj;
                                try
                                {
                                    //this.manager.ExportData2Tdb( attributeName );
                                    IArchivingManager archivingManager = ArchivingManagerFactory.getCurrentImpl ();
                                    archivingManager.exportData2Tdb ( attributeName );
                                }
                                catch (ArchivingException e)
                                {
                                    String msg = Messages.getMessage( "VIEW_ACTION_EXPORT_WARNING" ) + attributeName;
                                    logger.trace( ILogger.LEVEL_WARNING, msg );
                                    logger.trace( ILogger.LEVEL_WARNING, e );
                                }
                            }
                        }
                    }
                }
                else
                {
                    logger.trace( ILogger.LEVEL_INFO, Messages.getMessage( "VIEW_ACTION_NO_EXPORT" ) );
                }
            }
            prepareLegends();
            
            GeneralTab.getInstance().getDateRangeBox().update(ViewAttributesTreePanel.getInstance().getDateRangeBox());

            //--setting the generic plot data
            GeneralTabbedPane generalTabbedPane = GeneralTabbedPane.getInstance();
            GeneralChartProperties generalChartProperties = generalTabbedPane.getGeneralChartProperties();
            YAxis y1Axis = generalTabbedPane.getY1Axis();
            YAxis y2Axis = generalTabbedPane.getY2Axis();

            ViewConfigurationData newData = new ViewConfigurationData( generalChartProperties , y1Axis , y2Axis );
            ViewConfigurationData oldData = ViewConfiguration.getCurrentViewConfiguration().getData();

        	newData.setHistoric( oldData.isHistoric() );
            newData.setName( oldData.getName() );
            newData.setSamplingType( oldData.getSamplingType() );

            //--setting the generic plot data
            if(ViewConfigurationData.verifyDates( ViewAttributesTreePanel.getInstance().getDateRangeBox() )){
                newData.upDateVCData( ViewAttributesTreePanel.getInstance().getDateRangeBox() );
            }
            /* else{ //dans le cas ou la saisi est fausse
             	return;
             } 
             */
            //-------------------
//            ViewAttributesPanel viewAttributesPanel = ViewAttributesPanel.getInstance();
            ViewAttributesGraphePanel viewAttributesGraphePanelOld = ViewAttributesGraphePanel.getInstance();
            
            JTabbedPane attributesTab = viewAttributesGraphePanelOld.getAttributesTab();
            
            int numberTab = 0;
            if( attributesTab != null ) {
            	numberTab = attributesTab.getSelectedIndex();
            	if( numberTab < 0 )numberTab = 0;
                viewAttributesGraphePanelOld.clean();
            }
            
            ViewAttributesGraphePanel viewAttributesGraphePanelNew = new ViewAttributesGraphePanel();
            viewAttributesGraphePanelNew.getAttributesTab().setSelectedIndex( numberTab );
            viewAttributesGraphePanelOld.add( viewAttributesGraphePanelNew.getAttributesTab() );
            viewAttributesGraphePanelOld.setAttributesTab( viewAttributesGraphePanelNew.getAttributesTab() );
            viewAttributesGraphePanelOld.setLayout();
            viewAttributesGraphePanelOld.repaint();
            viewAttributesGraphePanelOld.validate();
            
//            viewAttributesPanel.remove( viewAttributesGraphePanelOld );
////            viewAttributesGraphePanel.clearInstance();
//            ViewAttributesGraphePanel viewAttributesGraphePanelNew = new ViewAttributesGraphePanel();
//            ViewAttributesGraphePanel.setInstance( viewAttributesGraphePanelNew );
//            viewAttributesPanel.add( viewAttributesGraphePanelNew );
//            viewAttributesPanel.setLayout();
//            viewAttributesPanel.repaint();
//            viewAttributesPanel.validate();
            ViewActionPanel.getInstance().refreshButtonDefaultColor();
            WaitingDialog.closeInstance();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            WaitingDialog.closeInstance();
        }
    }
    
    
    /**
     * 
     */
    /*private void prepareLegends ()
    {
        //i don't know why but this makes the curve legends appear correctly..

        GeneralTabbedPane generalTabbedPane = GeneralTabbedPane.getInstance();
        GeneralChartProperties generalChartProperties = generalTabbedPane.getGeneralChartProperties();
        YAxis y1Axis = generalTabbedPane.getY1Axis();
        YAxis y2Axis = generalTabbedPane.getY2Axis();
        ViewConfigurationData newData = new ViewConfigurationData( generalChartProperties , y1Axis , y2Axis );

        ViewConfiguration selectedVC = ViewConfiguration.getSelectedViewConfiguration();
        ViewConfigurationData oldData = selectedVC.getData();
        newData.setCreationDate( oldData.getCreationDate() );
        newData.setPath( oldData.getPath() );

        newData.setLastUpdateDate( GUIUtilities.now() );

        //--do not remove
        boolean dynamic = oldData.isDynamicDateRange();
        if ( dynamic )
        {
            Timestamp[] range = oldData.getDynamicStartAndEndDates();
            newData.setStartDate( range[ 0 ] );
            newData.setEndDate( range[ 1 ] );
        }
        else
        {
            newData.setStartDate( oldData.getStartDate() );
            newData.setEndDate( oldData.getEndDate() );
        }
        newData.setDynamicDateRange( dynamic );
        newData.setDateRange( oldData.getDateRange() );
        newData.setHistoric( oldData.isHistoric() );
        newData.setName( oldData.getName() );
        newData.setSamplingType( oldData.getSamplingType() );

        selectedVC.setData( newData );
        //--do not remove
    }*/

}