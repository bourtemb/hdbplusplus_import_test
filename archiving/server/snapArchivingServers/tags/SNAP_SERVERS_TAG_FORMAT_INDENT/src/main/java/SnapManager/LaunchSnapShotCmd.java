//+======================================================================
// $Source$
//
// Project:      Tango Device Server
//
// Description:  Java source code for the command TemplateClass of the
//               SnapArchiver class.
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 1.3  2009/12/17 10:29:53  pierrejoseph
// CheckStyle:Organize imports
//
// Revision 1.2  2009/05/29 09:08:03  soleilarc
// manage exception for the LaunchSnapshot command
//
// Revision 1.1  2009/01/27 14:05:50  soleilarc
// move LauchSnapshot command from Archiver to Manager
//
// Revision 1.5  2006/04/12 15:46:40  ounsy
// corrected the missing attributes problem
//
// Revision 1.4  2005/11/29 17:33:03  chinkumo
// no message
//
// Revision 1.3.10.2  2005/11/29 16:16:50  chinkumo
// Code reformated (pogo compatible)
//
// Revision 1.3.10.1  2005/11/15 13:46:17  chinkumo
// ...
//
// Revision 1.3  2005/06/15 14:02:53  chinkumo
// The device was regenerated in Tango V5.
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
 * @author	$Author$
 * @version	$Revision$
 */
package SnapManager;

import org.omg.CORBA.Any;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DispLevel;
import fr.esrf.TangoDs.Command;
import fr.esrf.TangoDs.DeviceImpl;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.TangoDs.Util;

/**
 * Class Description: This command is used to trigger a snapShot. All
 * informations concerning the snapshot will be retrieved with the identifier
 * parameter.
 */

public class LaunchSnapShotCmd extends Command implements TangoConst {
	// ===============================================================
	/**
	 * Constructor for Command class LaunchSnapShotCmd
	 * 
	 * @param name
	 *            command name
	 * @param in
	 *            argin type
	 * @param out
	 *            argout type
	 */
	// ===============================================================
	public LaunchSnapShotCmd(String name, int in, int out) {
		super(name, in, out);
	}

	// ===============================================================
	/**
	 * Constructor for Command class LaunchSnapShotCmd
	 * 
	 * @param name
	 *            command name
	 * @param in
	 *            argin type
	 * @param in_comments
	 *            argin description
	 * @param out
	 *            argout type
	 * @param out_comments
	 *            argout description
	 */
	// ===============================================================
	public LaunchSnapShotCmd(String name, int in, int out, String in_comments,
			String out_comments) {
		super(name, in, out, in_comments, out_comments);
	}

	// ===============================================================
	/**
	 * Constructor for Command class LaunchSnapShotCmd
	 * 
	 * @param name
	 *            command name
	 * @param in
	 *            argin type
	 * @param in_comments
	 *            argin description
	 * @param out
	 *            argout type
	 * @param out_comments
	 *            argout description
	 * @param level
	 *            The command display type OPERATOR or EXPERT
	 */
	// ===============================================================
	public LaunchSnapShotCmd(String name, int in, int out, String in_comments,
			String out_comments, DispLevel level) {
		super(name, in, out, in_comments, out_comments, level);
	}

	// ===============================================================
	/**
	 * return the result of the device's command.
	 */
	// ===============================================================
	public Any execute(DeviceImpl device, Any in_any) throws DevFailed {
		Util.out2.println("LaunchSnapShotCmd.execute(): arrived");
		short argin = extract_DevShort(in_any);
		if (argin < 0) {
			Except.throw_exception("INPUT_ERROR", "Invalid Context ID",
					"SnapManager.LaunchSnapshot");
		}

		return insert(((SnapManager) (device)).launch_snap_shot(argin));
	}

	// ===============================================================
	/**
	 * Check if it is allowed to execute the command.
	 */
	// ===============================================================
	public boolean is_allowed(DeviceImpl device, Any data_in) {
		// End of Generated Code

		// Re-Start of Generated Code
		return true;
	}
}

// -----------------------------------------------------------------------------
/*
 * end of $Source:
 * /cvsroot/tango-cs/archiving/server/snapArchivingServers/src/main
 * /java/SnapManager/LaunchSnapShotCmd.java,v $
 */