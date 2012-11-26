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
package de.schaeuffelhut.android.openvpn.service;

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
import de.schaeuffelhut.android.openvpn.*;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.tun.ShareTunActivity;

final class Notifications {

    private Notifications(){}
	
	private static final int SHARE_TUN_ID = 1000;

    static void notifyConnected(int id, Context context, NotificationManager notificationManager, File configFile)
	{
		notifyConnected(id, context, notificationManager, configFile, null);
	}
	
	static void notifyConnected(int id, Context context, NotificationManager notificationManager, File configFile, String msg) {
		Notification notification = new Notification(
				R.drawable.vpn_connected,
				Preferences.getConfigName( context, configFile ) + ": Connected",
				System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
		
		Intent intent = new Intent(context, OpenVpnSettings.class );
		
		notification.setLatestEventInfo(
				context,
				"OpenVPN, " + Preferences.getConfigName(context, configFile),
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
	
	static void notifyBytes(int id, Context context, NotificationManager notificationManager, File configFile, String msg) {
		// To update latestEventInfo only, exactly the same notification type must be used. 
		// Otherwise the user will get permanent notifications in his title-bar
		notifyConnected(id, context, notificationManager, configFile, msg);
	}

	static void notifyDisconnected(int id, Context context, NotificationManager notificationManager, File configFile, String msg) {
		Notification notification = new Notification(
				R.drawable.vpn_disconnected,
				Preferences.getConfigName(context, configFile) +": " + msg,
				System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		Intent intent = new Intent(context, OpenVpnSettings.class );
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

    static void cancel(int id, Context context)
	{
		getNotificationManager(context).cancel( id );
	}
	
	private static NotificationManager getNotificationManager(Context context) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		return notificationManager;
	}
	
	
	static void sendShareTunModule(Context context, NotificationManager notificationManager) {
		Notification notification = new Notification(
				R.drawable.ic_share_tun,
				"Please share your tun module",
				System.currentTimeMillis()
		);
//		notification.flags |= Notification.FLAG_NO_CLEAR;
//		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		Intent intent = new Intent(context, ShareTunActivity.class ); //TODO: put tun sharing activity here
//		intent.putExtra( EnterPassphrase.EXTRA_FILENAME, configFile.getAbsolutePath() );
		
		notification.setLatestEventInfo(
				context,
				"Help to improve OpenVPN Settings",
				"Please share your tun module",
				PendingIntent.getActivity(
						context,
						0,
						intent,
						0
				)
		);
		
		notificationManager.notify( SHARE_TUN_ID, notification);
	}

	static void cancelShareTunModule(Context context) {
		cancel( SHARE_TUN_ID, context);
	}
}
