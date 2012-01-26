/**
 * Copyright 2009 Friedrich Schäuffelhut
 * Copyright 2010 Christophe Vandeplas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package de.schaeuffelhut.android.openvpn;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import de.schaeuffelhut.android.openvpn.tun.ShareTunActivity;

public final class Notifications {

	private Notifications(){}
	
	private static final int SHARE_TUN_ID = 1000;
	public static final int FIRST_CONFIG_ID = 1000000;

	public static void notifyConnected(int id, Context context, NotificationManager notificationManager, File configFile) 
	{
		notifyConnected(id, context, notificationManager, configFile, null);
	}
	
	public static void notifyConnected(int id, Context context, NotificationManager notificationManager, File configFile, String msg) {
		Notification notification = new Notification(
				R.drawable.vpn_connected,
				Preferences.getConfigName(context, configFile) + ": Connected",
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
	
	public static void notifyBytes(int id, Context context, NotificationManager notificationManager, File configFile, String msg) {
		// To update latestEventInfo only, exactly the same notification type must be used. 
		// Otherwise the user will get permanent notifications in his title-bar
		notifyConnected(id, context, notificationManager, configFile, msg);
	}

	public static void notifyDisconnected(int id, Context context, NotificationManager notificationManager, File configFile, String msg) {
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
	
	public static void sendPassphraseRequired(int id, Context context, NotificationManager notificationManager, File configFile) {
		Notification notification = new Notification(
				R.drawable.vpn_disconnected_attention,
				"Passphrase required",
				System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		Intent intent = new Intent(null, Uri.fromFile(configFile), context, EnterPassphrase.class );
		
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
	}
	
	public static void sendUsernamePasswordRequired(int id, Context context, File configFile, NotificationManager notificationManager) {
		Notification notification = new Notification(
				R.drawable.vpn_disconnected_attention,
				"Username/Password required",
				System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		Intent intent = new Intent(null, Uri.fromFile(configFile), context, EnterUserPassword.class );
		
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
	}
	
	public static void cancel(int id, Context context)
	{
		getNotificationManager(context).cancel( id );
	}
	
	private static NotificationManager getNotificationManager(Context context) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		return notificationManager;
	}
	
	
	public static void sendShareTunModule(Context context, NotificationManager notificationManager) {
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

	public static void cancelShareTunModule(Context context) {
		cancel( SHARE_TUN_ID, context);
	}
}
