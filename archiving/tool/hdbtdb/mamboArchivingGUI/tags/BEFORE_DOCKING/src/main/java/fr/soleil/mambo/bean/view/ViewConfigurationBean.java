package fr.soleil.mambo.bean.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPanel;

import fr.soleil.mambo.actions.view.listeners.IDateRangeBoxListener;
import fr.soleil.mambo.components.view.OpenedVCComboBox;
import fr.soleil.mambo.components.view.VCPopupMenu;
import fr.soleil.mambo.containers.view.ViewAttributesPanel;
import fr.soleil.mambo.containers.view.ViewGeneralPanel;
import fr.soleil.mambo.containers.view.ViewSelectionPanel;
import fr.soleil.mambo.containers.view.dialogs.AttributesPlotPropertiesPanel;
import fr.soleil.mambo.containers.view.dialogs.AxisPanel;
import fr.soleil.mambo.containers.view.dialogs.ChartGeneralTabbedPane;
import fr.soleil.mambo.containers.view.dialogs.GeneralTab;
import fr.soleil.mambo.containers.view.dialogs.VCEditDialog;
import fr.soleil.mambo.containers.view.dialogs.VCVariationDialog;
import fr.soleil.mambo.containers.view.dialogs.VCVariationEditCopyDialog;
import fr.soleil.mambo.data.view.ExpressionAttribute;
import fr.soleil.mambo.data.view.ViewConfiguration;
import fr.soleil.mambo.data.view.ViewConfigurationAttribute;
import fr.soleil.mambo.data.view.ViewConfigurationAttributePlotProperties;
import fr.soleil.mambo.data.view.ViewConfigurationAttributes;
import fr.soleil.mambo.data.view.ViewConfigurationData;
import fr.soleil.mambo.event.DateRangeBoxEvent;
import fr.soleil.mambo.models.VCAttributesTreeModel;
import fr.soleil.mambo.models.VCPossibleAttributesTreeModel;
import fr.soleil.mambo.tools.GUIUtilities;
import fr.soleil.tango.util.entity.data.Domain;
import fr.soleil.tango.util.entity.data.Family;
import fr.soleil.tango.util.entity.data.Member;

public class ViewConfigurationBean implements IDateRangeBoxListener {

    private ViewConfiguration             viewConfiguration;
    private ViewConfiguration             editingViewConfiguration;

    private ViewGeneralPanel              generalPanel;
    private ViewAttributesPanel           attributesPanel;
    private JPanel                        viewDisplayPanel;

    private VCPossibleAttributesTreeModel vcPossibleAttributesTreeModel;
    private VCAttributesTreeModel         editingModel;
    private VCAttributesTreeModel         validatedModel;

    private VCEditDialog                  editDialog;
    private VCVariationDialog             variationDialog;
    private VCVariationEditCopyDialog     variationEditCopyDialog;

    public ViewConfigurationBean(ViewConfiguration viewConfiguration) {
        super();
        this.viewConfiguration = viewConfiguration;
        this.editingViewConfiguration = null;
        initUI();
    }

    private void initUI() {
        boolean historic = true;
        if ((viewConfiguration != null)
                && (viewConfiguration.getData() != null)) {
            historic = viewConfiguration.getData().isHistoric();
        }
        vcPossibleAttributesTreeModel = new VCPossibleAttributesTreeModel(
                historic);
        editingModel = new VCAttributesTreeModel(vcPossibleAttributesTreeModel);
        editingModel.setHistoric(historic);
        validatedModel = new VCAttributesTreeModel(
                vcPossibleAttributesTreeModel);
        validatedModel.setHistoric(historic);
    }

    public ViewConfiguration getViewConfiguration() {
        return viewConfiguration;
    }

    public ViewConfiguration getEditingViewConfiguration() {
        return editingViewConfiguration;
    }

    public void cancelEdition() {
        editingViewConfiguration = null;
        refreshEditingUI();
    }

    public void refreshMainUI() {
        pushSelectedViewConfigurationData();
        pushSelectedAttributes();
        if (viewConfiguration != null) {
            OpenedVCComboBox openedVCComboBox = OpenedVCComboBox.getInstance();
            if (openedVCComboBox != null) {
                openedVCComboBox.selectElement(viewConfiguration);
            }
        }
        getAttributesPanel().getViewAttributesTreePanel().getTree().expandAll(
                true);
        getAttributesPanel().revalidate();
        getAttributesPanel().repaint();
        getViewDisplayPanel().revalidate();
        getViewDisplayPanel().repaint();
    }

    public void refreshEditingUI() {
        pushEditingViewConfigurationData();
        pushEditingAttributes();
        if (editingViewConfiguration == null) {
            getEditDialog().getExpressionTab().getExpressionTree().getModel()
                    .setExpressionAttributes(
                            new TreeMap<String, ExpressionAttribute>());
        }
        else {
            getEditDialog().getExpressionTab().getExpressionTree().getModel()
                    .setExpressionAttributes(
                            editingViewConfiguration.getExpressions());
        }
    }

    public void editViewConfiguration() {
        if (viewConfiguration == null) {
            editingViewConfiguration = new ViewConfiguration();
        }
        else {
            editingViewConfiguration = viewConfiguration.cloneVC();
        }
        getEditDialog().getVcCustomTabbedPane().setEnabledAt(0, true);
        getEditDialog().getVcCustomTabbedPane().setSelectedIndex(0);
        refreshEditingUI();
        getEditDialog().pack();
        Dimension size = getEditDialog().getSize();
        int width = size.width;
        int height = size.height;
        size = null;
        if (width > 800) {
            width = 800;
        }
        if (height > 600) {
            height = 600;
        }
        getEditDialog().setSize(width, height);
        getEditDialog().setLocationRelativeTo(viewDisplayPanel);
        getEditDialog().setVisible(true);
    }

    public VCEditDialog getEditDialog() {
        if (editDialog == null) {
            editDialog = new VCEditDialog(this);
        }
        return editDialog;
    }

    public VCPossibleAttributesTreeModel getVcPossibleAttributesTreeModel() {
        return vcPossibleAttributesTreeModel;
    }

    public VCAttributesTreeModel getEditingModel() {
        return editingModel;
    }

    public VCAttributesTreeModel getValidatedModel() {
        return validatedModel;
    }

    public synchronized ViewAttributesPanel getAttributesPanel() {
        if (attributesPanel == null) {
            attributesPanel = new ViewAttributesPanel(this);
            attributesPanel.getViewAttributesTreePanel().getDateRangeBox()
                    .addDateRangeBoxListener(this);
        }
        return attributesPanel;
    }

    public synchronized ViewGeneralPanel getGeneralPanel() {
        if (generalPanel == null) {
            generalPanel = new ViewGeneralPanel();
        }
        return generalPanel;
    }

    private void pushSelectedViewConfigurationData() {
        ViewConfigurationData data = null;
        if (viewConfiguration != null) {
            data = viewConfiguration.getData();
        }
        if (data == null) {
            getGeneralPanel().setLastUpdateDate(null);
            getGeneralPanel().setCreationDate(null);
            getGeneralPanel().setName(null);
            getGeneralPanel().setPath(null);
        }
        else {
            validatedModel.setHistoric(data.isHistoric());
            getGeneralPanel().setLastUpdateDate(data.getLastUpdateDate());
            getGeneralPanel().setCreationDate(data.getCreationDate());
            getGeneralPanel().setName(data.getName());
            getGeneralPanel().setPath(data.getPath());
        }

        // No need to listen to the DateRangeBox while updating it
        getAttributesPanel().getViewAttributesTreePanel().getDateRangeBox()
                .removeDateRangeBoxListener(this);
        getAttributesPanel().getViewAttributesTreePanel().getDateRangeBox()
                .update(data);
        getAttributesPanel().getViewAttributesTreePanel().getDateRangeBox()
                .addDateRangeBoxListener(this);

        ViewSelectionPanel.getInstance().updateForceExport();
    }

    private void pushEditingViewConfigurationData() {
        ViewConfigurationData data = null;
        if (editingViewConfiguration != null) {
            data = editingViewConfiguration.getData();
        }
        if (data == null) {

        }
        else {
            GeneralTab generalTab = getEditDialog().getGeneralTab();
            generalTab.setLastUpdateDate(data.getLastUpdateDate());
            generalTab.setCreationDate(data.getCreationDate());
            generalTab.setName(data.getName());
            generalTab.setSamplingType(data.getSamplingType());

            boolean dynamic = data.isDynamicDateRange();
            if (dynamic) {
                Timestamp[] range = generalTab.getDynamicStartAndEndDates();
                generalTab.setStartDate(range[0]);
                generalTab.setEndDate(range[1]);
            }
            else {
                generalTab.setStartDate(data.getStartDate());
                generalTab.setEndDate(data.getEndDate());
            }
            generalTab.setDynamicDateRange(dynamic);
            generalTab.setDateRange(data.getDateRange());
            generalTab.setHistoric(data.isHistoric());

            // we need to update the trees according to the "historic" state
            VCAttributesTreeModel vCAttributesTreeModel = getEditDialog()
                    .getAttributesTab().getSelectedAttributesTree().getModel();
            vCAttributesTreeModel.setRootName(data.isHistoric());

            vcPossibleAttributesTreeModel.setHistoric(data.isHistoric());
            editingModel.setHistoric(data.isHistoric());

            // General chart properties
            if (data.getGeneralChartProperties() != null) {
                ChartGeneralTabbedPane generalTabbedPane = getEditDialog()
                        .getGeneralTab().getChartGeneralTabbedPane();
                generalTabbedPane.setAxisGrid(data.getGeneralChartProperties()
                        .getAxisGrid());
                generalTabbedPane.setAxisGridStyle(data
                        .getGeneralChartProperties().getAxisGridStyle());
                generalTabbedPane.setLegendsPlacement(data
                        .getGeneralChartProperties().getLegendsPlacement());
                generalTabbedPane.setBackgroundColor(data
                        .getGeneralChartProperties().getBackgroundColor());
                generalTabbedPane.setDisplayDuration(data
                        .getGeneralChartProperties().getDisplayDuration());
                generalTabbedPane.setLegendsAreVisible(data
                        .getGeneralChartProperties().isLegendsAreVisible());
                generalTabbedPane.setHeaderFont(data
                        .getGeneralChartProperties().getHeaderFont());
                generalTabbedPane.setLabelFont(data.getGeneralChartProperties()
                        .getLabelFont());
                generalTabbedPane.setTitle(data.getGeneralChartProperties()
                        .getTitle());
                generalTabbedPane.setTimePrecision(Integer.toString(data
                        .getGeneralChartProperties().getTimePrecision()));
            }
            // Y1 axis setup
            if (data.getY1() != null) {
                AxisPanel axisPanel = getEditDialog().getGeneralTab()
                        .getChartGeneralTabbedPane().getY1Panel();
                axisPanel.setLabelFormat(data.getY1().getLabelFormat());
                axisPanel.setPosition(data.getY1().getPosition());
                axisPanel.setScaleMode(data.getY1().getScaleMode());
                axisPanel.setAutoScale(data.getY1().isAutoScale());
                axisPanel.setColor(data.getY1().getColor());
                axisPanel.setDrawOpposite(data.getY1().isDrawOpposite());
                axisPanel.setScaleMax(data.getY1().getScaleMax());
                axisPanel.setScaleMin(data.getY1().getScaleMin());
                axisPanel.setShowSubGrid(data.getY1().isShowSubGrid());
                axisPanel.setVisible(data.getY1().isVisible());
                axisPanel.setTitle(data.getY1().getTitle());
            }
            // Y2 axis setup
            if (data.getY2() != null) {
                AxisPanel axisPanel = getEditDialog().getGeneralTab()
                        .getChartGeneralTabbedPane().getY2Panel();
                axisPanel.setLabelFormat(data.getY2().getLabelFormat());
                axisPanel.setPosition(data.getY2().getPosition());
                axisPanel.setScaleMode(data.getY2().getScaleMode());
                axisPanel.setAutoScale(data.getY2().isAutoScale());
                axisPanel.setColor(data.getY2().getColor());
                axisPanel.setDrawOpposite(data.getY2().isDrawOpposite());
                axisPanel.setScaleMax(data.getY2().getScaleMax());
                axisPanel.setScaleMin(data.getY2().getScaleMin());
                axisPanel.setShowSubGrid(data.getY2().isShowSubGrid());
                axisPanel.setVisible(data.getY2().isVisible());
                axisPanel.setTitle(data.getY2().getTitle());
            }
        }
    }

    private void pushSelectedAttributes() {
        ViewConfigurationAttributes attributes = null;
        if (viewConfiguration != null) {
            attributes = viewConfiguration.getAttributes();
        }
        // Save expanded Row
        TreeMap<String, ViewConfigurationAttribute> attributesHash = null;
        if (attributes != null) {
            attributesHash = attributes.getAttributes();
        }
        if (attributesHash == null) {
            attributesHash = new TreeMap<String, ViewConfigurationAttribute>();
        }
        Set<String> keySet = attributesHash.keySet();
        Iterator<String> keyIterator = keySet.iterator();
        Vector<Domain> domainList = new Vector<Domain>();
        while (keyIterator.hasNext()) {
            String completeName = (String) keyIterator.next();
            ViewConfigurationAttribute next = attributesHash.get(completeName);
            String domain_s = next.getDomainName();
            String family_s = next.getFamilyName();
            String member_s = next.getMemberName();
            String attr_s = next.getName();
            ViewConfigurationAttribute attr = new ViewConfigurationAttribute();
            attr.setName(attr_s);
            attr.setCompleteName(completeName);
            attr.setDevice(member_s);
            attr.setDomain(domain_s);
            attr.setFamily(family_s);
            Domain domain = new Domain(domain_s);
            Family family = new Family(family_s);
            Member member = new Member(member_s);
            Domain dom = Domain.hasDomain(domainList, domain_s);
            if (dom != null) {
                Family fam = dom.getFamily(family_s);
                if (fam != null) {
                    Member mem = fam.getMember(member_s);
                    if (mem != null) {
                        mem.addAttribute(attr);
                        fam.addMember(mem);
                        dom.addFamily(fam);
                        domainList = Domain.addDomain(domainList, dom);
                    }
                    else {
                        member.addAttribute(attr);
                        fam.addMember(member);
                        dom.addFamily(fam);
                        domainList = Domain.addDomain(domainList, dom);
                    }
                }
                else {
                    member.addAttribute(attr);
                    family.addMember(member);
                    dom.addFamily(family);
                    domainList = Domain.addDomain(domainList, dom);
                }
            }
            else {
                member.addAttribute(attr);
                family.addMember(member);
                domain.addFamily(family);
                domainList = Domain.addDomain(domainList, domain);
            }
        }
        validatedModel.removeAll();
        validatedModel.build(domainList);
        validatedModel.reload();
        getAttributesPanel().repaint();
    }

    private void pushEditingAttributes() {
        ViewConfigurationAttributes attributes = null;
        if (editingViewConfiguration != null) {
            attributes = editingViewConfiguration.getAttributes();
        }
        // Save expanded Row
        TreeMap<String, ViewConfigurationAttribute> attributesHash = null;
        if (attributes != null) {
            attributesHash = attributes.getAttributes();
        }
        if (attributesHash == null) {
            attributesHash = new TreeMap<String, ViewConfigurationAttribute>();
        }
        Set<String> keySet = attributesHash.keySet();
        Iterator<String> keyIterator = keySet.iterator();
        Vector<Domain> domainList = new Vector<Domain>();
        while (keyIterator.hasNext()) {
            String completeName = (String) keyIterator.next();
            ViewConfigurationAttribute next = attributesHash.get(completeName);
            String domain_s = next.getDomainName();
            String family_s = next.getFamilyName();
            String member_s = next.getMemberName();
            String attr_s = next.getName();
            ViewConfigurationAttribute attr = new ViewConfigurationAttribute();
            attr.setName(attr_s);
            attr.setCompleteName(completeName);
            attr.setDevice(member_s);
            attr.setDomain(domain_s);
            attr.setFamily(family_s);
            Domain domain = new Domain(domain_s);
            Family family = new Family(family_s);
            Member member = new Member(member_s);
            Domain dom = Domain.hasDomain(domainList, domain_s);
            if (dom != null) {
                Family fam = dom.getFamily(family_s);
                if (fam != null) {
                    Member mem = fam.getMember(member_s);
                    if (mem != null) {
                        mem.addAttribute(attr);
                        fam.addMember(mem);
                        dom.addFamily(fam);
                        domainList = Domain.addDomain(domainList, dom);
                    }
                    else {
                        member.addAttribute(attr);
                        fam.addMember(member);
                        dom.addFamily(fam);
                        domainList = Domain.addDomain(domainList, dom);
                    }
                }
                else {
                    member.addAttribute(attr);
                    family.addMember(member);
                    dom.addFamily(family);
                    domainList = Domain.addDomain(domainList, dom);
                }
            }
            else {
                member.addAttribute(attr);
                family.addMember(member);
                domain.addFamily(family);
                domainList = Domain.addDomain(domainList, domain);
            }
        }
        editingModel.removeAll();
        editingModel.build(domainList);
        editingModel.reload();
        if (attributes != null) {
            getEditDialog().getAttributesPlotPropertiesTab()
                    .getPropertiesPanel().setColorIndex(
                            attributes.getColorIndex());
            getEditDialog().getExpressionTab().getPropertiesPanel()
                    .setColorIndex(attributes.getColorIndex());
            getEditDialog().getAttributesPlotPropertiesTab()
                    .getPropertiesPanel().rotateCurveColor();
        }
        if (getEditDialog().isVisible()) {
            getEditDialog().repaint();
        }
    }

    public void pushAttributesPlotProperties(
            ViewConfigurationAttributePlotProperties plotProperties) {
        if (plotProperties != null) {
            AttributesPlotPropertiesPanel plotPropertiesPanel = getEditDialog()
                    .getAttributesPlotPropertiesTab().getPropertiesPanel();

            plotPropertiesPanel.setViewType(plotProperties.getViewType());
            plotPropertiesPanel.setAxisChoice(plotProperties.getAxisChoice());

            if (plotProperties.getBar() != null) {
                plotPropertiesPanel.setBarFillColor(plotProperties.getBar()
                        .getFillColor());
                plotPropertiesPanel.setBarFillingMethod(plotProperties.getBar()
                        .getFillingMethod());
                plotPropertiesPanel.setBarFillStyle(plotProperties.getBar()
                        .getFillStyle());
                plotPropertiesPanel.setBarWidth(plotProperties.getBar()
                        .getWidth());
            }
            if (plotProperties.getCurve() != null) {
                plotPropertiesPanel.setCurveColor(plotProperties.getCurve()
                        .getColor());
                plotPropertiesPanel.setCurveLineStyle(plotProperties.getCurve()
                        .getLineStyle());
                plotPropertiesPanel.setCurveWidth(plotProperties.getCurve()
                        .getWidth());
            }
            if (plotProperties.getMarker() != null) {
                plotPropertiesPanel.setMarkerColor(plotProperties.getMarker()
                        .getColor());
                plotPropertiesPanel.setMarkerSize(plotProperties.getMarker()
                        .getSize());
                plotPropertiesPanel.setMarkerStyle(plotProperties.getMarker()
                        .getStyle());
            }
            if (plotProperties.getTransform() != null) {
                plotPropertiesPanel.setTransformA0(plotProperties
                        .getTransform().getA0());
                plotPropertiesPanel.setTransformA1(plotProperties
                        .getTransform().getA1());
                plotPropertiesPanel.setTransformA2(plotProperties
                        .getTransform().getA2());
            }
            plotPropertiesPanel.setHidden(plotProperties.isHidden());
        }
    }

    public void validateEdition() {
        getEditDialog().setVisible(false);
        if (editingViewConfiguration != null) {
            if (viewConfiguration == null) {
                viewConfiguration = editingViewConfiguration;
            }
            else {
                viewConfiguration.setData(editingViewConfiguration.getData());
                viewConfiguration.setAttributes(editingViewConfiguration
                        .getAttributes());
                viewConfiguration.setExpressions(editingViewConfiguration
                        .getExpressions());
                viewConfiguration.setPath(editingViewConfiguration.getPath());
                viewConfiguration.setModified(editingViewConfiguration
                        .isModified());
            }
        }
        editingViewConfiguration = null;
        refreshMainUI();
        prepareGraphPanel();
        cancelEdition();
    }

    public VCVariationDialog getVariationDialog() {
        if (variationDialog == null) {
            variationDialog = new VCVariationDialog(this);
        }
        return variationDialog;
    }

    public VCVariationEditCopyDialog getVariationEditCopyDialog() {
        if (variationEditCopyDialog == null) {
            variationEditCopyDialog = new VCVariationEditCopyDialog(this);
        }
        return variationEditCopyDialog;
    }

    public synchronized JPanel getViewDisplayPanel() {
        if (viewDisplayPanel == null) {
            viewDisplayPanel = new JPanel(new GridBagLayout());
            final VCPopupMenu popupMenu = new VCPopupMenu(this);
            viewDisplayPanel.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });

            GridBagConstraints generalConstraints = new GridBagConstraints();
            generalConstraints.fill = GridBagConstraints.HORIZONTAL;
            generalConstraints.gridx = 0;
            generalConstraints.gridy = 0;
            generalConstraints.weightx = 1;
            generalConstraints.weighty = 0;
            viewDisplayPanel.add(getGeneralPanel(), generalConstraints);
            GridBagConstraints attributesConstraints = new GridBagConstraints();
            attributesConstraints.fill = GridBagConstraints.BOTH;
            attributesConstraints.gridx = 0;
            attributesConstraints.gridy = 1;
            attributesConstraints.weightx = 1;
            attributesConstraints.weighty = 1;
            viewDisplayPanel.add(getAttributesPanel(), attributesConstraints);
            GUIUtilities.setObjectBackground(viewDisplayPanel,
                    GUIUtilities.VIEW_COLOR);
        }
        return viewDisplayPanel;
    }

    public void prepareGraphPanel() {
        getAttributesPanel().getViewAttributesGraphPanel().clean();
        getAttributesPanel().getViewAttributesGraphPanel().initComponents();
    }

    @Override
    public void dateRangeBoxChanged(DateRangeBoxEvent event) {
        if ((viewConfiguration != null)
                && (viewConfiguration.getData() != null) && (event != null)
                && (event.getSource() != null)) {
            // Some changes occurred in DateRangeBox
            // --> update ViewConfigurationData
            viewConfiguration.getData().upDateDateRangeBox(event.getSource());
            viewConfiguration.setModified(true);
            ViewSelectionPanel.getInstance().repaint();
        }
    }

}