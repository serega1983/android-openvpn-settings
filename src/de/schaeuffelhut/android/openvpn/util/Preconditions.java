package de.schaeuffelhut.android.openvpn.util;

import android.content.Context;
import android.widget.Toast;

public class Preconditions {

	public static boolean check(Context context)
	{
		final boolean hasSu = !Shell.findBinary( "su" ).equals( "su" );
		
		final boolean ok = hasSu;
		if ( !ok ){
			Toast.makeText(context, "Some system requirements are not met! You need root!", Toast.LENGTH_LONG).show();
		}
		return ok;
	}

}
