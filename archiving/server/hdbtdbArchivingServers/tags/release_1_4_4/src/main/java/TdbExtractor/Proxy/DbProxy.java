//+======================================================================
// $Source$
//
// Project:      Tango Archiving Service
//
// Description:  Java source code for the class  DbProxy.
//						(Chinkumo Jean) - 1 f�vr. 2005
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 1.5  2007/03/05 16:25:20  ounsy
// non-static DataBase
//
// Revision 1.4  2007/02/01 13:50:18  pierrejoseph
// ArchivingConfigureWithoutArchiverListInit instead of ArchivingConfigure
//
// Revision 1.3  2005/11/29 17:32:48  chinkumo
// no message
//
// Revision 1.2.10.2  2005/11/29 16:13:31  chinkumo
// Code reformated (pogo compatible)
//
// Revision 1.2.10.1  2005/11/15 13:45:16  chinkumo
// ...
//
// Revision 1.2  2005/06/14 10:43:12  chinkumo
// Branch (tdbExtractor_1_0_1-branch_0)  and HEAD merged.
//
// Revision 1.1.4.1  2005/06/13 13:28:02  chinkumo
// Changes made to improve the management of exceptions were reported here.
//
// Revision 1.1  2005/02/01 12:39:40  chinkumo
// This class replaces the TdbProxy class.
//
//
// copyleft :	Synchrotron SOLEIL
//					L'Orme des Merisiers
//					Saint-Aubin - BP 48
//					91192 GIF-sur-YVETTE CEDEX
//
//-======================================================================
package TdbExtractor.Proxy;

import fr.soleil.hdbtdbArchivingApi.ArchivingApi.DataBaseApi;
import fr.soleil.hdbtdbArchivingApi.ArchivingManagerApi.ArchivingManagerApi;
import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.ArchivingException;

public class DbProxy
{
    private ArchivingManagerApi manager;

    public DbProxy()
	{
		try
		{
            this.manager = new ArchivingManagerApi ();
            this.manager.ArchivingConfigureWithoutArchiverListInit(TdbExtractor.TdbExtractor.myDbUser , TdbExtractor.TdbExtractor.myDbPassword , false);
		}
		catch ( ArchivingException e )
		{
			System.err.println(e.toString());
		}
	}

	public boolean is_db_connected()
	{
		return this.manager.is_db_connected(false);
	}

	public DataBaseApi getDataBase()
	{
		return this.manager.getDataBase(false);
	}

    public ArchivingManagerApi getManager() 
    {
        return this.manager;
    }
}