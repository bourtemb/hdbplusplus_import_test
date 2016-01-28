/*----- PROTECTED REGION ID(HdbEventSubscriber::ClassFactory.cpp) ENABLED START -----*/
static const char *RcsId = "$Id: ClassFactory.cpp,v 1.2 2014-02-20 14:57:06 graziano Exp $";
//=============================================================================
//
// file :        ClassFactory.cpp
//
// description : C++ source for the class_factory method of the DServer
//               device class. This method is responsible for the creation of
//               all class singleton for a device server. It is called
//               at device server startup.
//
// project :     Tango Device Server.
//
// $Author: graziano $
//
// $Revision: 1.2 $
// $Date: 2014-02-20 14:57:06 $
//
// SVN only:
// $HeadURL$
//
// CVS only:
// $Source: /home/cvsadm/cvsroot/fermi/servers/hdb++/hdb++es/src/ClassFactory.cpp,v $
// $Log: ClassFactory.cpp,v $
// Revision 1.2  2014-02-20 14:57:06  graziano
// name and path fixing
//
// Revision 1.1  2013-07-17 13:37:43  graziano
// *** empty log message ***
//
//
//=============================================================================
//                This file is generated by POGO
//        (Program Obviously used to Generate tango Object)
//=============================================================================


#include <tango.h>
#include <HdbEventSubscriberClass.h>

//	Add class header files if needed


/**
 *	Create HdbEventSubscriber Class singleton and store it in DServer object.
 */

void Tango::DServer::class_factory()
{
	//	Add method class init if needed
	
	add_class(HdbEventSubscriber_ns::HdbEventSubscriberClass::init("HdbEventSubscriber"));
}

/*----- PROTECTED REGION END -----*/	//	HdbEventSubscriber::ClassFactory.cpp
