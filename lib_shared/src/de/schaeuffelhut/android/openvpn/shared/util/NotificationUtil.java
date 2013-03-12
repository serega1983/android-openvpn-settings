package de.schaeuffelhut.android.openvpn.shared.util;

import android.app.NotificationManager;
import android.content.Context;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class NotificationUtil
{
    public static void cancel(int id, Context context)
	{
		getNotificationManager(context).cancel( id );
	}

    private static NotificationManager getNotificationManager(Context context) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		return notificationManager;
	}
}
