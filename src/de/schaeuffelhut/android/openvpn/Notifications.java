package de.schaeuffelhut.android.openvpn;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class Notifications {
	private Notifications(){}
	
	public static final int PASSPHRASE_REQUIRED = 1;
	public static final int USERNAME_PASSWORD_REQUIRED = 2;
	
	public static void sendPassphraseRequired(Context context, NotificationManager notificationManager, File configFile) {
		Notification notification = new Notification(
				R.drawable.vpn_disconnected_attention,
				"Passphrase required",
				System.currentTimeMillis()
		);
		
		Intent intent = new Intent(context, EnterPassphrase.class );
		intent.putExtra( EnterPassphrase.EXTRA_FILENAME, configFile.getAbsolutePath() );
		
		notification.setLatestEventInfo(
				context,
				"Passphrase required",
				String.format( "for configuration %s", configFile.getName() ),
				PendingIntent.getActivity(
						context,
						0,
						intent,
						0
				)
		);
	
		notificationManager.notify( PASSPHRASE_REQUIRED, notification);
	}

	public static void cancelPassphraseRequired(Context context)
	{
		getNotificationManager(context).cancel( PASSPHRASE_REQUIRED );
	}

	
	
	public static void sendUsernamePasswordRequired(Context context, File configFile, NotificationManager notificationManager) {
		Notification notification = new Notification(
				R.drawable.vpn_disconnected_attention,
				"Username/Password required",
				System.currentTimeMillis()
		);
		
		Intent intent = new Intent(context, EnterUserPassword.class );
		intent.putExtra( EnterUserPassword.EXTRA_FILENAME, configFile.getAbsolutePath() );
		
		notification.setLatestEventInfo(
				context,
				"Username/Password required",
				String.format( "for configuration %s", configFile.getName() ),
				PendingIntent.getActivity(
						context,
						0,
						intent,
						0
				)
		);
	
		notificationManager.notify( USERNAME_PASSWORD_REQUIRED, notification);
	}

	public static void cancelUsernamePasswordRequired(Context context) {
		getNotificationManager(context).cancel( USERNAME_PASSWORD_REQUIRED );
	}


	private static NotificationManager getNotificationManager(Context context) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		return notificationManager;
	}
}
