package fr.soleil.mambo.thread;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.ArchivingException;
import fr.soleil.mambo.bean.view.ViewConfigurationBean;
import fr.soleil.mambo.containers.sub.dialogs.WaitingDialog;
import fr.soleil.mambo.containers.view.ViewSelectionPanel;
import fr.soleil.mambo.data.view.ViewConfiguration;
import fr.soleil.mambo.data.view.ViewConfigurationAttribute;
import fr.soleil.mambo.datasources.db.archiving.ArchivingManagerFactory;
import fr.soleil.mambo.datasources.db.extracting.ExtractingManagerFactory;
import fr.soleil.mambo.logs.ILogger;
import fr.soleil.mambo.logs.LoggerFactory;
import fr.soleil.mambo.options.Options;
import fr.soleil.mambo.thread.manager.VCRefreshThreadManager;
import fr.soleil.mambo.tools.Messages;

public class VCRefreshThread extends Thread {

    private boolean               canceled;
    private ViewConfigurationBean viewConfigurationBean;

    public VCRefreshThread(ViewConfigurationBean viewConfigurationBean) {
        super();
        this.setPriority(Thread.MIN_PRIORITY);
        canceled = false;
        this.viewConfigurationBean = viewConfigurationBean;
    }

    @Override
    public void run() {
        WaitingDialog.openInstance();
        if ((!canceled) && (viewConfigurationBean != null)) {
            viewConfigurationBean.getAttributesPanel()
                    .getViewAttributesTreePanel().getTree().saveExpandedPath();
        }

        this.refreshGraph();

        if ((!canceled) && (viewConfigurationBean != null)) {
            viewConfigurationBean.getAttributesPanel()
                    .getViewAttributesTreePanel().getTree().openExpandedPath();
        }
        if (!canceled) {
            WaitingDialog.closeInstance();
        }
        if (!canceled) {
            viewConfigurationBean.getAttributesPanel()
                    .getViewAttributesTreePanel().getViewActionPanel()
                    .putRefreshButton();
        }
        if (!canceled) {
            VCRefreshThreadManager.getInstance().setRefreshThread(null);
        }

    }

    private void refreshGraph() {
        if (ExtractingManagerFactory.getCurrentImpl() != null) {
            // allow database communication
            ExtractingManagerFactory.getCurrentImpl().allow();
        }
        ViewConfiguration selectedVC = viewConfigurationBean
                .getViewConfiguration();
        if ((!canceled) && (selectedVC != null)
                && (viewConfigurationBean != null)) {
            try {
                boolean modification_status = selectedVC.isModified();
                boolean doForceTdbExport = Options.getInstance().getVcOptions()
                        .isDoForceTdbExport();
                viewConfigurationBean.refreshMainUI();
                ViewSelectionPanel.getInstance().updateForceExport();
                prepareLegends();

                if ((!canceled) && selectedVC.getData() != null
                        && (!selectedVC.getData().isHistoric())) {
                    if (doForceTdbExport) {
                        if ((!canceled) && (selectedVC.getAttributes() != null)) {
                            LoggerFactory.getCurrentImpl().trace(
                                    ILogger.LEVEL_INFO,
                                    Messages.getMessage("VIEW_ACTION_EXPORT"));
                            TreeMap<String, ViewConfigurationAttribute> attrs = selectedVC
                                    .getAttributes().getAttributes();
                            Set<String> keySet = attrs.keySet();
                            Iterator<String> keyIterator = keySet.iterator();
                            while ((!canceled) && keyIterator.hasNext()) {
                                String attributeName = keyIterator.next();
                                try {
                                    if (!canceled) {
                                        ArchivingManagerFactory
                                                .getCurrentImpl()
                                                .exportData2Tdb(
                                                        attributeName,
                                                        viewConfigurationBean
                                                                .getAttributesPanel()
                                                                .getViewAttributesTreePanel()
                                                                .getDateRangeBox()
                                                                .getEndDateField()
                                                                .getText());
                                    }
                                } catch (ArchivingException e) {
                                    String msg = Messages
                                            .getMessage("VIEW_ACTION_EXPORT_WARNING")
                                            + attributeName;
                                    LoggerFactory.getCurrentImpl().trace(
                                            ILogger.LEVEL_WARNING, msg);
                                    LoggerFactory.getCurrentImpl().trace(
                                            ILogger.LEVEL_WARNING, e);
                                }
                            } // end while ((!canceled) &&
                            // keyIterator.hasNext())
                        } // end if ((!canceled) &&
                        // (selectedVC.getAttributes() != null))
                    } // end if (doForceTdbExport)
                    else if (!canceled) {
                        LoggerFactory.getCurrentImpl().trace(
                                ILogger.LEVEL_INFO,
                                Messages.getMessage("VIEW_ACTION_NO_EXPORT"));
                    }
                } // end if ((!canceled) && selectedVC.getData() != null &&
                // (!selectedVC.getData().isHistoric()))
                if (!canceled) {
                    // This is where database communication happens
                    viewConfigurationBean.getAttributesPanel()
                            .getViewAttributesGraphPanel().loadPanels();
                    if (!canceled) {
                        selectedVC.setModified(modification_status);
                        viewConfigurationBean.getAttributesPanel()
                                .getViewAttributesTreePanel()
                                .getViewActionPanel()
                                .refreshButtonDefaultColor();
                    }
                } // end if (!canceled)
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } // end if ((!canceled) && (selectedVC != null))
    } // end run()

    private void prepareLegends() {
        if (!canceled) {
            viewConfigurationBean.getViewConfiguration().refreshContent();
        }
    }

    public void cancel() {
        canceled = true;
        if (ExtractingManagerFactory.getCurrentImpl() != null) {
            // Doing so avoids any further database communication
            ExtractingManagerFactory.getCurrentImpl().cancel();
        }
    }

}