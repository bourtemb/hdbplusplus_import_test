/**
 * @author  $Author$
 * @version $Revision$
 */
package TdbExtractor;

import org.omg.CORBA.Any;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DispLevel;
import fr.esrf.TangoDs.Command;
import fr.esrf.TangoDs.DeviceImpl;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.TangoDs.Util;

/**
 * Class Description: Returns "true" if the attribute of given name is currently
 * archived, "false" otherwise.
 */

public class GetMaxTimeClass extends Command implements TangoConst {
	// ===============================================================
	/**
	 * Constructor for Command class IsArchivedClass
	 * 
	 * @param name
	 *            command name
	 * @param in
	 *            argin type
	 * @param out
	 *            argout type
	 */
	// ===============================================================
	public GetMaxTimeClass(String name, int in, int out) {
		super(name, in, out);
	}

	// ===============================================================
	/**
	 * Constructor for Command class IsArchivedClass
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
	public GetMaxTimeClass(String name, int in, int out, String in_comments,
			String out_comments) {
		super(name, in, out, in_comments, out_comments);
	}

	// ===============================================================
	/**
	 * Constructor for Command class IsArchivedClass
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
	public GetMaxTimeClass(String name, int in, int out, String in_comments,
			String out_comments, DispLevel level) {
		super(name, in, out, in_comments, out_comments, level);
	}

	// ===============================================================
	/**
	 * return the result of the device's command.
	 */
	// ===============================================================
	public Any execute(DeviceImpl device, Any in_any) throws DevFailed {
		Util.out2.println("GetMaxTimeClass.execute(): arrived");
		String argin = extract_DevString(in_any);
		return insert(((TdbExtractor) (device)).get_max_time(argin));
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
 * /cvsroot/tango-cs/archiving/server/hdbtdbArchivingServers/src/
 * main/java/TdbExtractor/GetMaxTimeClass.java,v $
 */