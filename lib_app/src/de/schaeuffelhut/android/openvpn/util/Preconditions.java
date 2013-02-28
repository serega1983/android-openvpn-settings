/*
 * This file is part of OpenVPN-Settings.
 *
 * Copyright © 2009-2012  Friedrich Schäuffelhut
 *
 * OpenVPN-Settings is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenVPN-Settings is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenVPN-Settings.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Report bugs or new features at: http://code.google.com/p/android-openvpn-settings/
 * Contact the author at:          android.openvpn@schaeuffelhut.de
 */

package de.schaeuffelhut.android.openvpn.util;

import android.content.Context;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.shared.util.Shell;

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
