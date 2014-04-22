/*----- PROTECTED REGION ID(HdbEventSubscriber.cpp) ENABLED START -----*/
static const char *RcsId = "$Id: HdbEventSubscriber.cpp,v 1.8 2014-03-07 14:05:54 graziano Exp $";
//=============================================================================
//
// file :        HdbEventSubscriber.cpp
//
// description : C++ source for the HdbEventSubscriber and its commands.
//               The class is derived from Device. It represents the
//               CORBA servant object which will be accessed from the
//               network. All commands which can be executed on the
//               HdbEventSubscriber are implemented in this file.
//
// project :     Tango Device Server.
//
// $Author: graziano $
//
// $Revision: 1.8 $
// $Date: 2014-03-07 14:05:54 $
//
// SVN only:
// $HeadURL$
//
// CVS only:
// $Source: /home/cvsadm/cvsroot/fermi/servers/hdb++/hdb++es/src/HdbEventSubscriber.cpp,v $
// $Log: HdbEventSubscriber.cpp,v $
// Revision 1.8  2014-03-07 14:05:54  graziano
// added ResetStatistics command
//
// Revision 1.7  2014-03-06 15:21:43  graziano
// StartArchivingAtStartup,
// start_all and stop_all,
// archiving of first event received at subscribe
//
// Revision 1.6  2014-02-20 15:17:29  graziano
// name and path fixing
// regenerated with new pogo
// added StartArchivingAtStartup property
//
// Revision 1.5  2013-09-02 12:18:46  graziano
// cleaned
//
// Revision 1.4  2013-08-26 13:24:57  graziano
// added transformation to lowercase
//
// Revision 1.3  2013-08-23 10:04:53  graziano
// development
//
// Revision 1.2  2013-08-14 13:10:07  graziano
// development
//
// Revision 1.1  2013-07-17 13:37:43  graziano
// *** empty log message ***
//
//
//
//=============================================================================
//                This file is generated by POGO
//        (Program Obviously used to Generate tango Object)
//=============================================================================


#include <tango.h>
#include <HdbEventSubscriber.h>
#include <HdbEventSubscriberClass.h>

/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber.cpp

/**
 *  HdbEventSubscriber class description:
 *    This class is able to subscribe on archive events and store value in Historical DB
 */

//================================================================
//  The following table gives the correspondence
//  between command and method names.
//
//  Command name     |  Method name
//================================================================
//  State            |  Inherited (no method)
//  Status           |  Inherited (no method)
//  AttributeAdd     |  attribute_add
//  AttributeRemove  |  attribute_remove
//  AttributeStatus  |  attribute_status
//  Start            |  start
//  Stop             |  stop
//  AttributeStart   |  attribute_start
//  AttributeStop    |  attribute_stop
//  ResetStatistics  |  reset_statistics
//================================================================

//================================================================
//  Attributes managed are:
//================================================================
//  AttributeOkNumber       |  Tango::DevLong	Scalar
//  AttributeNokNumber      |  Tango::DevLong	Scalar
//  AttributePendingNumber  |  Tango::DevLong	Scalar
//  AttributeNumber         |  Tango::DevLong	Scalar
//  AttributeList           |  Tango::DevString	Spectrum  ( max = 10000)
//  AttributeOkList         |  Tango::DevString	Spectrum  ( max = 10000)
//  AttributeNokList        |  Tango::DevString	Spectrum  ( max = 10000)
//  AttributePendingList    |  Tango::DevString	Spectrum  ( max = 10000)
//================================================================

namespace HdbEventSubscriber_ns
{
/*----- PROTECTED REGION ID(HdbEventSubscriber::namespace_starting) ENABLED START -----*/

	//	static initializations

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::namespace_starting

//--------------------------------------------------------
/**
 *	Method      : HdbEventSubscriber::HdbEventSubscriber()
 *	Description : Constructors for a Tango device
 *                implementing the classHdbEventSubscriber
 */
//--------------------------------------------------------
HdbEventSubscriber::HdbEventSubscriber(Tango::DeviceClass *cl, string &s)
 : TANGO_BASE_CLASS(cl, s.c_str())
{
	/*----- PROTECTED REGION ID(HdbEventSubscriber::constructor_1) ENABLED START -----*/

	init_device();

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::constructor_1
}
//--------------------------------------------------------
HdbEventSubscriber::HdbEventSubscriber(Tango::DeviceClass *cl, const char *s)
 : TANGO_BASE_CLASS(cl, s)
{
	/*----- PROTECTED REGION ID(HdbEventSubscriber::constructor_2) ENABLED START -----*/

	init_device();

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::constructor_2
}
//--------------------------------------------------------
HdbEventSubscriber::HdbEventSubscriber(Tango::DeviceClass *cl, const char *s, const char *d)
 : TANGO_BASE_CLASS(cl, s, d)
{
	/*----- PROTECTED REGION ID(HdbEventSubscriber::constructor_3) ENABLED START -----*/

	init_device();

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::constructor_3
}

//--------------------------------------------------------
/**
 *	Method      : HdbEventSubscriber::delete_device()
 *	Description : will be called at device destruction or at init command
 */
//--------------------------------------------------------
void HdbEventSubscriber::delete_device()
{
	DEBUG_STREAM << "HdbEventSubscriber::delete_device() " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::delete_device) ENABLED START -----*/

	//	Delete device allocated objects
	cout << "-------- Delete device's allocated object --------" << endl;
	stop();
	delete hdb_dev;
	cout << "-------- Delete device's allocated object done !--------" << endl;
	//Tango::client_leavefunc();

	if(attr_AttributeList_read != NULL)
		delete [] attr_AttributeList_read;
	if(attr_AttributeOkList_read != NULL)
		delete [] attr_AttributeOkList_read;
	if(attr_AttributeNokList_read != NULL)
		delete [] attr_AttributeNokList_read;
	if(attr_AttributePendingList_read != NULL)
		delete [] attr_AttributePendingList_read;

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::delete_device
	delete[] attr_AttributeOkNumber_read;
	delete[] attr_AttributeNokNumber_read;
	delete[] attr_AttributePendingNumber_read;
	delete[] attr_AttributeNumber_read;
}

//--------------------------------------------------------
/**
 *	Method      : HdbEventSubscriber::init_device()
 *	Description : will be called at device initialization.
 */
//--------------------------------------------------------
void HdbEventSubscriber::init_device()
{
	DEBUG_STREAM << "HdbEventSubscriber::init_device() create device " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::init_device_before) ENABLED START -----*/

	//	Initialization before get_device_property() call
	

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::init_device_before
	

	//	Get the device properties from database
	get_device_property();
	
	attr_AttributeOkNumber_read = new Tango::DevLong[1];
	attr_AttributeNokNumber_read = new Tango::DevLong[1];
	attr_AttributePendingNumber_read = new Tango::DevLong[1];
	attr_AttributeNumber_read = new Tango::DevLong[1];

	/*----- PROTECTED REGION ID(HdbEventSubscriber::init_device) ENABLED START -----*/
	attr_AttributeList_read = NULL;
	attr_AttributeOkList_read = NULL;
	attr_AttributeNokList_read = NULL;
	attr_AttributePendingList_read = NULL;

	//	Initialize device
	initialized = false;
	set_state(Tango::MOVING);
	set_status("Initializing....");

	//	Create one event handler by HDB access device
	string	status("");
	hdb_dev = new HdbDevice(subscribeRetryPeriod, this);
	hdb_dev->startArchivingAtStartup = startArchivingAtStartup;
	try
	{
		hdb_dev->initialize();
	}
	catch(Tango::DevFailed &e)
	{
		status += "PushThread:\n";
		status += e.errors[0].desc;
	}
	//	Check if WARNING
	if (hdb_dev->status.length()>0)
	{
		status += "PushThread:\n";
		status += hdb_dev->status;
	}
	//	Set state and status if something wrong
	if (status.length()>0)
	{
		set_state(Tango::ALARM);
		set_status(status);
		cout << status << endl;
	}

	initialized = true;

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::init_device
}

//--------------------------------------------------------
/**
 *	Method      : HdbEventSubscriber::get_device_property()
 *	Description : Read database to initialize property data members.
 */
//--------------------------------------------------------
void HdbEventSubscriber::get_device_property()
{
	/*----- PROTECTED REGION ID(HdbEventSubscriber::get_device_property_before) ENABLED START -----*/

	//	Initialize property data members
	subscribeRetryPeriod = 60;

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::get_device_property_before


	//	Read device properties from database.
	Tango::DbData	dev_prop;
	dev_prop.push_back(Tango::DbDatum("SubscribeRetryPeriod"));
	dev_prop.push_back(Tango::DbDatum("AttributeList"));
	dev_prop.push_back(Tango::DbDatum("DbHost"));
	dev_prop.push_back(Tango::DbDatum("DbUser"));
	dev_prop.push_back(Tango::DbDatum("DbPassword"));
	dev_prop.push_back(Tango::DbDatum("DbName"));
	dev_prop.push_back(Tango::DbDatum("DbPort"));
	dev_prop.push_back(Tango::DbDatum("StartArchivingAtStartup"));

	//	is there at least one property to be read ?
	if (dev_prop.size()>0)
	{
		//	Call database and extract values
		if (Tango::Util::instance()->_UseDb==true)
			get_db_device()->get_property(dev_prop);
	
		//	get instance on HdbEventSubscriberClass to get class property
		Tango::DbDatum	def_prop, cl_prop;
		HdbEventSubscriberClass	*ds_class =
			(static_cast<HdbEventSubscriberClass *>(get_device_class()));
		int	i = -1;

		//	Try to initialize SubscribeRetryPeriod from class property
		cl_prop = ds_class->get_class_property(dev_prop[++i].name);
		if (cl_prop.is_empty()==false)	cl_prop  >>  subscribeRetryPeriod;
		else {
			//	Try to initialize SubscribeRetryPeriod from default device value
			def_prop = ds_class->get_default_device_property(dev_prop[i].name);
			if (def_prop.is_empty()==false)	def_prop  >>  subscribeRetryPeriod;
		}
		//	And try to extract SubscribeRetryPeriod value from database
		if (dev_prop[i].is_empty()==false)	dev_prop[i]  >>  subscribeRetryPeriod;

		//	Try to initialize AttributeList from class property
		cl_prop = ds_class->get_class_property(dev_prop[++i].name);
		if (cl_prop.is_empty()==false)	cl_prop  >>  attributeList;
		else {
			//	Try to initialize AttributeList from default device value
			def_prop = ds_class->get_default_device_property(dev_prop[i].name);
			if (def_prop.is_empty()==false)	def_prop  >>  attributeList;
		}
		//	And try to extract AttributeList value from database
		if (dev_prop[i].is_empty()==false)	dev_prop[i]  >>  attributeList;

		//	Try to initialize DbHost from class property
		cl_prop = ds_class->get_class_property(dev_prop[++i].name);
		if (cl_prop.is_empty()==false)	cl_prop  >>  dbHost;
		else {
			//	Try to initialize DbHost from default device value
			def_prop = ds_class->get_default_device_property(dev_prop[i].name);
			if (def_prop.is_empty()==false)	def_prop  >>  dbHost;
		}
		//	And try to extract DbHost value from database
		if (dev_prop[i].is_empty()==false)	dev_prop[i]  >>  dbHost;

		//	Try to initialize DbUser from class property
		cl_prop = ds_class->get_class_property(dev_prop[++i].name);
		if (cl_prop.is_empty()==false)	cl_prop  >>  dbUser;
		else {
			//	Try to initialize DbUser from default device value
			def_prop = ds_class->get_default_device_property(dev_prop[i].name);
			if (def_prop.is_empty()==false)	def_prop  >>  dbUser;
		}
		//	And try to extract DbUser value from database
		if (dev_prop[i].is_empty()==false)	dev_prop[i]  >>  dbUser;

		//	Try to initialize DbPassword from class property
		cl_prop = ds_class->get_class_property(dev_prop[++i].name);
		if (cl_prop.is_empty()==false)	cl_prop  >>  dbPassword;
		else {
			//	Try to initialize DbPassword from default device value
			def_prop = ds_class->get_default_device_property(dev_prop[i].name);
			if (def_prop.is_empty()==false)	def_prop  >>  dbPassword;
		}
		//	And try to extract DbPassword value from database
		if (dev_prop[i].is_empty()==false)	dev_prop[i]  >>  dbPassword;

		//	Try to initialize DbName from class property
		cl_prop = ds_class->get_class_property(dev_prop[++i].name);
		if (cl_prop.is_empty()==false)	cl_prop  >>  dbName;
		else {
			//	Try to initialize DbName from default device value
			def_prop = ds_class->get_default_device_property(dev_prop[i].name);
			if (def_prop.is_empty()==false)	def_prop  >>  dbName;
		}
		//	And try to extract DbName value from database
		if (dev_prop[i].is_empty()==false)	dev_prop[i]  >>  dbName;

		//	Try to initialize DbPort from class property
		cl_prop = ds_class->get_class_property(dev_prop[++i].name);
		if (cl_prop.is_empty()==false)	cl_prop  >>  dbPort;
		else {
			//	Try to initialize DbPort from default device value
			def_prop = ds_class->get_default_device_property(dev_prop[i].name);
			if (def_prop.is_empty()==false)	def_prop  >>  dbPort;
		}
		//	And try to extract DbPort value from database
		if (dev_prop[i].is_empty()==false)	dev_prop[i]  >>  dbPort;

		//	Try to initialize StartArchivingAtStartup from class property
		cl_prop = ds_class->get_class_property(dev_prop[++i].name);
		if (cl_prop.is_empty()==false)	cl_prop  >>  startArchivingAtStartup;
		else {
			//	Try to initialize StartArchivingAtStartup from default device value
			def_prop = ds_class->get_default_device_property(dev_prop[i].name);
			if (def_prop.is_empty()==false)	def_prop  >>  startArchivingAtStartup;
		}
		//	And try to extract StartArchivingAtStartup value from database
		if (dev_prop[i].is_empty()==false)	dev_prop[i]  >>  startArchivingAtStartup;

	}

	/*----- PROTECTED REGION ID(HdbEventSubscriber::get_device_property_after) ENABLED START -----*/

	//	Check device property data members init
	//cout << "hdbAccessDevice      = " << hdbAccessDevice << endl;
	cout << "subscribeRetryPeriod = " << subscribeRetryPeriod << endl;

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::get_device_property_after
}

//--------------------------------------------------------
/**
 *	Method      : HdbEventSubscriber::always_executed_hook()
 *	Description : method always executed before any command is executed
 */
//--------------------------------------------------------
void HdbEventSubscriber::always_executed_hook()
{
	INFO_STREAM << "HdbEventSubscriber::always_executed_hook()  " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::always_executed_hook) ENABLED START -----*/

	//	code always executed before all requests
	//	Do not check state before end ofinitializing phase
	if (initialized)
	{
		Tango::DevState	state = hdb_dev->subcribing_state();
		set_state(state);

		if (state==Tango::ON)
			set_status("Everything is OK");
		else
			set_status("At least, one signal is faulty");
	}

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::always_executed_hook
}

//--------------------------------------------------------
/**
 *	Method      : HdbEventSubscriber::read_attr_hardware()
 *	Description : Hardware acquisition for attributes
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_attr_hardware(TANGO_UNUSED(vector<long> &attr_list))
{
	DEBUG_STREAM << "HdbEventSubscriber::read_attr_hardware(vector<long> &attr_list) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_attr_hardware) ENABLED START -----*/

	//	Add your own code

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_attr_hardware
}

//--------------------------------------------------------
/**
 *	Read attribute AttributeOkNumber related method
 *	Description: Number of archived attributes not in error
 *
 *	Data type:	Tango::DevLong
 *	Attr type:	Scalar
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_AttributeOkNumber(Tango::Attribute &attr)
{
	DEBUG_STREAM << "HdbEventSubscriber::read_AttributeOkNumber(Tango::Attribute &attr) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_AttributeOkNumber) ENABLED START -----*/

	//	Set the attribute value
	if (initialized)
	{
		*attr_AttributeOkNumber_read = hdb_dev->get_sig_not_on_error_num();
	}
	else
		*attr_AttributeOkNumber_read = 0;

	attr.set_value(attr_AttributeOkNumber_read);

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_AttributeOkNumber
}
//--------------------------------------------------------
/**
 *	Read attribute AttributeNokNumber related method
 *	Description: Number of archived attributes in error
 *
 *	Data type:	Tango::DevLong
 *	Attr type:	Scalar
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_AttributeNokNumber(Tango::Attribute &attr)
{
	DEBUG_STREAM << "HdbEventSubscriber::read_AttributeNokNumber(Tango::Attribute &attr) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_AttributeNokNumber) ENABLED START -----*/

	//	Set the attribute value
	if (initialized)
	{
		*attr_AttributeNokNumber_read = hdb_dev->get_sig_on_error_num();
	}
	else
		*attr_AttributeNokNumber_read = 0;
	attr.set_value(attr_AttributeNokNumber_read);

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_AttributeNokNumber
}
//--------------------------------------------------------
/**
 *	Read attribute AttributePendingNumber related method
 *	Description: Number of attributes waiting to be archived
 *
 *	Data type:	Tango::DevLong
 *	Attr type:	Scalar
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_AttributePendingNumber(Tango::Attribute &attr)
{
	DEBUG_STREAM << "HdbEventSubscriber::read_AttributePendingNumber(Tango::Attribute &attr) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_AttributePendingNumber) ENABLED START -----*/

	//	Set the attribute value
	if (initialized)
	{
		//	Get the load
		//*attr_AttributePendingNumber_read = hdb_dev->get_max_waiting();
		*attr_AttributePendingNumber_read = hdb_dev->nb_cmd_waiting();
		//cout << "-------> hdb_dev->get_max_waiting()" << endl;
	}
	else
		*attr_AttributePendingNumber_read = 0;
	attr.set_value(attr_AttributePendingNumber_read);

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_AttributePendingNumber
}
//--------------------------------------------------------
/**
 *	Read attribute AttributeNumber related method
 *	Description: Number of configured attributes
 *
 *	Data type:	Tango::DevLong
 *	Attr type:	Scalar
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_AttributeNumber(Tango::Attribute &attr)
{
	DEBUG_STREAM << "HdbEventSubscriber::read_AttributeNumber(Tango::Attribute &attr) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_AttributeNumber) ENABLED START -----*/

	//	Set the attribute value
	if (initialized)
	{
		vector<string> tmp = hdb_dev->get_sig_list();
		*attr_AttributeNumber_read = tmp.size();
	}
	else
		*attr_AttributeNumber_read = 0;

	attr.set_value(attr_AttributeNumber_read);

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_AttributeNumber
}
//--------------------------------------------------------
/**
 *	Read attribute AttributeList related method
 *	Description: Returns the configured attribute list
 *
 *	Data type:	Tango::DevString
 *	Attr type:	Spectrum max = 10000
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_AttributeList(Tango::Attribute &attr)
{
	DEBUG_STREAM << "HdbEventSubscriber::read_AttributeList(Tango::Attribute &attr) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_AttributeList) ENABLED START -----*/

	//	Set the attribute value
	attribute_list_str = hdb_dev->get_sig_list();
	if(attr_AttributeList_read != NULL)
		delete [] attr_AttributeList_read;
	attr_AttributeList_read = new Tango::DevString[attribute_list_str.size()];
	for (unsigned int i=0 ; i<attribute_list_str.size() ; i++)
		attr_AttributeList_read[i] = (char *)attribute_list_str[i].c_str();

	attr.set_value(attr_AttributeList_read, attribute_list_str.size());

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_AttributeList
}
//--------------------------------------------------------
/**
 *	Read attribute AttributeOkList related method
 *	Description: Returns the attributes not on error list
 *
 *	Data type:	Tango::DevString
 *	Attr type:	Spectrum max = 10000
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_AttributeOkList(Tango::Attribute &attr)
{
	DEBUG_STREAM << "HdbEventSubscriber::read_AttributeOkList(Tango::Attribute &attr) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_AttributeOkList) ENABLED START -----*/

	//	Set the attribute value
	attribute_ok_list_str = hdb_dev->get_sig_not_on_error_list();

	if(attr_AttributeOkList_read != NULL)
		delete [] attr_AttributeOkList_read;
	attr_AttributeOkList_read = new Tango::DevString[attribute_ok_list_str.size()];
	for (unsigned int i=0 ; i<attribute_ok_list_str.size() ; i++)
		attr_AttributeOkList_read[i] = (char *)attribute_ok_list_str[i].c_str();

	attr.set_value(attr_AttributeOkList_read, attribute_ok_list_str.size());

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_AttributeOkList
}
//--------------------------------------------------------
/**
 *	Read attribute AttributeNokList related method
 *	Description: Returns the attributes on error list
 *
 *	Data type:	Tango::DevString
 *	Attr type:	Spectrum max = 10000
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_AttributeNokList(Tango::Attribute &attr)
{
	DEBUG_STREAM << "HdbEventSubscriber::read_AttributeNokList(Tango::Attribute &attr) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_AttributeNokList) ENABLED START -----*/

	//	Set the attribute value
	attribute_nok_list_str = hdb_dev->get_sig_on_error_list();

	if(attr_AttributeNokList_read != NULL)
		delete [] attr_AttributeNokList_read;
	attr_AttributeNokList_read = new Tango::DevString[attribute_nok_list_str.size()];
	for (unsigned int i=0 ; i<attribute_nok_list_str.size() ; i++)
		attr_AttributeNokList_read[i] = (char *)attribute_nok_list_str[i].c_str();

	attr.set_value(attr_AttributeNokList_read, attribute_nok_list_str.size());

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_AttributeNokList
}
//--------------------------------------------------------
/**
 *	Read attribute AttributePendingList related method
 *	Description: Returns the attributes waiting to be archived
 *
 *	Data type:	Tango::DevString
 *	Attr type:	Spectrum max = 10000
 */
//--------------------------------------------------------
void HdbEventSubscriber::read_AttributePendingList(Tango::Attribute &attr)
{
	DEBUG_STREAM << "HdbEventSubscriber::read_AttributePendingList(Tango::Attribute &attr) entering... " << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::read_AttributePendingList) ENABLED START -----*/

	//	Set the attribute value
	attribute_pending_list_str = hdb_dev->get_sig_list_waiting();

	if(attr_AttributePendingList_read != NULL)
		delete [] attr_AttributePendingList_read;
	attr_AttributePendingList_read = new Tango::DevString[attribute_pending_list_str.size()];
	for (unsigned int i=0 ; i<attribute_pending_list_str.size() ; i++)
		attr_AttributePendingList_read[i] = (char *)attribute_pending_list_str[i].c_str();

	attr.set_value(attr_AttributePendingList_read, attribute_pending_list_str.size());

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::read_AttributePendingList
}

//--------------------------------------------------------
/**
 *	Method      : HdbEventSubscriber::add_dynamic_attributes()
 *	Description : Create the dynamic attributes if any
 *                for specified device.
 */
//--------------------------------------------------------
void HdbEventSubscriber::add_dynamic_attributes()
{
	/*----- PROTECTED REGION ID(HdbEventSubscriber::add_dynamic_attributes) ENABLED START -----*/

	//	Add your own code to create and add dynamic attributes if any

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::add_dynamic_attributes
}

//--------------------------------------------------------
/**
 *	Command AttributeAdd related method
 *	Description: Add a new attribute to archive in HDB.
 *
 *	@param argin Attribute name
 */
//--------------------------------------------------------
void HdbEventSubscriber::attribute_add(Tango::DevString argin)
{
	DEBUG_STREAM << "HdbEventSubscriber::AttributeAdd()  - " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::attribute_add) ENABLED START -----*/

	//	Add your own code
	string	signame(argin);
	hdb_dev->add(signame);


	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::attribute_add
}
//--------------------------------------------------------
/**
 *	Command AttributeRemove related method
 *	Description: Remove attribute from configuration.
 *
 *	@param argin Attribute name
 */
//--------------------------------------------------------
void HdbEventSubscriber::attribute_remove(Tango::DevString argin)
{
	DEBUG_STREAM << "HdbEventSubscriber::AttributeRemove()  - " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::attribute_remove) ENABLED START -----*/

	//	Add your own code
	string	signame(argin);
	hdb_dev->fix_tango_host(signame);

	if(hdb_dev->shared->is_running(signame))
	{
		hdb_dev->shared->stop(signame);
		hdb_dev->push_shared->stop_attr(signame);
	}
	hdb_dev->remove(signame);

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::attribute_remove
}
//--------------------------------------------------------
/**
 *	Command AttributeStatus related method
 *	Description: Read a attribute status.
 *
 *	@param argin The attribute name
 *	@returns The attribute status. TODO: DevString OK?
 */
//--------------------------------------------------------
Tango::DevString HdbEventSubscriber::attribute_status(Tango::DevString argin)
{
	Tango::DevString argout;
	DEBUG_STREAM << "HdbEventSubscriber::AttributeStatus()  - " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::attribute_status) ENABLED START -----*/

	//	Add your own code
	string	signame(argin);
	hdb_dev->fix_tango_host(signame);

	stringstream attr_status;
	attr_status << "Event status       : "<<hdb_dev->get_sig_status(signame);
	attr_status << endl;
	attr_status << "Events engine      : "<<(hdb_dev->shared->get_sig_source(signame) ? "ZMQ" : "Notifd");
	attr_status << endl;
	hdb_dev->shared->lock();
	attr_status << "Archiving          : "<<(hdb_dev->shared->is_running(signame) ? "Started" : "Stopped");
	hdb_dev->shared->unlock();
	attr_status << endl;
	attr_status << "Event OK counter   : "<<hdb_dev->shared->get_ok_event(signame);
	attr_status << endl;
	attr_status << "Event NOK counter  : "<<hdb_dev->shared->get_nok_event(signame);
	attr_status << endl;
	attr_status << "DB ERRORS counter  : "<<hdb_dev->push_shared->get_nok_db(signame);
	attr_status << endl;
	attr_status << "Store time AVG     : "<<fixed<<hdb_dev->push_shared->get_avg_store_time(signame)<<"s";
	attr_status << endl;
	attr_status << "Processing time AVG: "<<fixed<<hdb_dev->push_shared->get_avg_process_time(signame)<<"s";
	argout  = new char[attr_status.str().length()+1];
	strcpy(argout, attr_status.str().c_str());

/*
	vector<string>	names   = hdb_dev->shared->get_sig_list();
	vector<bool>	sources = hdb_dev->shared->get_sig_source_list();	//1=ZMQ, 0=notifd
	argout = new Tango::DevVarLongStringArray();
	argout->svalue.length(names.size());
	argout->lvalue.length(names.size());
	for (unsigned int i=0 ; i<names.size() ; i++)
	{
		argout->svalue[i] = CORBA::string_dup(names[i].c_str());
		argout->lvalue[i] = sources[i];
	}
 */

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::attribute_status
	return argout;
}
//--------------------------------------------------------
/**
 *	Command Start related method
 *	Description: Start archiving
 *
 */
//--------------------------------------------------------
void HdbEventSubscriber::start()
{
	DEBUG_STREAM << "HdbEventSubscriber::Start()  - " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::start) ENABLED START -----*/

	//	Add your own code
	hdb_dev->push_shared->start_all();
	hdb_dev->shared->start_all();
	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::start
}
//--------------------------------------------------------
/**
 *	Command Stop related method
 *	Description: Stop archiving
 *
 */
//--------------------------------------------------------
void HdbEventSubscriber::stop()
{
	DEBUG_STREAM << "HdbEventSubscriber::Stop()  - " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::stop) ENABLED START -----*/

	//	Add your own code
	hdb_dev->shared->stop_all();
	hdb_dev->push_shared->stop_all();
	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::stop
}
//--------------------------------------------------------
/**
 *	Command AttributeStart related method
 *	Description: Start archiving single attribute
 *
 *	@param argin Attribute name
 */
//--------------------------------------------------------
void HdbEventSubscriber::attribute_start(Tango::DevString argin)
{
	DEBUG_STREAM << "HdbEventSubscriber::AttributeStart()  - " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::attribute_start) ENABLED START -----*/

	//	Add your own code

	string	signame(argin);
	hdb_dev->fix_tango_host(signame);

	hdb_dev->push_shared->start_attr(signame);
	hdb_dev->shared->start(signame);

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::attribute_start
}
//--------------------------------------------------------
/**
 *	Command AttributeStop related method
 *	Description: Stop archiving single attribute
 *
 *	@param argin Attribute name
 */
//--------------------------------------------------------
void HdbEventSubscriber::attribute_stop(Tango::DevString argin)
{
	DEBUG_STREAM << "HdbEventSubscriber::AttributeStop()  - " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::attribute_stop) ENABLED START -----*/

	//	Add your own code

	string	signame(argin);
	hdb_dev->fix_tango_host(signame);

	hdb_dev->shared->stop(signame);
	hdb_dev->push_shared->stop_attr(signame);

	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::attribute_stop
}
//--------------------------------------------------------
/**
 *	Command ResetStatistics related method
 *	Description: Reset statistic counters
 *
 */
//--------------------------------------------------------
void HdbEventSubscriber::reset_statistics()
{
	DEBUG_STREAM << "HdbEventSubscriber::ResetStatistics()  - " << device_name << endl;
	/*----- PROTECTED REGION ID(HdbEventSubscriber::reset_statistics) ENABLED START -----*/
	
	//	Add your own code
	hdb_dev->reset_statistics();
	
	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::reset_statistics
}

/*----- PROTECTED REGION ID(HdbEventSubscriber::namespace_ending) ENABLED START -----*/

	//	Additional Methods


	/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::namespace_ending
} //	namespace
