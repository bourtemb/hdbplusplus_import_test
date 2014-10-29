//Source file: D:\\eclipse\\eclipse\\workspace\\DevArchivage\\javaapi\\fr\\soleil\\TangoArchiving\\ArchivingApi\\DbFactories\\db\\oracle\\OracleSupportDataBaseApiConcreteFactory.java

package fr.soleil.hdbtdbArchivingApi.ArchivingApi.DbFactories.db.oracle;

import fr.esrf.Tango.ErrSeverity;
import java.sql.SQLException;
import fr.soleil.commonarchivingapi.ArchivingTools.Tools.GlobalConst;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DbConnection.IDataBaseConnection;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DbFactories.SupportDataBaseApiAbstractFactory;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DbImpl.db.oracle.OracleExtractorDataBaseApiImpl;
import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DbImpl.db.oracle.OracleRecorderDataBaseApiImpl;
import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.ArchivingException;

public class OracleSupportDataBaseApiConcreteFactory extends SupportDataBaseApiAbstractFactory 
{

	/**
	 * @param dataBaseApiTools
	 * @roseuid 45C9BD510276
	 */
	public OracleSupportDataBaseApiConcreteFactory(IDataBaseConnection dbConn) 
	{
		super(dbConn);
	}

	/**
	 * @roseuid 45ED86B6034F
	 */
	protected void createIExtractorDataBaseApi() 
	{
		m_IExtractorInstance = new OracleExtractorDataBaseApiImpl(this);
	}

	/**
	 * @roseuid 45ED86B60350
	 */
	protected void createIRecorderDataBaseApi() 
	{
		m_IRecorderInstance = new OracleRecorderDataBaseApiImpl(this);
	}

	/**
	 * @param e
	 * @param methodeName
	 * @throws fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.ArchivingException
	 * @roseuid 45ED86B6035F
	 */
	public void sqlExceptionTreatment(SQLException e, String methodeName) throws ArchivingException 
	{
		String message = "";
		if ( e.getMessage().equalsIgnoreCase(GlobalConst.COMM_FAILURE_ORACLE))
			message = GlobalConst.ARCHIVING_ERROR_PREFIX + " : " + GlobalConst.ADB_CONNECTION_FAILURE;
		else
			message = GlobalConst.ARCHIVING_ERROR_PREFIX + " : " + GlobalConst.STATEMENT_FAILURE;

		String reason = GlobalConst.QUERY_FAILURE;
		String desc = "Failed while executing " + methodeName + " method...";
		throw new ArchivingException(message , reason , ErrSeverity.WARN , desc , this.getClass().getName() , e);

	}
}