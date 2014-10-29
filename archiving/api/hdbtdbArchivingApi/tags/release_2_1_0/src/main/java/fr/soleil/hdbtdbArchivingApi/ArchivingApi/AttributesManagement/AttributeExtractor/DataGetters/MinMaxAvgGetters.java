/**
 * 
 */
package fr.soleil.hdbtdbArchivingApi.ArchivingApi.AttributesManagement.AttributeExtractor.DataGetters;

import fr.esrf.Tango.AttrDataFormat;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.AttributesManagement.AdtAptAttributes.AdtAptAttributesFactory;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.AttributesManagement.AdtAptAttributes.IAdtAptAttributes;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.AttributesManagement.AttributeExtractor.DataExtractor;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DataBaseManagement.DbConnection.ConnectionFactory;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DataBaseManagement.DbConnection.IDBConnection;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DataBaseUtils.DbUtilsFactory;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DataBaseUtils.IDbUtils;
import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.ArchivingException;

/**
 * @author AYADI
 *
 */
public class MinMaxAvgGetters extends DataExtractor {

	/**
	 * @param con
	 * @param ut
	 * @param at
	 */
	public MinMaxAvgGetters(int type) {
		super(type);
		// TODO Auto-generated constructor stub
	}
	/**
	 * <b>Description</b> Returns the lower value recorded for the given attribute
	 *
	 * @param att_name The attribute's name
	 * @return The lower scalar data for the specified attribute
	 * @throws ArchivingException
	 */
	public double getAttDataMin(String att_name) throws ArchivingException
	{
		IAdtAptAttributes att = AdtAptAttributesFactory.getInstance(arch_type);
		if(att==null)
			return 0;

		double min = 0;
		int[] tfw = att.getAtt_TFW_Data(att_name);
		int data_format = tfw[ 1 ];
		int writable = tfw[ 2 ];
		switch ( data_format )
		{  // [0 - > SCALAR] (1 - > SPECTRUM] [2 - > IMAGE]
			case AttrDataFormat._SCALAR:
				min = getAttScalarDataMin(att_name , writable);
				break;
			case AttrDataFormat._SPECTRUM: methods.makeDataException(data_format, "Scalar", "Spectrum");
			case AttrDataFormat._IMAGE: methods.makeDataException(data_format, "Scalar", "Image");
		}
		return min;
	}
	/**
	 * <b>Description : </b>  Returns the biggest value generated by the given attribute
	 *
	 * @param att_name The attribute's name
	 * @return The biggest value generated by the attribute
	 * @throws ArchivingException
	 */
	public double getAttDataMax(String att_name) throws ArchivingException
	{
		IAdtAptAttributes att = AdtAptAttributesFactory.getInstance(arch_type);
		if(att==null )
			return 0;

		double max = 0;
		int[] tfw = att.getAtt_TFW_Data(att_name);
		int data_format = tfw[ 1 ];
		int writable = tfw[ 2 ];
		switch ( data_format )
		{  // [0 - > SCALAR] (1 - > SPECTRUM] [2 - > IMAGE]
			case AttrDataFormat._SCALAR:
				max = getAttScalarDataMax(att_name , writable);
				break;
			case AttrDataFormat._SPECTRUM: methods.makeDataException(data_format, "Scalar", "Spectrum");
			case AttrDataFormat._IMAGE: methods.makeDataException(data_format, "Scalar", "Image");
		}
		return max;
	}
	/**
	 * <b>Description : </b>  Returns the mean value for the given attribute
	 *
	 * @param att_name The attribute's name
	 * @return The mean scalar data for the specified attribute
	 * @throws ArchivingException
	 */
	public double getAttDataAvg(String att_name) throws ArchivingException
	{
		IAdtAptAttributes att = AdtAptAttributesFactory.getInstance(arch_type);
		if(att==null)
			return 0;

		double avg = 0;
		int[] tfw = att.getAtt_TFW_Data(att_name);
		int data_format = tfw[ 1 ];
		int writable = tfw[ 2 ];
		switch ( data_format )
		{  // [0 - > SCALAR] (1 - > SPECTRUM] [2 - > IMAGE]
			case AttrDataFormat._SCALAR:
				avg = getAttScalarDataAvg(att_name , writable);
				break;
			case AttrDataFormat._SPECTRUM: methods.makeDataException(data_format, "Scalar", "Spectrum");
			case AttrDataFormat._IMAGE: methods.makeDataException(data_format, "Scalar", "Image");
		}
		return avg;
	}
	/*
	 * 
	 */
	public double getAttScalarDataMin(String att_name , int writable) throws ArchivingException
	{
		return methods.getAttScalarDataMinMaxAvg(att_name, writable, "MIN");
	}
	/*
	 * 
	 */
	public double getAttScalarDataMax(String att_name , int writable) throws ArchivingException
	{
		return methods.getAttScalarDataMinMaxAvg(att_name, writable, "MAX");
	}
	/*
	 * 
	 */
	public double getAttScalarDataAvg(String att_name , int writable) throws ArchivingException
	{
		return methods.getAttScalarDataMinMaxAvg(att_name, writable, "AVG");
	}

}