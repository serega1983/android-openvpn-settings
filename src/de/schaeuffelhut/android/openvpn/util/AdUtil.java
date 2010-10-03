package de.schaeuffelhut.android.openvpn.util;

import android.content.Context;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.R;

public class AdUtil {
	private AdUtil() {
	}

	private static Boolean sHasAdMobSupport = null;
	public static boolean hasAdSupport()
	{
		if ( sHasAdMobSupport == null )
		{
			try {
				Class.forName( "com.admob.android.ads.AdView" );
				sHasAdMobSupport = true;
			} catch (ClassNotFoundException e) {
				sHasAdMobSupport = false;
			}
		}
		return sHasAdMobSupport.booleanValue();
	}
	
	public static final int getAdSupportedListView(Context context)
	{
		return hasAdSupport() && Preferences.getShowAds(context) ? R.layout.listview_with_ad : R.layout.listview ;
	}

}
