//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// : pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// :  $
//
//-======================================================================

package org.tango.hdbcpp.diagnostics;


import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import org.tango.hdbcpp.common.Subscriber;
import org.tango.hdbcpp.common.SubscriberMap;
import org.tango.hdbcpp.tools.TangoUtils;
import org.tango.hdbcpp.tools.Utils;

import java.util.Date;
import java.util.List;

/**
 * This class is able to send a command on all subscribers
 * It could be triggered by external program like a unix cron
 *  to reset counters for instance
 *
 * @author verdier
 */

public class SubscriberCommand {
    private  List<Subscriber>   subscribers;
    //===============================================================
    //===============================================================
    private SubscriberCommand() throws DevFailed {
        String  configuratorDeviceName = TangoUtils.getConfiguratorDeviceName();
        DeviceProxy configuratorProxy = new DeviceProxy(configuratorDeviceName);

        //  Get subscriber labels if any
        SubscriberMap subscriberMap = new SubscriberMap(configuratorProxy);
        subscribers = subscriberMap.getSubscriberList();
    }
    //===============================================================
    //===============================================================
    private void executeCommand(String commandName) throws Exception {
        String errorMessage = "";
        for (Subscriber subscriber : subscribers) {
            try {
                subscriber.command_inout(commandName);
            }
            catch (DevFailed e) {
                errorMessage += subscriber.getLabel() + ": "+e.errors[0].desc+"\n";
            }
        }
        if (!errorMessage.isEmpty()) {
            throw new Exception(errorMessage);
        }
    }
    //===============================================================
    //===============================================================
    private void saveEventNumber(String fileName) throws Exception {
        String errorMessage = "";
        long totalEvents = 0;
        long totalAttributes = 0;
        //  read event number for each subscriber
        for (Subscriber subscriber : subscribers) {
            try {
                DeviceAttribute attribute = subscriber.read_attribute("AttributeEventNumberList");
                if (!attribute.hasFailed()) {
                    int[] nbEvents = attribute.extractLongArray();
                    for (int nb : nbEvents) {
                        totalEvents += nb;
                        totalAttributes++;
                    }
                }
             }
            catch (DevFailed e) {
                errorMessage += subscriber.getLabel() + ": "+e.errors[0].desc+"\n";
            }
        }
        System.out.println("Total events:   " + totalEvents + "/" + totalAttributes);
        //  Rad file and append
        String code;
        try {
            code = Utils.readFile(fileName);
        } catch (DevFailed e) {
            code = "Date\tEvents Received\tNb Attributes\n";
        }
        code += new Date().toString() + "\t" + totalEvents + "\t" + totalAttributes + "\n";
        Utils.writeFile(fileName, code);

        if (!errorMessage.isEmpty()) {
            throw new Exception(errorMessage);
        }
    }
    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        if (args.length>0) {
            try {
                SubscriberCommand   subscriberCommand = new SubscriberCommand();
                String commandName = args[0];
                if (commandName.equals("-save")) {
                    if (args.length>1) {
                        String fileName = args[1];
                        subscriberCommand.saveEventNumber(fileName);
                    }
                    else
                        System.err.println("File name expected");
                }
                else
                    subscriberCommand.executeCommand(commandName);
            }
            catch (Exception e) {
                if (e instanceof DevFailed) {
                    System.err.println(new Date()+":");
                    Except.print_exception(e);
                }
                else
                    System.err.println(new Date()+":\n  "+e);
            }
        }
        else
            System.err.println("Command name expected");
        System.exit(0);
    }
}
