//+======================================================================
// $Source: /cvsroot/tango-cs/tango/jserver/archiving/HdbArchivingWatcher/GetErrorDomainsLatestErrorClass.java,v $
//
// Project:      Tango Device Server
//
// Description:  Java source code for the command TemplateClass of the
//               ArchivingWatcher class.
//
// $Author: ounsy $
//
// $Revision: 1.1 $
//
// $Log: GetErrorDomainsLatestErrorClass.java,v $
// Revision 1.1  2006/08/24 13:51:57  ounsy
// creation
//
// Revision 1.1  2006/01/19 16:36:59  ounsy
// New device specialised in watching archiving.
//
//
// copyleft :    European Synchrotron Radiation Facility
//               BP 220, Grenoble 38043
//               FRANCE
//
//-======================================================================
//
//  		This file is generated by POGO
//	(Program Obviously used to Generate tango Object)
//
//         (c) - Software Engineering Group - ESRF
//=============================================================================



/**
 * @author	$Author: ounsy $
 * @version	$Revision: 1.1 $
 */
package HdbArchivingWatcher;



import org.omg.CORBA.Any;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.Tango.DispLevel;
import fr.esrf.TangoDs.Command;
import fr.esrf.TangoDs.DeviceImpl;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.TangoDs.Util;

/**
 *	Class Description:
 *	
*/


public class GetErrorDomainsLatestErrorClass extends Command implements TangoConst
{
	//===============================================================
	/**
	 *	Constructor for Command class GetErrorDomainsLatestErrorClass
	 *
	 *	@param	name	command name
	 *	@param	in	argin type
	 *	@param	out	argout type
	 */
	//===============================================================
	public GetErrorDomainsLatestErrorClass(String name,int in,int out)
	{
		super(name, in, out);
	}

	//===============================================================
	/**
	 *	Constructor for Command class GetErrorDomainsLatestErrorClass
	 *
	 *	@param	name            command name
	 *	@param	in              argin type
	 *	@param	in_comments     argin description
	 *	@param	out             argout type
	 *	@param	out_comments    argout description
	 */
	//===============================================================
	public GetErrorDomainsLatestErrorClass(String name,int in,int out, String in_comments, String out_comments)
	{
		super(name, in, out, in_comments, out_comments);
	}
	//===============================================================
	/**
	 *	Constructor for Command class GetErrorDomainsLatestErrorClass
	 *
	 *	@param	name            command name
	 *	@param	in              argin type
	 *	@param	in_comments     argin description
	 *	@param	out             argout type
	 *	@param	out_comments    argout description
	 *	@param	level           The command display type OPERATOR or EXPERT
	 */
	//===============================================================
	public GetErrorDomainsLatestErrorClass(String name,int in,int out, String in_comments, String out_comments, DispLevel level)
	{
		super(name, in, out, in_comments, out_comments, level);
	}
	//===============================================================
	/**
	 *	return the result of the device's command.
	 */
	//===============================================================
	public Any execute(DeviceImpl device,Any in_any) throws DevFailed
	{
		Util.out2.println("GetErrorDomainsLatestErrorClass.execute(): arrived");
		return insert(((HdbArchivingWatcher)(device)).get_error_domains_latest_error());
	}

	//===============================================================
	/**
	 *	Check if it is allowed to execute the command.
	 */
	//===============================================================
	public boolean is_allowed(DeviceImpl device, Any data_in)
	{
		if (device.get_state() == DevState.OFF  ||
			device.get_state() == DevState.INIT)
		{
			//	End of Generated Code

			//	Re-Start of Generated Code
			return false;
		}
		return true;
	}
}
//-----------------------------------------------------------------------------
/* end of $Source: /cvsroot/tango-cs/tango/jserver/archiving/HdbArchivingWatcher/GetErrorDomainsLatestErrorClass.java,v $ */