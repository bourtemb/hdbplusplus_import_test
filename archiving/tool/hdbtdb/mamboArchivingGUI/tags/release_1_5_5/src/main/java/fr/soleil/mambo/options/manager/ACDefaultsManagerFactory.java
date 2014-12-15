//+======================================================================
//$Source: /cvsroot/tango-cs/tango/tools/mambo/options/manager/ACDefaultsManagerFactory.java,v $
//
//Project:      Tango Archiving Service
//
//Description:  Java source code for the class  OptionsManagerFactory.
//						(Claisse Laurent) - 5 juil. 2005
//
//$Author: chinkumo $
//
//$Revision: 1.1 $
//
//$Log: ACDefaultsManagerFactory.java,v $
//Revision 1.1  2005/11/29 18:27:07  chinkumo
//no message
//
//Revision 1.1.2.3  2005/09/15 10:30:05  chinkumo
//Third commit !
//
//Revision 1.1.2.2  2005/09/14 15:41:44  chinkumo
//Second commit !
//
//
//copyleft :	Synchrotron SOLEIL
//					L'Orme des Merisiers
//					Saint-Aubin - BP 48
//					91192 GIF-sur-YVETTE CEDEX
//
//-======================================================================
package fr.soleil.mambo.options.manager;

public class ACDefaultsManagerFactory
{
    public static final int DUMMY_IMPL_TYPE = 1;
    public static final int PROPERTIES_IMPL_TYPE = 2;

    private static IACDefaultsManager currentImpl = null;

    /**
     * @param typeOfImpl
     * @return 8 juil. 2005
     */
    public static IACDefaultsManager getImpl ( int typeOfImpl )
    {
        switch ( typeOfImpl )
        {
            case DUMMY_IMPL_TYPE:
                currentImpl = new DummyACDefaultsManager();
                break;

            case PROPERTIES_IMPL_TYPE:
                currentImpl = new PropertiesACDefaultsManager();
                break;

            default:
                throw new IllegalStateException( "Expected either DUMMY_IMPL_TYPE (1) or XML_IMPL_TYPE (2), got " + typeOfImpl + " instead." );
        }

        return currentImpl;
    }

    /**
     * @return 28 juin 2005
     */
    public static IACDefaultsManager getCurrentImpl ()
    {
        return currentImpl;
    }
}