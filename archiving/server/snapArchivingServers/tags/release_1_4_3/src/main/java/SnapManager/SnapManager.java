//+============================================================================
// $Source: /cvsroot/tango-cs/tango/jserver/snapshoting/SnapManager/SnapManager.java,v $
//
// project :     Tango Device Server
//
// Description:	java source code for the SnapManager class and its commands.
//              This class is derived from DeviceImpl class.
//              It represents the CORBA servant obbject which
//              will be accessed from the network. All commands which
//              can be executed on the SnapManager are implemented
//              in this file.
//
// $Author: pierrejoseph $
//
// $Revision: 1.22 $
//
// $Log: SnapManager.java,v $
// Revision 1.22  2007/12/12 17:39:42  pierrejoseph
// Add comments on the SetEquipments method for the Javadoc
//
// Revision 1.21  2007/11/23 09:23:13  pierrejoseph
// Modification of set_equipements method to no more works with the SnapExtractor api
//
// Revision 1.20  2007/11/20 15:27:19  pierrejoseph
// Exception are raised by the method SetEquipments
//
// Revision 1.19  2007/11/16 10:14:21  soleilarc
// Author: XPigeon
// Mantis bug ID: 5341
// Comment :
// In the init_device method, create an instance of ISnapReader.
// Define the new method named setEquipments.
//
// Revision 1.18  2007/05/11 13:58:54  pierrejoseph
// Attribute addition : release version
//
// Revision 1.17  2007/03/02 07:54:57  ounsy
// extend DeviceImpl instead of DeviceImplWithShutdownRunnable
//
// Revision 1.16  2006/12/06 10:16:02  ounsy
// minor changes
//
// Revision 1.15  2006/11/20 09:38:54  ounsy
// minor changes
//
// Revision 1.14  2006/11/13 15:58:06  ounsy
// all java devices now inherit from UnexportOnShutdownDeviceImpl instead of from DeviceImpl
//
// Revision 1.13  2006/05/30 13:03:10  ounsy
// minor changes
//
// Revision 1.12  2006/04/21 09:05:28  ounsy
// New command "UpdateSnapComment" added
//
// Revision 1.11  2006/02/15 09:09:57  ounsy
// minor changes : uncomment to debug
//
// Revision 1.10  2005/11/29 17:34:34  chinkumo
// no message
//
// Revision 1.9.2.2  2005/11/29 16:18:25  chinkumo
// Code reformated (pogo compatible)
//
// Revision 1.9.2.1  2005/11/15 13:45:32  chinkumo
// ...
//
// Revision 1.9  2005/08/19 14:03:26  chinkumo
// no message
//
// Revision 1.8.6.1  2005/08/11 08:16:44  chinkumo
// The 'SetEquipement' command and thus functionnality was added.
//
// Revision 1.8  2005/06/28 09:10:28  chinkumo
// Changes made to improve the management of exceptions were reported here.
//
// Revision 1.7  2005/06/22 09:28:34  chinkumo
// Tango V5 regenerated.
//
// Revision 1.5  2005/06/15 14:03:29  chinkumo
// The device was regenerated in Tango V5.
//
// Revision 1.4  2005/06/14 12:11:40  chinkumo
// Branch (snapManager_1_0_1-branch_0)  and HEAD merged.
//
// Revision 1.3.4.1  2005/05/11 15:53:43  chinkumo
// The create_new_context's command comment was enhanced.
//
// Revision 1.3  2005/01/26 17:06:25  chinkumo
// Ultimate synchronization before real sharing.
//
// Revision 1.2  2004/12/06 17:30:44  chinkumo
// Renaming package 'TangoSnap' into 'TangoSnapshoting'
//
// Revision 1.1  2004/06/11 14:13:41  chinkumo
// The first team version.
//
// Revision 1.1  2004/05/10 14:01:33  chinkumo
// Archiving sources
//
//
//
// copyleft :   European Synchrotron Radiation Facility
//              BP 220, Grenoble 38043
//              FRANCE
//
//-============================================================================
//
//  		This file is generated by POGO
//	(Program Obviously used to Generate tango Object)
//
//         (c) - Software Engineering Group - ESRF
//=============================================================================


package SnapManager;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;

import SnapArchiver.SnapArchiverClass;
import SnapArchiver.grouplink.UsePluginBuilder;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoDs.Attribute;
import fr.esrf.TangoDs.DeviceClass;
import fr.esrf.TangoDs.DeviceImpl;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.TangoDs.Util;
import fr.soleil.actiongroup.collectiveaction.onattributes.UsePlugin;
import fr.soleil.commonarchivingapi.ArchivingTools.Tools.GlobalConst;
import fr.soleil.snapArchivingApi.SnapManagerApi.ISnapManager;
import fr.soleil.snapArchivingApi.SnapManagerApi.SnapManagerApi;
import fr.soleil.snapArchivingApi.SnapManagerApi.SnapManagerImpl;
import fr.soleil.snapArchivingApi.SnapshotingApi.ConfigConst;
import fr.soleil.snapArchivingApi.SnapshotingApi.persistence.SnapshotPersistenceManager;
import fr.soleil.snapArchivingApi.SnapshotingApi.persistence.SnapshotPersistenceManagerFactory;
import fr.soleil.snapArchivingApi.SnapshotingTools.Tools.SnapAttributeExtract;
import fr.soleil.snapArchivingApi.SnapshotingTools.Tools.SnapContext;
import fr.soleil.snapArchivingApi.SnapshotingTools.Tools.SnapShot;
import fr.soleil.snapArchivingApi.SnapshotingTools.Tools.SnapshotingException;


/**
 * Class Description:
 * This DServer provides the connections points and methods to the SnapShot service.
 *
 * @author	$Author: pierrejoseph $
 * @version	$Revision: 1.22 $
 */

//--------- Start of States Description ----------
/*
 *	Device States Description:
 */
//--------- End of States Description ----------


public class SnapManager extends DeviceImpl/*WithShutdownRunnable*/ implements TangoConst
{
	protected int state;
	private String m_version; 
	protected SnapshotPersistenceManager manager;
	//--------- Start of attributes data members ----------

	//--------- End of attributes data members ----------


	//--------- Start of properties data members ----------
	/**
	 * Computer identifier on wich is settled the database HDB. The identifier can be the computer name or its IP address. <br> <b>Default value : </b> localhost.
	 */
	String dbHost;
	/**
	 * Database name.<br> <b>Default value : </b> hdb
	 */
	String dbName;
	/**
	 * Database schema name.<br> <b>Default value : </b> snap
	 */
	String dbSchema;


	/**
	 * User identifier (name) used to connect the database for snapshots. <br> <b>Default value : </b> archiver
	 */
	String dbUser;
	/**
	 * Password used to connect the database for snapshots. <br> <b>Default value : </b> archiver
	 */
	String dbPassword;

//--------- End of properties data members ----------


	//	Add your own data members here
	private ISnapManager m_manager;
    /**
     * The name of the spring beans file <br><b>Default value : </b> beans.xml
     */
    String beansFileName;

	//private DataBaseAPI snapDb;
	//--------------------------------------



//=========================================================
	/**
	 * Constructor for simulated Time Device Server.
	 *
	 * @param	cl	The DeviceClass object
	 * @param	s	The Device name.
	 * @param   version The device version
	 */
//=========================================================
	SnapManager(DeviceClass cl , String s, String version) throws DevFailed
	{
		super(cl , s);
		m_version = version;
		init_device();
	}
//=========================================================
	/**
	 * Constructor for simulated Time Device Server.
	 *
	 * @param	cl	The DeviceClass object
	 * @param	s	The Device name.
	 * @param	d	Device description.
	 * @param   version The device version
	 */
//=========================================================
	SnapManager(DeviceClass cl , String s , String d, String version) throws DevFailed
	{
		super(cl , s , d);
		m_version = version;
		init_device();
	}


//=========================================================
	/**
	 * Initialize the device.
	 */
//=========================================================
	public void init_device() throws DevFailed
	{
		System.out.println("SnapManager() create " + device_name);

		//	Initialise variables to default values
		//-------------------------------------------
		get_device_property();
        try
        {
            SnapManagerApi.SnapshotingConfigure(dbUser , dbPassword);
            SnapshotPersistenceManagerFactory factory = SnapshotPersistenceManagerFactory.getInstance ( this.beansFileName );
            manager = factory.getManager ();
        }
        catch ( SnapshotingException e )
        {
            get_logger().warn(e.toString() , e);
            throw e.toTangoException();
        }

		set_state(DevState.ON);
	}

//===================================================================
	/**
	 * Read the device properties from database.
	 */
//===================================================================
	public void get_device_property() throws DevFailed
	{
		//	Initialize your default values here.
		//------------------------------------------
		dbUser = ConfigConst.default_sauser;
		dbPassword = ConfigConst.default_sapasswd;
        beansFileName = ConfigConst.default_sabeansfilename;


		//	Read device properties from database.(Automatic code generation)
		//-------------------------------------------------------------
		if ( Util._UseDb == false )
			return;
		String[] propnames = {
        	"DbHost",
    		"DbName",
    		"DbSchema",
			"DbUser",
			"DbPassword",
            "BeansFileName"
		};

		//	Call database and extract values
		//--------------------------------------------
		DbDatum[] dev_prop = get_db_device().get_property(propnames);
		SnapManagerClass ds_class = ( SnapManagerClass ) get_device_class();
        int i = -1;
        //  Extract DbHost value
        if ( dev_prop[ ++i ].is_empty() == false )
            dbHost = dev_prop[ i ].extractString();
        else
        {
            //  Try to get value from class property
            DbDatum cl_prop = ds_class.get_class_property(dev_prop[ i ].name);
            if ( cl_prop.is_empty() == false ) dbHost = cl_prop.extractString();
        }
        //  Extract DbName value
        if ( dev_prop[ ++i ].is_empty() == false )
            dbName = dev_prop[ i ].extractString();
        else
        {
            //  Try to get value from class property
            DbDatum cl_prop = ds_class.get_class_property(dev_prop[ i ].name);
            if ( cl_prop.is_empty() == false ) dbName = cl_prop.extractString();
        }
        //  Extract DbSchema value
        if ( dev_prop[ ++i ].is_empty() == false )
            dbSchema = dev_prop[ i ].extractString();
        else
        {
            //  Try to get value from class property
            DbDatum cl_prop = ds_class.get_class_property(dev_prop[ i ].name);
            if ( cl_prop.is_empty() == false ) dbSchema = cl_prop.extractString();
        }


		//	Extract DbUser value
		if ( dev_prop[ ++i ].is_empty() == false )
			dbUser = dev_prop[ i ].extractString();
		else
		{
			//	Try to get value from class property
			DbDatum cl_prop = ds_class.get_class_property(dev_prop[ i ].name);
			if ( cl_prop.is_empty() == false ) dbUser = cl_prop.extractString();
		}

		//	Extract DbPassword value
		if ( dev_prop[ ++i ].is_empty() == false )
			dbPassword = dev_prop[ i ].extractString();
		else
		{
			//	Try to get value from class property
			DbDatum cl_prop = ds_class.get_class_property(dev_prop[ i ].name);
			if ( cl_prop.is_empty() == false ) dbPassword = cl_prop.extractString();
		}

        //  Extract BeansFileName value
        if ( dev_prop[ ++i ].is_empty() == false )
            beansFileName = dev_prop[ i ].extractString();
        else
        {
            //  Try to get value from class property
            DbDatum cl_prop = ds_class.get_class_property(dev_prop[ i ].name);
            if ( cl_prop.is_empty() == false ) beansFileName = cl_prop.extractString();
        }

		//	End of Automatic code generation
		//-------------------------------------------------------------

	}
//=========================================================
	/**
	 * Method always executed before command execution.
	 */
//=========================================================
	public void always_executed_hook()
	{
		get_logger().info("In always_executed_hook method()");
	}



//=========================================================
	/**
	 * Execute command "CreateNewContext" on device.
	 * This command is used to register a snapShot context.
	 * All informations concerning the snapshot pattern (creation date, reason, description and, the <I>list of attributes</I> that are included in the context) are passed through an array (DEVVAR_STRINGARRAY).
	 *
 *	
 *
	 * @param	argin	All the informations usefull to create a context ,Snapshot pattern).
	 * @return	The new assigned context ID
	 */
//=========================================================
	public int create_new_context(String[] argin) throws DevFailed
	{
		int argout = -1;
		get_logger().info("Entering create_new_context()");

		// ---Add your Own code to control device here ---
		SnapContext snapContext = new SnapContext(argin);
		try
		{
			argout = SnapManagerApi.createContext(snapContext);
		}
		catch ( SnapshotingException e )
		{
			get_logger().warn(e.toString() , e);
			throw e.toTangoException();
		}
		get_logger().info("Exiting create_new_context()");
		return argout;
	}
	//=========================================================
    /**
     * Execute command "LaunchSnapShot" on device.
     * This command is used to trigger a snapShot.
     * All informations concerning the snapshot will be retrieved with the identifier parameter.
     *
     * @param   argin   The snapshot associated context's identifier.
     */
//=========================================================
    public synchronized short launch_snap_shot(short argin) throws DevFailed
    {
        Timestamp startDate = new Timestamp ( System.currentTimeMillis() );
        get_logger().info("Entering launch_snap_shot()");
        
        // ---Add your Own code to control device here ---
        SnapShot snapShot = null;
        short snapId = 0;
        SnapshotingException snapshotingException = new SnapshotingException();
        try
        {
            snapShot = SnapManagerApi.registerSnapShot(( int ) argin);
            snapId = (short) snapShot.getId_snap ();
            // For each attribute of the object 'Snapshot', a snapshot is triggered...
            ArrayList attributeList = snapShot.getAttribute_List();
            
            UsePluginBuilder builder = new UsePluginBuilder ( snapId , manager );
            UsePlugin group = builder.build ( attributeList );
            group.execute();
            Map<String, String> messages =group.getMessages ();
        }
        catch ( SnapshotingException e )
        {
            e.printStackTrace();
            Util.out2.println(e.toString());
            throw e.toTangoException();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        } 
        
        if ( snapshotingException.stackSize() != 0 )
        {
            Util.out2.println(snapshotingException.toString());
            throw snapshotingException.toTangoException();
        }

        return snapId;
    }

//=========================================================
	/**
	 * Execute command "SetEquipmentsWithSnapshot" on device.
	 * This command is used to set values to equipments.
	 *
	 * @param	argin	The snapshot from which equipments are set.
	 */
//=========================================================
	public void set_equipments_with_snapshot(String[] argin) throws DevFailed
	{
		get_logger().info("Entering set_equipments_with_snapshot() xxx");
		//System.out.println("set_equipments_with_snapshot(String[] argin)");
		SnapShot snapShot = new SnapShot(argin);
		// ---Add your Own code to control device here ---
		//System.out.println("snapshot : " + snapShot.toString());
		try
		{
            SnapManagerApi.TriggerSetEquipments(snapShot);
		}
        catch ( SnapshotingException e )
		{
            get_logger().warn(e.toString() , e);
			throw e.toTangoException();
		}
        catch ( Throwable t )
        {
            t.printStackTrace ();
        }
		get_logger().info("Exiting set_equipments_with_snapshot()");
	}

//	=========================================================
	/**
	 * Execute command "SetEquipments" on device.
	 * This command is used to set values to equipments.
	 *
	 * @param	argin	"SetEquipments arguments... <BR><blockquote> <ul>             <li><strong>First Case:</strong> Setpoint is  done on all the snapshot attributes             <ul>                  <li><var>argin</var>[0]<b> =</b> the snap identifier<li><var>argin</var>[1]<b> =</b> STORED_READ_VALUE (Setpoint with theirs read values) or STORED_WRITE_VALUE (Setpoint with theirs write values)<br> </ul><li><strong>Second Case: </strong> Setpoint is done on a set of the snapshot attributes               <ul>                  <li><var>argin</var>[0]<b> =</b> the snap identifier<li><var>argin</var>[1]<b> =</b> the number of attributes <br>           Let us note <i>&quot;<var>index</var>&quot; </i>the last <var>index</var> used (for example, at this point, <i><var>index</var></i> = 2).<li><var>argin</var>[index]<b> =</b> NEW_VALUE or STORED_READ_VALUE or STORED_WRITE_VALUE<li><var>argin</var>[index+1]<b> =</b> the attribut name <li><var>argin</var>[index+2]<b> =</b> the value to set when NEW_VALUE is requested </ul> </blockquote>" 
	 */
//=========================================================
	public void set_equipments(String[] argin) throws DevFailed
	{
		get_logger().info("Entering set_equipments() xxx");
		//System.out.println("set_equipments(String[] argin)");
		// ---Add your Own code to control device here ---
		//System.out.println("snapshot : " + snapShot.toString());
		if(argin.length < 2 ) {
            Except.throw_exception("CONFIGURATION_ERROR" ,
                                   "Wrong number of parameters" ,
                                   "SnapManager.SetEquipments");
        }
        else
        {
      		int snapId;
      		try{
     		    // Retrieve the snapid value
     			snapId = Integer.parseInt(argin[0]);
 
    	    	// The attributes of the snapshot are gotten as they were saved in the database.
    			SnapAttributeExtract[] snapAttributeExtractArray = m_manager.getSnap(snapId);
    			
    		    if ( snapAttributeExtractArray == null || snapAttributeExtractArray.length == 0 ) {
     				Except.throw_exception("CONFIGURATION_ERROR" ,
                            "Invalid SnapId" ,
                            "SnapManager.SetEquipments");
    		    }
         		SnapShot snapShot = SnapShot.getPartialSnapShot(argin , snapId, snapAttributeExtractArray);
        		if ( snapShot == null )
        			return;
        		SnapManagerApi.TriggerSetEquipments(snapShot);
        	}
        	catch ( SnapshotingException e )
        	{
        		get_logger().warn(e.toString() , e);
        		throw e.toTangoException();
        	}
    		catch ( NumberFormatException e ) {
   				Except.throw_exception("CONFIGURATION_ERROR" ,
                        "Wrong parameters values " ,
                        "SnapManager.SetEquipments");
			}	   		
        	catch ( Throwable t )
        	{
        		t.printStackTrace ();
        	}
        }
		get_logger().info("Exiting set_equipments()");
	}

//=========================================================
/**
 *	Execute command "UpdateSnapComment" on device.
 *	This command updates the comment of given snapshot
 *	
 *
 * @param	argin	1) snapshot identifier 2) The new comment
 */
//=========================================================
public void update_snap_comment(DevVarLongStringArray argin) throws DevFailed
	{
		get_logger().info("Entering update_snap_comment()");

		// ---Add your Own code to control device here ---
        int id_snap = argin.lvalue[0];
        String comment = argin.svalue[0];
        try {
            SnapManagerApi.updateSnapComment(id_snap , comment);
        } catch (SnapshotingException e) {
            get_logger().warn(e.toString() , e);
			throw e.toTangoException();
        }
        get_logger().info("Exiting update_snap_comment()");
	}
//===================================================================
/**
 * Method called by the read_attributes CORBA operation to
 * set internal attribute value.
 *
 * @param   attr    reference to the Attribute object
 */
//===================================================================
public void read_attr(Attribute attr) throws DevFailed
{
	String attr_name = attr.get_name();
	get_logger().info("In read_attr for attribute " + attr_name);

	//  Switch on attribute name
	//---------------------------------
	if ( attr_name == "version" )
	{
		//  Add your own code here
		attr.set_value(m_version);
	}
}

//=========================================================
	/**
	 * main part for the device server class
	 */
//=========================================================
	public static void main(String[] argv)
	{
		try
		{
			Util tg = Util.init(argv , "SnapManager");
			tg.server_init();

			System.out.println("Ready to accept request");

			tg.server_run();
		}

		catch ( OutOfMemoryError ex )
		{
			System.err.println("Can't allocate memory !!!!");
			System.err.println("Exiting");
		}
		catch ( UserException ex )
		{
			Except.print_exception(ex);

			System.err.println("Received a CORBA user exception");
			System.err.println("Exiting");
		}
		catch ( SystemException ex )
		{
			Except.print_exception(ex);

			System.err.println("Received a CORBA system exception");
			System.err.println("Exiting");
		}

		System.exit(-1);
	}
}


//--------------------------------------------------------------------------
/* end of $Source: /cvsroot/tango-cs/tango/jserver/snapshoting/SnapManager/SnapManager.java,v $ */