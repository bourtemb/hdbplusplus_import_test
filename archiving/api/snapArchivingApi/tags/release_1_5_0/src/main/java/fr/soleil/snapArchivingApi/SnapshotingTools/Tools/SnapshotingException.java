package fr.soleil.snapArchivingApi.SnapshotingTools.Tools;

import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;

//+======================================================================
// $Source: /cvsroot/tango-cs/tango/api/java/fr/soleil/TangoSnapshoting/SnapshotingTools/Tools/SnapshotingException.java,v $
//
// Project:      Tango Archiving Service
//
// Description:  Java source code for the class  SnapshotingException.
//						(chinkumo) - 24 juin 2005
//
// $Author: soleilarc $
//
// $Revision: 1.5 $
//
// $Log: SnapshotingException.java,v $
// Revision 1.5  2007/10/15 13:35:13  soleilarc
// Author: XP
// Mantis bug: 6695
// Comment: Add a new SnapshotingException builder : public SnapshotingException(DevFailed devFailed). For the builder SnapshotingException(String message, String reason, ErrSeverity archSeverity, String desc, String origin, Exception e), put another skeleton : if () {�} else if () {�} else if () {�}.
//
// Revision 1.4  2007/10/03 16:07:18  soleilarc
// Author: XP
// Mantis bug ID: 6594
// Comment: SnapshotingException has been completed to be able to recognize a timeout.
//
// Revision 1.3  2006/05/17 10:13:28  ounsy
// corrected a bug
//
// Revision 1.2  2005/11/29 17:11:17  chinkumo
// no message
//
// Revision 1.1.10.1  2005/11/15 13:34:38  chinkumo
// no message
//
// Revision 1.1  2005/06/28 07:41:13  chinkumo
// The SnapshotingException object defines exceptions for the Snapshoting Service.
//
//
// copyleft :	Synchrotron SOLEIL
//					L'Orme des Merisiers
//					Saint-Aubin - BP 48
//					91192 GIF-sur-YVETTE CEDEX
//
//-======================================================================

public class SnapshotingException extends Exception {
	private String archExcepMessage;
	private DevError[] devErrorTab;
	private static final String TIMEOUT = "org.omg.CORBA.TIMEOUT";
	private boolean isDueToATimeOut = false;

	public SnapshotingException() {
		super();
		archExcepMessage = "";
		devErrorTab = null;
	}

	/**
	 * @param message
	 *            deprecated
	 */
	public SnapshotingException(String message) {
		super(message);
		archExcepMessage = message;
		String reason = "Unknown reason";
		ErrSeverity archSeverity = ErrSeverity.WARN;
		String desc = "Unknown exception";
		String origin = this.getClass().toString();
		DevError devError = new DevError(reason, archSeverity, desc, origin);
		devErrorTab = new DevError[1];
		devErrorTab[0] = devError;
	}

	/**
	 * This class can be instanciated when exceptions in the archiving service.
	 * Exceptions can be : ConnectionException, ATKException
	 */
	public SnapshotingException(String message, String reason,
			ErrSeverity archSeverity, String desc, String origin) {
		super(message);
		archExcepMessage = message;
		String _reason = (reason == null || reason.equals("")) ? "Unknown reason"
				: reason;
		ErrSeverity _archSeverity = (archSeverity == null) ? ErrSeverity.WARN
				: archSeverity;
		String _desc = (desc == null || desc.equals("")) ? "Unknown exception"
				: desc;
		String _origin = (origin == null || origin.equals("")) ? "Unknown origin"
				: origin;
		DevError devError = new DevError(_reason, _archSeverity, _desc, _origin);
		devErrorTab = new DevError[1];
		devErrorTab[0] = devError;
	}

	public SnapshotingException(DevFailed devFailed) {
		super(devFailed);
		archExcepMessage = "";

		// The current stack is initialized
		DevError[] errors = devFailed.errors;
		int l = errors == null ? 0 : devFailed.errors.length;
		devErrorTab = new DevError[l];
		// The stack of the catched error copied
		if (errors != null) {
			System.arraycopy(errors, 0, devErrorTab, 0, l);
		}
	}

	public SnapshotingException(String message, String reason,
			ErrSeverity archSeverity, String desc, String origin, Exception e) {
		super(e);
		archExcepMessage = message;

		if (e instanceof DevFailed) {
			// The current stack is initialized
			DevError[] errors = ((DevFailed) e).errors;
			int l = errors == null ? 0 : ((DevFailed) e).errors.length;
			devErrorTab = new DevError[l + 1];
			// The stack of the catched error copied
			if (errors != null) {
				System.arraycopy(errors, 0, devErrorTab, 0, l);
			}
			// A new DevError object is built with the given parameters
			String _reason = (reason == null || reason.equals("")) ? "Unknown reason"
					: reason;
			ErrSeverity _archSeverity = (archSeverity == null) ? ErrSeverity.WARN
					: archSeverity;
			String _desc = (desc == null || desc.equals("")) ? "DevFailed EXCEPTION"
					: desc;
			String _origin = (origin == null || origin.equals("")) ? e
					.getClass().getName() : origin;
			DevError devError = new DevError(_reason, _archSeverity, _desc,
					_origin);
			// The DevError object is added at the end of the stack
			devErrorTab[devErrorTab.length - 1] = devError;
		} else if (e instanceof SnapshotingException) {
			// The current stack is initialized
			devErrorTab = new DevError[((SnapshotingException) e).devErrorTab.length + 1];
			// The stack of the catched error copied
			System.arraycopy(((SnapshotingException) e).devErrorTab, 0,
					devErrorTab, 0,
					((SnapshotingException) e).devErrorTab.length);
			// A new DevError object is built with the given parameters
			String _reason = (reason == null || reason.equals("")) ? "Unknown reason"
					: reason;
			ErrSeverity _archSeverity = (archSeverity == null) ? ErrSeverity.WARN
					: archSeverity;
			String _desc = (desc == null || desc.equals("")) ? "Unknown exception"
					: desc;
			String _origin = (origin == null || origin.equals("")) ? e
					.getClass().getName() : origin;
			DevError devError = new DevError(_reason, _archSeverity, _desc,
					_origin);

			// The DevError object is added at the end of the stack
			devErrorTab[devErrorTab.length - 1] = devError;
		} else {
			// The current stack is initialized
			devErrorTab = new DevError[2];
			// A new DevError object is built for the catched exception
			// (Original exception)
			String reason_original = e.getMessage();
			ErrSeverity archSeverity_original = ErrSeverity.WARN;
			String desc_original = e.getLocalizedMessage();
			String origin_original = e.getClass().getName();
			DevError devError_original = new DevError(reason_original,
					archSeverity_original, desc_original, origin_original);
			// A new DevError object is built with the given parameters
			String _reason = (reason == null || reason.equals("")) ? "Unknown reason"
					: reason;
			ErrSeverity _archSeverity = ((archSeverity == null) ? ErrSeverity.WARN
					: archSeverity);
			String _desc = (desc == null || desc.equals("")) ? "Unknown exception"
					: desc;
			String _origin = (origin == null || origin.equals("")) ? e
					.getClass().getName() : origin;
			DevError devError = new DevError(_reason, _archSeverity, _desc,
					_origin);

			// The DevError objects are added at the end of the stack
			devErrorTab[0] = devError_original;
			devErrorTab[1] = devError;
		}
	}

	public void addStack(String message, String reason,
			ErrSeverity archSeverity, String desc, String origin,
			SnapshotingException e) {
		archExcepMessage = message;
		// new SnapshotingException
		String _reason = (reason == null || reason.equals("")) ? "Unknown reason"
				: reason;
		ErrSeverity _archSeverity = ((archSeverity == null) ? ErrSeverity.WARN
				: archSeverity);
		String _desc = (desc == null || desc.equals("")) ? "Unknown exception"
				: desc;
		String _origin = (origin == null || origin.equals("")) ? e.getClass()
				.getName() : origin;
		DevError _devError = new DevError(_reason, _archSeverity, _desc,
				_origin);
		// The current stack is cloned
		DevError[] devErrorTabClone = devErrorTab;

		if (devErrorTabClone != null) {
			// The current stack is re-initialized
			devErrorTab = new DevError[devErrorTabClone.length
					+ e.devErrorTab.length + 1];
			// The cloned is copied again
			System.arraycopy(devErrorTabClone, 0, devErrorTab, 0,
					devErrorTabClone.length);
			// The stack of the catched error copied
			System.arraycopy(e.devErrorTab, 0, devErrorTab,
					devErrorTabClone.length, e.devErrorTab.length);
			// The DevError builded with the given parameters is added at the
			// end of the stack
			devErrorTab[devErrorTabClone.length + e.devErrorTab.length] = _devError;
		} else {
			// The current stack is re-initialized
			devErrorTab = new DevError[e.devErrorTab.length + 1];
			// The stack of the catched error copied
			System.arraycopy(e.devErrorTab, 0, devErrorTab, 0,
					e.devErrorTab.length);
			// The DevError builded with the given parameters is added at the
			// end of the stack
			devErrorTab[e.devErrorTab.length] = _devError;
		}
	}

	public String getMessage() {
		return archExcepMessage;
	}

	public String getLastExceptionMessage() {
		return devErrorTab[0].desc;
	}

	/**
	 * @return the devErrorTab
	 */
	public DevError[] getDevErrorTab() {
		return devErrorTab;
	}

	/**
	 * @param devErrorTab
	 *            the devErrorTab to set
	 */
	public void setDevErrorTab(DevError[] devErrorTab) {
		this.devErrorTab = devErrorTab;
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Message : ").append(archExcepMessage).append(
				"\r\n");
		for (int i = 0; i < devErrorTab.length; i++) {
			DevError devError = devErrorTab[i];
			stringBuffer.append("\t [").append(i + 1).append("] : ").append(
					"\r\n");
			stringBuffer.append("\t").append("\t").append("Reason : ").append(
					devError.reason).append("\r\n");
			stringBuffer.append("\t").append("\t").append("Severity : ")
					.append(errorSeverityToString(devError.severity)).append(
							"\r\n");
			stringBuffer.append("\t").append("\t").append("Description : ")
					.append(devError.desc).append("\r\n");
			stringBuffer.append("\t").append("\t").append("Origin : ").append(
					devError.origin).append("\r\n");
		}
		return stringBuffer.toString();
	}

	private String errorSeverityToString(ErrSeverity errSeverity) {
		if (errSeverity == null)
			return "WARNING";
		switch (errSeverity.value()) {
		case ErrSeverity._ERR:
			return "ERROR";
		case ErrSeverity._PANIC:
			return "PANIC";
		default:
			return "WARNING";
		}

	}

	public DevFailed toTangoException() {
		DevFailed devFailed = new DevFailed(archExcepMessage, devErrorTab);
		return devFailed;
	}

	public int stackSize() {
		if (devErrorTab == null)
			return 0;
		else
			return devErrorTab.length;
	}

	public boolean computeIsDueToATimeOut() {
		Throwable cause = this.getCause();
		if (cause instanceof DevFailed) {
			DevFailed devFailedCause = (DevFailed) cause;
			DevError[] errs = devFailedCause.errors;
			if (errs != null) {
				for (int i = 0; i < errs.length; i++) {
					DevError nextErr = errs[i];
					/*
					 * String desc = nextErr.desc; String origin =
					 * nextErr.origin;
					 */
					String reason = nextErr.reason;

					/*
					 * System.out.println("CLA/DevFailed/desc/"+desc+"/");
					 * System.out.println("CLA/DevFailed/origin/"+origin+"/");
					 * System.out.println("CLA/DevFailed/reason/"+reason+"/");
					 */

					if (reason.indexOf(TIMEOUT) != -1) {
						this.setDueToATimeOut(true);
						return true;
					}
				}
			}
		}

		this.setDueToATimeOut(false);
		return false;
	}

	/**
	 * @return Returns the isDueToATimeOut.
	 */
	public boolean isDueToATimeOut() {
		return this.isDueToATimeOut;
	}

	/**
	 * @param isDueToATimeOut
	 *            The isDueToATimeOut to set.
	 */
	public void setDueToATimeOut(boolean isDueToATimeOut) {
		this.isDueToATimeOut = isDueToATimeOut;
	}
}