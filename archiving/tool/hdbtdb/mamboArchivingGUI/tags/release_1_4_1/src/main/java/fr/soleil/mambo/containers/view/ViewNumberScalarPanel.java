package fr.soleil.mambo.containers.view;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import fr.esrf.tangoatk.widget.util.chart.math.StaticChartMathExpression;
import fr.soleil.mambo.containers.MamboCleanablePanel;
import fr.soleil.mambo.data.view.ViewConfiguration;
import fr.soleil.mambo.datasources.db.extracting.ExtractingManagerFactory;
import fr.soleil.mambo.datasources.db.extracting.IExtractingManager;
import fr.soleil.mambo.tools.GUIUtilities;
import fr.soleil.mambo.tools.Messages;
import fr.soleil.mambo.tools.SpringUtilities;

public class ViewNumberScalarPanel extends MamboCleanablePanel
{
    protected StaticChartMathExpression chart;
    protected JScrollPane chartScrollPane;
    private JLabel loadingLabel;
    private boolean cleaning = false;

    public ViewNumberScalarPanel (StaticChartMathExpression _chart)
    {
        super();
        this.chart = _chart;
        loadingLabel = new JLabel(Messages.getMessage("VIEW_ATTRIBUTES_NOT_LOADED"));
        loadingLabel.setForeground(Color.RED);
        add(loadingLabel);
        chartScrollPane = new JScrollPane( chart );
        GUIUtilities.setObjectBackground(chartScrollPane, GUIUtilities.VIEW_COLOR);
        GUIUtilities.setObjectBackground(chartScrollPane.getViewport(), GUIUtilities.VIEW_COLOR);
        add(chartScrollPane);
        initLayout();
        initBorder();
        GUIUtilities.setObjectBackground(this, GUIUtilities.VIEW_COLOR);
    }

    public void initLayout() {
        this.setLayout(new SpringLayout());
        SpringUtilities.makeCompactGrid(
                this,
                this.getComponentCount(), 1,
                5, 5,
                5, 5,
                true
        );
    }

    private void initBorder() {
        String msg = getFullName();
        TitledBorder tb = BorderFactory.createTitledBorder
                ( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) ,
                  msg ,
                  TitledBorder.CENTER ,
                  TitledBorder.TOP );
        Border border = ( Border ) ( tb );
        this.setBorder( border );
        border = null;
    }

    public void clean ()
    {
        cleaning = true;
        removeAll();
        chartScrollPane = null;
        loadingLabel = null;
        try
        {
            chart.getY1Axis().clearDataView();
        }
        catch(Exception e)
        {
            return;
        }
        finally
        {
            chart = null;
        }
    }

    public void loadPanel ()
    {
        IExtractingManager extractingManager = ExtractingManagerFactory.getCurrentImpl ();
        
        if (extractingManager.isCanceled() || cleaning) return;
        try
        {
            if (chart != null)
            {
                ViewConfiguration.getSelectedViewConfiguration().loadAttributes(chart);
                remove(chartScrollPane);
                chartScrollPane = null;
                if ( chart.getY1Axis().getViews().isEmpty()
                     && chart.getY2Axis().getViews().isEmpty()
                     && chart.getXAxis().getViews().isEmpty()
                   )
                {
                    JLabel empty = new JLabel(Messages.getMessage("VIEW_ATTRIBUTES_NO_DATA"), JLabel.CENTER);
                    empty.setOpaque(false);
                    chartScrollPane = new JScrollPane( empty );
                    empty = null;
                }
                else
                {
                    chartScrollPane = new JScrollPane( chart );
                }
                GUIUtilities.setObjectBackground(chartScrollPane, GUIUtilities.VIEW_COLOR);
                GUIUtilities.setObjectBackground(chartScrollPane.getViewport(), GUIUtilities.VIEW_COLOR);
            }
            loadingLabel.setText(Messages.getMessage("VIEW_ATTRIBUTES_LOADED"));
            loadingLabel.setToolTipText(Messages.getMessage("VIEW_ATTRIBUTES_LOADED"));
            loadingLabel.setForeground(Color.GREEN);
            add(chartScrollPane);
            initLayout();
            updateUI();
            repaint();
            
         }
        catch(java.lang.OutOfMemoryError oome)
        {
        	if (extractingManager.isCanceled() || cleaning) return;
        	outOfMemoryErrorManagement(); 
        	return ;
        }
        catch(Exception e)
        {
            if (extractingManager.isCanceled() || cleaning) return;
            
            else e.printStackTrace();
        }
    }

    public String getName ()
    {
        return Messages.getMessage("DIALOGS_VIEW_TAB_SCALAR_TITLE");
    }

    public String getFullName ()
    {
        return Messages.getMessage("DIALOGS_VIEW_TAB_SCALAR_TITLE");
    }
}