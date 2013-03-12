package de.schaeuffelhut.android.openvpn.tun;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.shared.util.NotificationUtil;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class TunNotification
{
    private static final int SHARE_TUN_ID = 1000;

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
        NotificationUtil.cancel( SHARE_TUN_ID, context );
    }
}
