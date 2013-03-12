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
package de.schaeuffelhut.android.openvpn.lib.service.impl;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.lib.service.impl.R;

final class Notifications {

    private Notifications(){}

    static void notifyConnected(int id, Context context, NotificationManager notificationManager, File configFile, ComponentName activityForOngoingNotification)
	{
		notifyConnected(id, context, notificationManager, configFile, null, activityForOngoingNotification );
	}
	
	static void notifyConnected(int id, Context context, NotificationManager notificationManager, File configFile, String msg, ComponentName activityForOngoingNotification) {
		Notification notification = new Notification(
				R.drawable.vpn_connected,
				Preferences.getConfigName( context, configFile ) + ": Connected",
				System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

		Intent intent = new Intent().setComponent( activityForOngoingNotification );
		
		notification.setLatestEventInfo(
				context,
				"OpenVPN, " + Preferences.getConfigName( context, configFile ),
				TextUtils.isEmpty( msg ) ? "Connected" : msg,
				PendingIntent.getActivity(
						context,
						0,
						intent,
						0
				)
		);
		
		notificationManager.notify( id, notification);
	}
	
	static void notifyBytes(int id, Context context, NotificationManager notificationManager, File configFile, String msg, ComponentName activityForOngoingNotification) {
		// To update latestEventInfo only, exactly the same notification type must be used. 
		// Otherwise the user will get permanent notifications in his title-bar
		notifyConnected(id, context, notificationManager, configFile, msg, activityForOngoingNotification );
	}

	static void notifyDisconnected(int id, Context context, NotificationManager notificationManager, File configFile, String msg, ComponentName activityForOngoingNotification) {
		Notification notification = new Notification(
				R.drawable.vpn_disconnected,
				Preferences.getConfigName(context, configFile) +": " + msg,
				System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		Intent intent = new Intent().setComponent( activityForOngoingNotification );
//		intent.putExtra( EnterPassphrase.EXTRA_FILENAME, configFile.getAbsolutePath() );
		
		notification.setLatestEventInfo(
				context,
				"OpenVPN, " + Preferences.getConfigName(context, configFile),
				msg,
				PendingIntent.getActivity(
						context,
						0,
						intent,
						0
				)
		);
	
		notificationManager.notify( id, notification);
	}
	
	static void sendPassphraseRequired(int id, Context context, NotificationManager notificationManager, File configFile, ComponentName component) {
		Notification notification = new Notification(
				R.drawable.vpn_disconnected_attention,
				"Passphrase required",
				System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;


		Intent intent = new Intent(null, Uri.fromFile(configFile)).setComponent( component );
		
		notification.setLatestEventInfo(
				context,
				"Passphrase required",
				String.format( "for configuration %s", Preferences.getConfigName(context, configFile) ),
				PendingIntent.getActivity(
						context,
						0,
						intent,
						0
				)
		);
	
		notificationManager.notify( id, notification);

        sendNeedPassword( context, intent );
    }

    static void sendUsernamePasswordRequired(int id, Context context, File configFile, NotificationManager notificationManager, ComponentName component) {
		Notification notification = new Notification(
				R.drawable.vpn_disconnected_attention,
				"Username/Password required",
				System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		Intent intent = new Intent(null, Uri.fromFile(configFile)). setComponent( component );

		notification.setLatestEventInfo(
				context,
				"Username/Password required",
				String.format( "for configuration %s", Preferences.getConfigName(context, configFile) ),
				PendingIntent.getActivity(
						context,
						0,
						intent,
						0
				)
		);
	
		notificationManager.notify( id, notification);

        sendNeedPassword( context, intent );
	}


    private static void sendNeedPassword(Context context, Parcelable intent)
    {
        Intent needPassword = new Intent( Intents.BROADCAST_NEED_PASSWORD );
        //needPassword.setPackage( Intents.NS ); //TODO: uncomment once minSdkVersion > 3
        needPassword.putExtra( "ACTION", intent );
        context.sendBroadcast( needPassword );
    }
}
