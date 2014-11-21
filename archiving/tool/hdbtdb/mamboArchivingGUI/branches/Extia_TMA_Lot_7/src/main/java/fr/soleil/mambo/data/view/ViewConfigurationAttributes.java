// +======================================================================
// $Source:
// /cvsroot/tango-cs/tango/tools/mambo/data/view/ViewConfigurationAttributes.java,v
// $
//
// Project: Tango Archiving Service
//
// Description: Java source code for the class ViewConfigurationAttributes.
// (Claisse Laurent) - 5 juil. 2005
//
// $Author: ounsy $
//
// $Revision: 1.20 $
//
// $Log: ViewConfigurationAttributes.java,v $
// Revision 1.20 2007/03/28 13:24:45 ounsy
// boolean and number scalars displayed on the same panel (Mantis bug 4428)
//
// Revision 1.19 2007/03/20 14:15:09 ounsy
// added the possibility to hide a dataview
//
// Revision 1.18 2007/02/01 14:21:46 pierrejoseph
// XmlHelper reorg
//
// Revision 1.17 2007/01/11 14:05:47 ounsy
// Math Expressions Management (warning ! requires atk 2.7.0 or greater)
//
// Revision 1.16 2006/11/14 09:35:55 ounsy
// color rotation bug correction
//
// Revision 1.15 2006/11/09 14:23:42 ounsy
// domain/family/member/attribute refactoring
//
// Revision 1.14 2006/11/07 14:35:17 ounsy
// Domain/Family/Member/Attribute refactoring
//
// Revision 1.13 2006/10/19 09:21:21 ounsy
// now the color rotation index of vc attributes is saved
//
// Revision 1.12 2006/09/22 09:34:41 ounsy
// refactoring du package mambo.datasources.db
//
// Revision 1.11 2006/09/05 14:03:12 ounsy
// updated for sampling compatibility
//
// Revision 1.10 2006/08/31 08:37:41 ounsy
// minor changes
//
// Revision 1.9 2006/08/29 14:19:27 ounsy
// now the view dialog is filled through a thread. Closing the dialog will kill
// this thread.
//
// Revision 1.8 2006/08/23 10:03:04 ounsy
// avoided a concurrent modification exception
//
// Revision 1.7 2006/08/07 13:03:07 ounsy
// trees and lists sort
//
// Revision 1.6 2006/07/18 10:27:49 ounsy
// Less time consuming by setting tree expanding on demand only +
// getImageAttributes
//
// Revision 1.5 2006/04/05 13:47:49 ounsy
// get String, state and boolean scalar attributes
//
// Revision 1.4 2006/03/10 12:03:25 ounsy
// state and string support
//
// Revision 1.3 2006/02/01 14:11:19 ounsy
// spectrum management
//
// Revision 1.2 2005/11/29 18:27:07 chinkumo
// no message
//
// Revision 1.1.2.3 2005/09/15 10:30:05 chinkumo
// Third commit !
//
// Revision 1.1.2.2 2005/09/14 15:41:32 chinkumo
// Second commit !
//
//
// copyleft : Synchrotron SOLEIL
// L'Orme des Merisiers
// Saint-Aubin - BP 48
// 91192 GIF-sur-YVETTE CEDEX
//
// -======================================================================
package fr.soleil.mambo.data.view;

import java.awt.Color;
import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeMap;

import org.nfunk.jep.JEP;

import chart.temp.chart.JLDataView;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;
import fr.soleil.comete.dao.DAOFactoryManager;
import fr.soleil.comete.util.DataArray;
import fr.soleil.comete.util.Point;
import fr.soleil.comete.widget.ChartViewer;
import fr.soleil.comete.widget.IChartViewer;
import fr.soleil.commonarchivingapi.ArchivingTools.Tools.DbData;
import fr.soleil.commonarchivingapi.ArchivingTools.Tools.NullableTimedData;
import fr.soleil.commonarchivingapi.ArchivingTools.Tools.TangoStateTranslation;
import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.SamplingType;
import fr.soleil.mambo.bean.manager.ViewConfigurationBeanManager;
import fr.soleil.mambo.containers.view.ViewStringStateBooleanScalarPanel;
import fr.soleil.mambo.containers.view.dialogs.AttributesPlotPropertiesPanel;
import fr.soleil.mambo.data.view.plot.Bar;
import fr.soleil.mambo.data.view.plot.Curve;
import fr.soleil.mambo.data.view.plot.Interpolation;
import fr.soleil.mambo.data.view.plot.Marker;
import fr.soleil.mambo.data.view.plot.MathPlot;
import fr.soleil.mambo.data.view.plot.Polynomial2OrderTransform;
import fr.soleil.mambo.data.view.plot.Smoothing;
import fr.soleil.mambo.datasources.db.extracting.ExtractingManagerFactory;
import fr.soleil.mambo.datasources.db.extracting.IExtractingManager;
import fr.soleil.mambo.options.Options;
import fr.soleil.mambo.tools.GUIUtilities;
import fr.soleil.mambo.tools.xmlhelpers.XMLLine;

public class ViewConfigurationAttributes {

    private TreeMap<String, ViewConfigurationAttribute> attributes;
    private int colorIndex = 0;
    public final static String COLOR_INDEX_XML_TAG = "MamboVCAttributesColorIndex";
    public final static String COLOR_INDEX_XML_ATTRIBUTE = "VALUE";
    private ViewConfiguration viewConfiguration;
    private ArrayList<ExpressionAttribute> notNumberExpression = new ArrayList<ExpressionAttribute>();

    @Override
    public String toString() {
        String ret = "";

        XMLLine colorIndexEmptyLine = new XMLLine(COLOR_INDEX_XML_TAG, XMLLine.EMPTY_TAG_CATEGORY);
        colorIndexEmptyLine.setAttribute(COLOR_INDEX_XML_ATTRIBUTE, Integer.toString(colorIndex));
        ret += colorIndexEmptyLine.toString();

        if (attributes != null) {
            Set<String> keySet = attributes.keySet();
            Iterator<String> keyIterator = keySet.iterator();
            int i = 0;
            while (keyIterator.hasNext()) {
                String nextKey = keyIterator.next();
                ViewConfigurationAttribute nextValue = attributes.get(nextKey);

                ret += nextValue.toString();
                if (i < attributes.size() - 1) {
                    ret += GUIUtilities.CRLF;
                }

                i++;
            }
        }

        return ret;
    }

    public boolean equals(ViewConfigurationAttributes this2) {
        return this.attributes.size() == this2.attributes.size();
    }

    public ViewConfigurationAttributes() {
        this.attributes = new TreeMap<String, ViewConfigurationAttribute>(Collator.getInstance());
    }

    public void addAttribute(ViewConfigurationAttribute _attribute) {
        String completeName = _attribute.getCompleteName();

        attributes.put(completeName, _attribute);
    }

    public ViewConfigurationAttribute getAttribute(String completeName) {
        return attributes.get(completeName);
    }

    /**
     * @return Returns the attributes.
     */
    public TreeMap<String, ViewConfigurationAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns the spectrum attributes
     * 
     * @param historic to know in which database you want to check if the attributes are spectrum or
     *            not.
     * @return The spectrum attributes
     */
    public TreeMap<String, ViewConfigurationAttribute> getSpectrumAttributes(boolean historic) {
        TreeMap<String, ViewConfigurationAttribute> spectrums = new TreeMap<String, ViewConfigurationAttribute>(
                Collator.getInstance());
        if (attributes != null) {
            Set<String> keySet = attributes.keySet();
            Iterator<String> keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                String nextKey = keyIterator.next();
                ViewConfigurationAttribute nextValue = attributes.get(nextKey);
                if (nextValue.isSpectrum(historic)) {
                    spectrums.put(nextKey, nextValue);
                }
            }
        }
        return spectrums;
    }

    /**
     * Returns the string and state scalar attributes
     * 
     * @param historic to know in which database you want to check the attributes
     * @return The string scalar attributes
     */
    public TreeMap<String, ViewConfigurationAttribute> getStringStateScalarAttributes(
            boolean historic) {
        TreeMap<String, ViewConfigurationAttribute> strings = new TreeMap<String, ViewConfigurationAttribute>(
                Collator.getInstance());
        if (attributes != null) {
            Set<String> keySet = attributes.keySet();
            Iterator<String> keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                String nextKey = keyIterator.next();
                ViewConfigurationAttribute nextValue = attributes.get(nextKey);
                if (nextValue.isScalar(historic)) {
                    if (nextValue.getDataType(historic) == TangoConst.Tango_DEV_STRING
                            || nextValue.getDataType(historic) == TangoConst.Tango_DEV_STATE) {
                        strings.put(nextKey, nextValue);
                    }
                }
            }
        }
        return strings;
    }

    /**
     * Returns the image attributes
     * 
     * @param historic to know in which database you want to check the attributes
     * @return The image attributes
     */
    public TreeMap<String, ViewConfigurationAttribute> getImageAttributes(boolean historic) {
        TreeMap<String, ViewConfigurationAttribute> images = new TreeMap<String, ViewConfigurationAttribute>(
                Collator.getInstance());
        if (attributes != null) {
            Set<String> keySet = attributes.keySet();
            Iterator<String> keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                String nextKey = keyIterator.next();
                ViewConfigurationAttribute nextValue = attributes.get(nextKey);
                if (nextValue.isImage(historic)) {
                    images.put(nextKey, nextValue);
                }
            }
        }
        return images;
    }

    /**
     * @param attributes The attributes to set.
     */
    public void setAttributes(TreeMap<String, ViewConfigurationAttribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * @param chart
     * @param historic
     * @param samplingType
     * @param string2
     * @param
     * @return 29 ao�t 2005
     * @throws ParseException
     * @throws DevFailed
     */
    public ChartViewer setChartAttributes(ChartViewer chart, String startDate, String endDate,
            boolean historic, SamplingType samplingType) throws DevFailed, ParseException {
        notNumberExpression.clear();
        IExtractingManager extractingManager = ExtractingManagerFactory.getCurrentImpl();

        if (extractingManager.isCanceled())
            return chart;
        if (attributes != null) {
            Set<String> keySet = attributes.keySet();
            Iterator<String> keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                if (extractingManager.isCanceled())
                    return chart;
                String nextKey = keyIterator.next();
                ViewConfigurationAttribute nextValue = attributes.get(nextKey);
                if (nextValue.isScalar(historic)) {
                    switch (nextValue.getDataType(historic)) {
                        case TangoConst.Tango_DEV_BOOLEAN:
                        case TangoConst.Tango_DEV_CHAR:
                        case TangoConst.Tango_DEV_UCHAR:
                        case TangoConst.Tango_DEV_LONG:
                        case TangoConst.Tango_DEV_ULONG:
                        case TangoConst.Tango_DEV_SHORT:
                        case TangoConst.Tango_DEV_USHORT:
                        case TangoConst.Tango_DEV_FLOAT:
                        case TangoConst.Tango_DEV_DOUBLE:
                            nextValue.addToChart(chart, startDate, endDate, historic, samplingType);
                            break;
                    }
                }
            }
            if (viewConfiguration != null) {
                TreeMap<String, ExpressionAttribute> expressions = viewConfiguration
                        .getExpressions();

                keySet = expressions.keySet();
                keyIterator = keySet.iterator();

                while (keyIterator.hasNext()) {
                    String key = keyIterator.next();
                    ExpressionAttribute attrExpression = expressions.get(key);
                    String[] variables = attrExpression.getVariables();
                    if (variables != null) {
                        boolean allVariablesAreNumber = true;
                        for (int i = 0; i < variables.length; i++) {
                            String attributName = "";
                            if (variables[i].contains("/"
                                    + ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY)) {
                                int index = variables[i].indexOf("/"
                                        + ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY);
                                attributName = variables[i].substring(0, index);
                            }
                            if (variables[i].contains("/"
                                    + ViewConfigurationAttribute.READ_DATA_VIEW_KEY)) {
                                int index = variables[i].indexOf("/"
                                        + ViewConfigurationAttribute.READ_DATA_VIEW_KEY);
                                attributName = variables[i].substring(0, index);
                            }
                            ViewConfigurationAttribute attr = attributes.get(attributName);
                            if (attr != null) {
                                int attrType = attr.getDataType(historic);
                                if (attrType != TangoConst.Tango_DEV_BOOLEAN
                                        && attrType != TangoConst.Tango_DEV_CHAR
                                        && attrType != TangoConst.Tango_DEV_UCHAR
                                        && attrType != TangoConst.Tango_DEV_LONG
                                        && attrType != TangoConst.Tango_DEV_ULONG
                                        && attrType != TangoConst.Tango_DEV_SHORT
                                        && attrType != TangoConst.Tango_DEV_USHORT
                                        && attrType != TangoConst.Tango_DEV_FLOAT
                                        && attrType != TangoConst.Tango_DEV_DOUBLE) {
                                    allVariablesAreNumber = false;
                                    break;
                                }
                            }
                        } // end for (int i = 0; i < variables.length; i++)

                        if (allVariablesAreNumber) {
                            int axis = -1;
                            switch (attrExpression.getProperties().getAxisChoice()) {
                                case ViewConfigurationAttributePlotProperties.AXIS_CHOICE_Y1:
                                    axis = IChartViewer.Y1;
                                    break;
                                case ViewConfigurationAttributePlotProperties.AXIS_CHOICE_Y2:
                                    axis = IChartViewer.Y2;
                                    break;
                                case ViewConfigurationAttributePlotProperties.AXIS_CHOICE_X:
                                    axis = IChartViewer.X;
                                    break;
                            }
                            attrExpression.prepareView(chart);
                            chart.getComponent().addExpression(attrExpression.getId(),
                                    attrExpression.getName(), attrExpression.getExpression(), axis,
                                    attrExpression.getVariables(), attrExpression.isX());
                        }
                        else {
                            notNumberExpression.add(attrExpression);
                        }
                    }
                }// end while
            }// end if (viewConfiguration != null)
        }
        return chart;
    }

    public void setChartAttributeExpressionOfString(
            ViewStringStateBooleanScalarPanel stringAndStatePanel, boolean historic,
            ChartViewer chart) {

        for (int i = 0; i < notNumberExpression.size(); i++) {
            ExpressionAttribute attributeExpression = notNumberExpression.get(i);
            String[] variablesNames = attributeExpression.getVariables();
            DbData[] variablesDatas = new DbData[variablesNames.length];
            if (variablesNames != null) {
                for (int j = 0; j < variablesNames.length; j++) {
                    String attributName = "";
                    boolean isRead = true;
                    if (variablesNames[j].contains("/"
                            + ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY)) {
                        int index = variablesNames[j].indexOf("/"
                                + ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY);
                        attributName = variablesNames[j].substring(0, index);
                        isRead = false;
                    }
                    if (variablesNames[j].contains("/"
                            + ViewConfigurationAttribute.READ_DATA_VIEW_KEY)) {
                        int index = variablesNames[j].indexOf("/"
                                + ViewConfigurationAttribute.READ_DATA_VIEW_KEY);
                        attributName = variablesNames[j].substring(0, index);
                        isRead = true;
                    }
                    ViewConfigurationAttribute attr = attributes.get(attributName);
                    if (attr != null) {

                        switch (attr.getDataType(historic)) {
                            case TangoConst.Tango_DEV_BOOLEAN:
                            case TangoConst.Tango_DEV_CHAR:
                            case TangoConst.Tango_DEV_UCHAR:
                            case TangoConst.Tango_DEV_LONG:
                            case TangoConst.Tango_DEV_ULONG:
                            case TangoConst.Tango_DEV_SHORT:
                            case TangoConst.Tango_DEV_USHORT:
                            case TangoConst.Tango_DEV_FLOAT:
                            case TangoConst.Tango_DEV_DOUBLE:
                                if (chart != null) {
                                    List<DataArray> chartData = chart.getData();
                                    if (chartData != null) {
                                        Iterator<DataArray> it = chartData.iterator();
                                        while (it.hasNext()) {
                                            DataArray dataAttribute = it.next();
                                            if (dataAttribute.getId().equals(variablesNames[j])) {
                                                ListIterator<Point> points = dataAttribute
                                                        .listIterator();
                                                NullableTimedData[] timedDatas = new NullableTimedData[dataAttribute
                                                        .size()];
                                                int index = 0;
                                                while (points.hasNext()) {
                                                    NullableTimedData timedData = new NullableTimedData();
                                                    Point point = points.next();
                                                    Object[] obj = new Object[2];
                                                    timedData.time = (Long) point.x;
                                                    obj[0] = point.y;
                                                    obj[1] = null;
                                                    timedData.value = obj;
                                                    timedDatas[index] = timedData;
                                                    index++;
                                                }
                                                DbData dat = new DbData(attributName);
                                                dat.setData_timed(timedDatas);
                                                variablesDatas[j] = dat;
                                                break;
                                            }
                                        }
                                        chartData = null;
                                        break;
                                    }
                                }

                            case TangoConst.Tango_DEV_STATE:
                                DbData data = stringAndStatePanel.getDataAttribute(attributName,
                                        isRead);
                                if (data != null) {
                                    NullableTimedData[] timedDatas = new NullableTimedData[data
                                            .getData_timed().length];
                                    for (int k = 0; k < data.getData_timed().length; k++) {
                                        NullableTimedData timedData = new NullableTimedData();
                                        if (data.getData_timed()[k] != null
                                                && data.getData_timed()[k].value[0] != null) {
                                            String state = TangoStateTranslation
                                                    .getStrStateValue((Integer) data
                                                            .getData_timed()[k].value[0]);
                                            Object[] obj = new Object[2];
                                            obj[0] = state;
                                            obj[1] = null;
                                            timedData.value = obj;
                                            timedData.time = data.getData_timed()[k].time;
                                            timedDatas[k] = timedData;
                                        }
                                    }
                                    DbData dat = new DbData(attributName);
                                    dat.setData_timed(timedDatas);
                                    variablesDatas[j] = dat;
                                    break;
                                }

                            case TangoConst.Tango_DEV_STRING:
                                variablesDatas[j] = stringAndStatePanel.getDataAttribute(
                                        attributName, isRead);
                                break;
                            default:
                                break;
                        }
                    }
                }// end for (int j = 0; j < variables.length; j++)

                if (chart != null) {
                    int timePrecision = chart.getComponent().getTimePrecision();
                    // calculating curve data
                    DataArray expressionDataArray = constructCurveForStringExpression(
                            notNumberExpression.get(i), variablesDatas, timePrecision);

                    // add expression to chart
                    if (expressionDataArray != null) {
                        String nameViewSelected = ViewConfigurationBeanManager.getInstance()
                                .getSelectedConfiguration().getDisplayableTitle();
                        String lastUpdate = Long.toString(ViewConfigurationBeanManager
                                .getInstance().getSelectedConfiguration().getData()
                                .getLastUpdateDate().getTime());
                        String idViewSelected = nameViewSelected + " - " + lastUpdate;
                        MamboViewDAOFactory daoFactory = (MamboViewDAOFactory) DAOFactoryManager
                                .getFactory(MamboViewDAOFactory.class.getName());
                        ViewScalarDAO dao = daoFactory.getDAOScalar(idViewSelected);
                        if (dao != null) {
                            notNumberExpression.get(i).prepareView(chart);
                            dao.addData(expressionDataArray);
                        }
                        expressionDataArray = null;
                    }
                }
            }
            variablesDatas = null;
        }// end int i = 0; i < notNumberExpression.size(); i++)
        notNumberExpression.clear();
    }

    private DataArray constructCurveForStringExpression(ExpressionAttribute expressionAttribute,
            DbData[] datas, int timePrecision) {

        if (datas == null) {
            return null;
        }
        JEP parser = new JEP();
        parser.addStandardConstants();
        parser.addStandardFunctions();
        DataArray result = new DataArray();
        result.setId(expressionAttribute.getId());
        Integer[] currentIndex = new Integer[datas.length];

        boolean hasNextValue = true;
        // init index
        for (int i = 0; i < currentIndex.length; i++) {
            currentIndex[i] = 0;
            if (datas[i] != null) {
                if (datas[i].getData_timed() != null) {
                    if (datas[i].getData_timed().length == 0) {
                        hasNextValue = false;
                    }
                }
            }
        }

        while (hasNextValue) {
            double minTime = Double.MAX_VALUE;
            boolean allIndexAtEnd = true;
            for (int i = 0; i < datas.length; i++) {
                if (datas[i] != null) {
                    if (datas[i].getData_timed() != null) {
                        if (datas[i].getData_timed().length > (currentIndex[i] + 1)) {
                            allIndexAtEnd = false;
                        }
                        // Find min time value
                        if (datas[i].getData_timed()[currentIndex[i]] != null) {
                            if (minTime > datas[i].getData_timed()[currentIndex[i]].time) {
                                minTime = datas[i].getData_timed()[currentIndex[i]].time;
                            }
                        }
                    }
                }
            }
            if (minTime == Double.MAX_VALUE || allIndexAtEnd) {
                hasNextValue = false;
            }

            for (int i = 0; i < datas.length; i++) {
                if (datas[i] != null) {
                    if (datas[i].getData_timed()[currentIndex[i]] != null) {
                        if ((datas[i].getData_timed()[currentIndex[i]].time >= (minTime - timePrecision))
                                && (datas[i].getData_timed()[currentIndex[i]].time <= (minTime + timePrecision))) {
                            parser.removeVariable("x" + (i + 1));
                            parser.addVariable("x" + (i + 1),
                                    datas[i].getData_timed()[currentIndex[i]].value[0]);
                            if ((currentIndex[i] + 1) < datas[i].getData_timed().length) {
                                currentIndex[i] = currentIndex[i] + 1;
                            }
                        }
                    }
                    else {
                        // if no data at current point
                        if ((currentIndex[i] + 1) < datas[i].getData_timed().length) {
                            currentIndex[i] = currentIndex[i] + 1;
                        }
                    }
                }
            }// end for (int i = 0; i < datas.length; i++)
            parser.parseExpression(expressionAttribute.getExpression());
            double value = parser.getValue();
            if (Double.isNaN(value)) {
                value = JLDataView.NAN_FOR_NULL;
            }
            result.add(minTime, value);
        }// end while (hasNextValue)
        return result;
    }

    /**
     * @param attrs 1 sept. 2005
     */
    public void removeAttributesNotInList(TreeMap<String, ?> attrs) {
        Set<String> keySet = this.attributes.keySet();
        ArrayList<String> toRemove = new ArrayList<String>();
        Iterator<String> keyIterator = keySet.iterator();
        while (keyIterator.hasNext()) {
            String next = keyIterator.next();
            if (!attrs.containsKey(next)) {
                toRemove.add(next);
            }
        }
        for (int i = 0; i < toRemove.size(); i++) {
            attributes.remove(toRemove.get(i));
        }
        toRemove.clear();
        toRemove = null;
    }

    /**
     * @param attrs 1 sept. 2005
     */
    public void addAttributes(TreeMap<String, ViewConfigurationAttribute> attrs) {
        Set<String> keySet = attrs.keySet();
        Iterator<String> keyIterator = keySet.iterator();
        while (keyIterator.hasNext()) {
            String nextKey = keyIterator.next();
            ViewConfigurationAttribute nextValue = attrs.get(nextKey);

            boolean needsColor = false;
            if (!this.attributes.containsKey(nextKey)) {
                this.attributes.put(nextKey, nextValue);

                needsColor = true;
                ViewConfigurationAttributeProperties currentProperties = new ViewConfigurationAttributeProperties();
                nextValue.setProperties(currentProperties);
            }
            else {
                ViewConfigurationAttributePlotProperties plotProperties = nextValue.getProperties()
                        .getPlotProperties();
                if (plotProperties.getCurve() == null) {
                    needsColor = true;
                }
            }
            if (needsColor) {
                int viewType = 0;
                int axisChoice = 0;
                int spectrumViewType = Options.getInstance().getVcOptions().getSpectrumViewType();

                Color color = AttributesPlotPropertiesPanel.getColorFor(colorIndex);
                colorIndex = AttributesPlotPropertiesPanel.getDefaultNextColorIndex(colorIndex);

                Bar bar = AttributesPlotPropertiesPanel.getDefaultBar(color);
                Curve curve = AttributesPlotPropertiesPanel.getDefaultCurve(color, nextValue
                        .getCompleteName());
                Marker marker = AttributesPlotPropertiesPanel.getDefaultMarker(color);
                Polynomial2OrderTransform transform = AttributesPlotPropertiesPanel
                        .getDefaultTransform();
                Interpolation interpolation = AttributesPlotPropertiesPanel
                        .getDefaultInterpolation();
                Smoothing smoothing = AttributesPlotPropertiesPanel.getDefaultSmoothing();
                MathPlot math = AttributesPlotPropertiesPanel.getDefaultMath();
                boolean hidden = false;
                ViewConfigurationAttributeProperties currentProperties = nextValue.getProperties();
                ViewConfigurationAttributePlotProperties currentPlotProperties = new ViewConfigurationAttributePlotProperties(
                        viewType, axisChoice, bar, curve, marker, transform, interpolation,
                        smoothing, math, spectrumViewType, hidden);
                currentProperties.setPlotProperties(currentPlotProperties);
                nextValue.setProperties(currentProperties);
                bar = null;
                curve = null;
                marker = null;
                transform = null;
                interpolation = null;
                smoothing = null;
                math = null;
                currentProperties = null;
                currentPlotProperties = null;
            }
        }
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public ViewConfigurationAttributes cloneAttrs() {
        ViewConfigurationAttributes ret = new ViewConfigurationAttributes();

        ret.setAttributes((TreeMap<String, ViewConfigurationAttribute>) this.getAttributes()
                .clone());
        ret.setColorIndex(colorIndex);

        return ret;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public void setViewConfiguration(ViewConfiguration viewConfiguration) {
        this.viewConfiguration = viewConfiguration;
    }

}