/*
 * Synchrotron Soleil File : BasicExtractingManager.java Project : mambo
 * Description : Author : CLAISSE Original : 22 sept. 2006 Revision: Author:
 * Date: State: Log: BasicExtractingManager.java,v
 */
/*
 * Created on 22 sept. 2006 To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.mambo.datasources.db.extracting;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;
import fr.soleil.commonarchivingapi.ArchivingTools.Tools.DbData;
import fr.soleil.commonarchivingapi.ArchivingTools.Tools.NullableTimedData;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.ConfigConst;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DataBaseManager;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.ImageData;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DataBaseUtils.DbUtils;
import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.ArchivingException;
import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.SamplingType;
import fr.soleil.mambo.data.view.ViewConfigurationAttribute;
import fr.soleil.mambo.datasources.db.DbConnectionManager;
import fr.soleil.mambo.tools.GUIUtilities;
import fr.soleil.mambo.tools.Messages;

public class BasicExtractingManager extends DbConnectionManager implements
        IExtractingManager {

    private boolean                          historic;
    private boolean                          showRead        = true;
    private boolean                          showWrite       = true;
    private boolean                          canceled        = false;

    static final java.util.GregorianCalendar calendar        = new java.util.GregorianCalendar();
    static final java.text.SimpleDateFormat  genFormatEU     = new java.text.SimpleDateFormat(
                                                                     "dd-MM-yyyy HH:mm:ss");
    static final java.text.SimpleDateFormat  genFormatUS     = new java.text.SimpleDateFormat(
                                                                     "yyyy-MM-dd HH:mm:ss");
    static final java.text.SimpleDateFormat  genFormatUSName = new java.text.SimpleDateFormat(
                                                                     "yyyy-MM-dd HH:mm:ss.SSS");
    private static final String              READ            = "READ";
    private static final String              WRITE           = "WRITE";

    BasicExtractingManager() {
        super();
    }

    @Override
    public Double[] getMinAndMax(String[] param, boolean _historic) {
        Double[] minmax = new Double[2];
        try {
            DataBaseManager db = getDataBaseApi(_historic);

            minmax[0] = new Double(db.getExtractor()
                    .getMinMaxAvgGettersBetweenDates()
                    .getAttDataMinBetweenDates(param));
            minmax[1] = new Double(db.getExtractor()
                    .getMinMaxAvgGettersBetweenDates()
                    .getAttDataMaxBetweenDates(param));
        } catch (Exception e) {
            minmax[0] = new Double(Double.NaN);
            minmax[1] = new Double(Double.NaN);
        }
        return minmax;
    }

    @Override
    public Hashtable<String, Object> retrieveDataHash(String[] param,
            boolean _historic, SamplingType samplingType) {
        if (canceled) {
            return null;
        }
        Hashtable<String, Object> ret = new Hashtable<String, Object>();
        JLDataView readView = new JLDataView();
        JLDataView writeView = new JLDataView();
        JLDataView[] readSpectrumView = new JLDataView[0];
        JLDataView[] writeSpectrumView = new JLDataView[0];
        historic = _historic;

        // WARNING CLA 28/08/06
        if (canceled) {
            return null;
        }
        DbData data = getDbDataWithSampling(historic, param, samplingType);
        if (canceled) {
            return null;
        }

        if (data == null) {
            return null;
        }
        DbData[] splitedData = splitDbData(data);
        int dataType = data.getData_type();
        int dataFormat = data.getData_format();
        int dataWritable = data.getWritable();

        switch (dataFormat) {
            case AttrDataFormat._SCALAR:
                switch (dataType) {
                    case TangoConst.Tango_DEV_BOOLEAN:
                    case TangoConst.Tango_DEV_DOUBLE:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                        readView = getScalarView(splitedData, dataWritable,
                                dataType, 0);
                        writeView = getScalarView(splitedData, dataWritable,
                                dataType, 1);
                        if (showRead) {
                            switch (dataWritable) {
                                case AttrWriteType._READ:
                                case AttrWriteType._READ_WITH_WRITE:
                                case AttrWriteType._READ_WRITE:
                                    ret
                                            .put(
                                                    ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                                                    readView);
                            }
                        }
                        if (showWrite) {
                            switch (dataWritable) {
                                case AttrWriteType._WRITE:
                                case AttrWriteType._READ_WITH_WRITE:
                                case AttrWriteType._READ_WRITE:
                                    ret
                                            .put(
                                                    ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                                                    writeView);
                            }
                        }
                        break;
                    case TangoConst.Tango_DEV_STRING:
                    case TangoConst.Tango_DEV_STATE:
                        if (splitedData[0] != null) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                                            splitedData[0]);
                        }
                        if (splitedData[1] != null) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                                            splitedData[1]);
                        }
                        break;
                    default:
                        ret = null;
                }
                break;
            case AttrDataFormat._SPECTRUM:
                switch (dataType) {
                    case TangoConst.Tango_DEV_DOUBLE:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                        Hashtable<String, JLDataView[]> spectrumViews = getSpectrumViews(
                                splitedData, dataWritable, dataType);
                        readSpectrumView = spectrumViews.get(READ);
                        writeSpectrumView = spectrumViews.get(WRITE);
                        if (readSpectrumView != null
                                && readSpectrumView.length > 0) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                                            readSpectrumView);
                        }
                        if (writeSpectrumView != null
                                && writeSpectrumView.length > 0) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                                            writeSpectrumView);
                        }
                        break;
                    case TangoConst.Tango_DEV_BOOLEAN:
                    case TangoConst.Tango_DEV_STRING:
                    case TangoConst.Tango_DEV_STATE:
                        if (splitedData[0] != null) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                                            splitedData[0]);
                        }
                        if (splitedData[1] != null) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                                            splitedData[1]);
                        }
                        break;
                    default:
                        ret = null;
                }
                break;
            case AttrDataFormat._IMAGE:
                if (splitedData[0] != null) {
                    ret.put(ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                            splitedData[0]);
                }
                if (splitedData[1] != null) {
                    ret.put(ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                            splitedData[1]);
                }
                break;
        }
        if (canceled) {
            return null;
        }
        return ret;
    }

    @Override
    public Vector<ImageData> retrieveImageDatas(String[] param,
            boolean _historic, SamplingType samplingType) throws DevFailed {
        try {
            DataBaseManager database = this.getDataBaseApi(_historic);
            Vector<ImageData> ret = database.getExtractor()
                    .getAttPartialImageDataBetweenDates(param, samplingType);
            return ret;
        } catch (ArchivingException e) {
            e.printStackTrace();
            GUIUtilities.throwDevFailed(e);
            return null;
        }
    }

    @Override
    public String timeToDateSGBD(long milli) throws DevFailed,
            ArchivingException {
        switch (DbUtils.getDbType(getDataBaseApi(historic).getDbConn()
                .getDbType())) {
            case ConfigConst.BD_MYSQL:
                return formatDate(milli, genFormatUS);
            default:
                return formatDate(milli, genFormatEU);
        }
    }

    private DbData getDbDataWithSampling(boolean _historic, String[] param,
            SamplingType samplingType) {
        if (canceled)
            return null;
        try {
            DataBaseManager db = getDataBaseApi(_historic);
            DbData data = db.getExtractor().getDataGettersBetweenDates()
                    .getAttDataBetweenDates(param, samplingType);
            return data;
        } catch (ArchivingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DbData[] splitDbData(DbData data) {
        DbData[] splitedData = null;
        try {
            splitedData = data.splitDbData();
        } catch (DevFailed e) {
            e.printStackTrace();
        }
        return splitedData;
    }

    /**
     * @param splitedData
     * @param dataWritable
     * @param dataType
     * @param dataFormat
     * @return
     */
    private JLDataView getScalarView(DbData[] splitedData, int dataWritable,
            int dataType, int readWriteIdx) {
        JLDataView readOrWriteView = new JLDataView();
        if (splitedData[readWriteIdx] == null || canceled) {
            return readOrWriteView;
        }
        NullableTimedData[] readOrWriteTimedAttrData = splitedData[readWriteIdx]
                .getData_timed();

        int readOrWriteSize = readOrWriteTimedAttrData.length;
        for (int i = 0; i < readOrWriteSize && !canceled; i++) {
            NullableTimedData timedAttrDataElement = readOrWriteTimedAttrData[i];
            long sec_l = timedAttrDataElement.time.longValue();
            switch (dataWritable) {
                case AttrWriteType._READ:
                case AttrWriteType._READ_WRITE:
                case AttrWriteType._READ_WITH_WRITE:
                case AttrWriteType._WRITE:
                    switch (dataType) {
                        case TangoConst.Tango_DEV_USHORT:
                        case TangoConst.Tango_DEV_SHORT:
                            if (canceled) {
                                return readOrWriteView;
                            } else if (timedAttrDataElement.value == null) {
                                readOrWriteView.add(sec_l, Double.NaN);
                            }
                            for (int j = 0; j < timedAttrDataElement.value.length
                                    && !canceled; j++) {
                                if (timedAttrDataElement.value[j] == null) {
                                    double d = JLDataView.NAN_FOR_NULL;
                                    readOrWriteView.add(sec_l, d);
                                } else {
                                    readOrWriteView
                                            .add(
                                                    sec_l,
                                                    ((Short) timedAttrDataElement.value[j])
                                                            .doubleValue());
                                }
                            }
                            break;
                        case TangoConst.Tango_DEV_DOUBLE:
                            if (canceled) {
                                return readOrWriteView;
                            } else if (timedAttrDataElement.value == null) {
                                double d = JLDataView.NAN_FOR_NULL;
                                readOrWriteView.add(sec_l, d);
                            }
                            for (int j = 0; j < timedAttrDataElement.value.length
                                    && !canceled; j++) {
                                if (timedAttrDataElement.value[j] == null) {
                                    double d = JLDataView.NAN_FOR_NULL;
                                    readOrWriteView.add(sec_l, d);
                                } else {
                                    readOrWriteView
                                            .add(
                                                    sec_l,
                                                    ((Double) timedAttrDataElement.value[j])
                                                            .doubleValue());
                                }
                            }
                            break;
                        case TangoConst.Tango_DEV_FLOAT:
                            if (canceled) {
                                return readOrWriteView;
                            } else if (timedAttrDataElement.value == null) {
                                double d = JLDataView.NAN_FOR_NULL;
                                readOrWriteView.add(sec_l, d);
                            }
                            for (int j = 0; j < timedAttrDataElement.value.length
                                    && !canceled; j++) {
                                if (timedAttrDataElement.value[j] == null) {
                                    double d = JLDataView.NAN_FOR_NULL;
                                    readOrWriteView.add(sec_l, d);
                                } else {
                                    readOrWriteView
                                            .add(
                                                    sec_l,
                                                    ((Float) timedAttrDataElement.value[j])
                                                            .doubleValue());
                                }
                            }
                            break;
                        case TangoConst.Tango_DEV_ULONG:
                        case TangoConst.Tango_DEV_LONG:
                            if (canceled) {
                                return readOrWriteView;
                            } else if (timedAttrDataElement.value == null) {
                                double d = JLDataView.NAN_FOR_NULL;
                                readOrWriteView.add(sec_l, d);
                            }
                            for (int j = 0; j < timedAttrDataElement.value.length
                                    && !canceled; j++) {
                                if (timedAttrDataElement.value[j] == null) {
                                    double d = JLDataView.NAN_FOR_NULL;
                                    readOrWriteView.add(sec_l, d);
                                } else {
                                    readOrWriteView
                                            .add(
                                                    sec_l,
                                                    ((Integer) timedAttrDataElement.value[j])
                                                            .doubleValue());
                                }
                            }
                            break;
                        case TangoConst.Tango_DEV_BOOLEAN:
                            if (canceled) {
                                return readOrWriteView;
                            } else if (timedAttrDataElement.value == null) {
                                double d = JLDataView.NAN_FOR_NULL;
                                readOrWriteView.add(sec_l, d);
                            }
                            for (int j = 0; j < timedAttrDataElement.value.length
                                    && !canceled; j++) {
                                if (timedAttrDataElement.value[j] == null) {
                                    double d = JLDataView.NAN_FOR_NULL;
                                    readOrWriteView.add(sec_l, d);
                                } else {
                                    readOrWriteView
                                            .add(
                                                    sec_l,
                                                    ((Boolean) timedAttrDataElement.value[j])
                                                            .booleanValue() ? 1
                                                            : 0);
                                }
                            }
                            break;
                    }
                    break;
            } // end switch(dataWritable)
        } // end for (int i = 0; i < readSize; i++)
        return readOrWriteView;
    }

    /**
     * @param splitedData
     * @param dataWritable
     * @return
     */
    private static Hashtable<String, JLDataView[]> getSpectrumViews(
            DbData[] splitedData, int dataWritable, int dataType) {
        Hashtable<String, JLDataView[]> ret = new Hashtable<String, JLDataView[]>();
        JLDataView[] readSpectrumView = null;
        JLDataView[] writeSpectrumView = null;
        switch (dataWritable) {
            case AttrWriteType._READ_WITH_WRITE:
            case AttrWriteType._READ_WRITE:
                readSpectrumView = generateIndexSpectrumView(splitedData[0],
                        dataType, Messages.getMessage("VIEW_SPECTRUM_READ"));
                writeSpectrumView = generateIndexSpectrumView(splitedData[1],
                        dataType, Messages.getMessage("VIEW_SPECTRUM_WRITE"));
                break;
            case AttrWriteType._READ:
                readSpectrumView = generateIndexSpectrumView(splitedData[0],
                        dataType, Messages.getMessage("VIEW_SPECTRUM_READ"));
                break;
            case AttrWriteType._WRITE:
                writeSpectrumView = generateIndexSpectrumView(splitedData[1],
                        dataType, Messages.getMessage("VIEW_SPECTRUM_WRITE"));
                break;
        }
        if (readSpectrumView != null) {
            ret.put(READ, readSpectrumView);
        }
        if (writeSpectrumView != null) {
            ret.put(WRITE, writeSpectrumView);
        }
        return ret;
    }

    @Override
    public void cancel() {
        canceled = true;
        // this.getDataBaseApi(historic).cancel();
    }

    @Override
    public void allow() {
        canceled = false;
        // this.getDataBaseApi(historic).allow();
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    /**
     * @return Returns the showRead.
     */
    public boolean isShowRead() {
        return this.showRead;
    }

    @Override
    public void setShowRead(boolean showRead) {
        this.showRead = showRead;
    }

    /**
     * @return Returns the showWrite.
     */
    public boolean isShowWrite() {
        return this.showWrite;
    }

    @Override
    public void setShowWrite(boolean showWrite) {
        this.showWrite = showWrite;
    }

    @Override
    public Hashtable<String, Object> retrieveDataHash(String[] param,
            boolean _historic, SamplingType samplingType, boolean isTime)
            throws DevFailed {
        if (canceled) {
            return null;
        }
        Hashtable<String, Object> ret = new Hashtable<String, Object>();
        JLDataView readView = new JLDataView();
        JLDataView writeView = new JLDataView();
        JLDataView[] readSpectrumView = new JLDataView[0];
        JLDataView[] writeSpectrumView = new JLDataView[0];
        historic = _historic;

        // WARNING CLA 28/08/06
        if (canceled) {
            return null;
        }
        DbData data = getDbDataWithSampling(historic, param, samplingType);
        if (canceled) {
            return null;
        }

        if (data == null) {
            return null;
        }
        DbData[] splitedData = splitDbData(data);
        int dataType = data.getData_type();
        int dataFormat = data.getData_format();
        int dataWritable = data.getWritable();

        switch (dataFormat) {
            case AttrDataFormat._SCALAR:
                switch (dataType) {
                    case TangoConst.Tango_DEV_BOOLEAN:
                    case TangoConst.Tango_DEV_DOUBLE:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                        readView = getScalarView(splitedData, dataWritable,
                                dataType, 0);
                        writeView = getScalarView(splitedData, dataWritable,
                                dataType, 1);
                        if (showRead) {
                            switch (dataWritable) {
                                case AttrWriteType._READ:
                                case AttrWriteType._READ_WITH_WRITE:
                                case AttrWriteType._READ_WRITE:
                                    ret
                                            .put(
                                                    ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                                                    readView);
                            }
                        }
                        if (showWrite) {
                            switch (dataWritable) {
                                case AttrWriteType._WRITE:
                                case AttrWriteType._READ_WITH_WRITE:
                                case AttrWriteType._READ_WRITE:
                                    ret
                                            .put(
                                                    ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                                                    writeView);
                            }
                        }
                        break;
                    case TangoConst.Tango_DEV_STRING:
                    case TangoConst.Tango_DEV_STATE:
                        if (splitedData[0] != null) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                                            splitedData[0]);
                        }
                        if (splitedData[1] != null) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                                            splitedData[1]);
                        }
                        break;
                    default:
                        ret = null;
                }
                break;
            case AttrDataFormat._SPECTRUM:
                switch (dataType) {
                    case TangoConst.Tango_DEV_DOUBLE:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                        Hashtable<String, JLDataView[]> spectrumViews = null;
                        if (isTime) {
                            // time mode
                            spectrumViews = getSpectrumViewsTimeMode(
                                    splitedData, dataWritable, dataType);
                        } else {
                            // classic mode: indice
                            spectrumViews = getSpectrumViews(splitedData,
                                    dataWritable, dataType);
                        }
                        readSpectrumView = spectrumViews.get(READ);
                        writeSpectrumView = spectrumViews.get(WRITE);
                        if (readSpectrumView != null
                                && readSpectrumView.length > 0) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                                            readSpectrumView);
                        }
                        if (writeSpectrumView != null
                                && writeSpectrumView.length > 0) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                                            writeSpectrumView);
                        }
                        break;
                    case TangoConst.Tango_DEV_BOOLEAN:
                    case TangoConst.Tango_DEV_STRING:
                    case TangoConst.Tango_DEV_STATE:
                        if (splitedData[0] != null) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                                            splitedData[0]);
                        }
                        if (splitedData[1] != null) {
                            ret
                                    .put(
                                            ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                                            splitedData[1]);
                        }
                        break;
                    default:
                        ret = null;
                }
                break;
            case AttrDataFormat._IMAGE:
                if (splitedData[0] != null) {
                    ret.put(ViewConfigurationAttribute.READ_DATA_VIEW_KEY,
                            splitedData[0]);
                }
                if (splitedData[1] != null) {
                    ret.put(ViewConfigurationAttribute.WRITE_DATA_VIEW_KEY,
                            splitedData[1]);
                }
                break;
        }
        if (canceled) {
            return null;
        }
        return ret;
    }

    /**
     * @param splitedData
     * @param dataWritable
     * @return
     */
    private static Hashtable<String, JLDataView[]> getSpectrumViewsTimeMode(
            DbData[] splitedData, int dataWritable, int dataType) {
        Hashtable<String, JLDataView[]> ret = new Hashtable<String, JLDataView[]>();
        JLDataView[] readSpectrumView = null;
        JLDataView[] writeSpectrumView = null;
        switch (dataWritable) {
            case AttrWriteType._READ_WITH_WRITE:
            case AttrWriteType._READ_WRITE:
                readSpectrumView = generateTimeSpectrumView(splitedData[0],
                        dataType, Messages.getMessage("VIEW_SPECTRUM_READ"));
                writeSpectrumView = generateTimeSpectrumView(splitedData[1],
                        dataType, Messages.getMessage("VIEW_SPECTRUM_WRITE"));
                break;
            case AttrWriteType._READ:
                readSpectrumView = generateTimeSpectrumView(splitedData[0],
                        dataType, Messages.getMessage("VIEW_SPECTRUM_READ"));
                break;
            case AttrWriteType._WRITE:
                writeSpectrumView = generateTimeSpectrumView(splitedData[1],
                        dataType, Messages.getMessage("VIEW_SPECTRUM_WRITE"));
                break;
        }
        if (readSpectrumView != null) {
            ret.put(READ, readSpectrumView);
        }
        if (writeSpectrumView != null) {
            ret.put(WRITE, writeSpectrumView);
        }
        return ret;
    }

    private static JLDataView[] generateTimeSpectrumView(DbData data,
            int dataType, String viewType) {
        JLDataView[] spectrumView = null;
        if (data != null) {
            spectrumView = new JLDataView[data.getData_timed().length];
            for (int i = 0; i < data.getData_timed().length; i++) {
                spectrumView[i] = new JLDataView();
                long time = data.getData_timed()[i].time.longValue();
                spectrumView[i].setName(formatDate(time, genFormatUSName) + " "
                        + viewType);
                switch (dataType) {
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_DOUBLE:
                        for (int j = 0; j < data.getMax_x(); j++) {
                            double y = Double.NaN;
                            if (data.getData_timed()[i].value != null
                                    && j < data.getData_timed()[i].value.length
                                    && data.getData_timed()[i].value[j] != null) {
                                y = ((Number) data.getData_timed()[i].value[j])
                                        .doubleValue();
                            }
                            spectrumView[i].add(j, y);
                        }
                        break;
                    default: // nothing to do
                }
            }
        }
        return spectrumView;
    }

    private static JLDataView[] generateIndexSpectrumView(DbData data,
            int dataType, String viewType) {
        JLDataView[] spectrumView = null;
        if (data != null) {
            spectrumView = new JLDataView[data.getMax_x()];
            for (int i = 0; i < data.getMax_x(); i++) {
                spectrumView[i] = new JLDataView();
                spectrumView[i].setName((i + 1) + " " + viewType);
                switch (dataType) {
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_DOUBLE:
                        for (int j = 0; j < data.getData_timed().length; j++) {
                            long time = (long) data.getData_timed()[j].time
                                    .longValue();
                            double y = Double.NaN;
                            if (data.getData_timed()[j].value != null
                                    && i < data.getData_timed()[j].value.length
                                    && data.getData_timed()[j].value[i] != null) {
                                y = ((Number) data.getData_timed()[j].value[i])
                                        .doubleValue();
                            }
                            spectrumView[i].add(time, y);
                        }
                        break;
                    default: // nothing to do
                }
            }
        }
        return spectrumView;
    }

    private static String formatDate(long timeInMillis, SimpleDateFormat format) {
        String date = null;
        if (format != null) {
            Calendar calendar = Calendar.getInstance();
            synchronized (calendar) {
                calendar.setTimeInMillis(timeInMillis);
                date = format.format(calendar.getTime());
            }
        }
        return date;
    }

}