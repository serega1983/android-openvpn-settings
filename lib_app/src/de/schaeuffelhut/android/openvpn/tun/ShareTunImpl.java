package de.schaeuffelhut.android.openvpn.tun;

import android.app.NotificationManager;
import android.content.Context;
import de.schaeuffelhut.android.openvpn.lib.service.impl.ShareTun;
import de.schaeuffelhut.android.openvpn.util.tun.TunPreferences;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class ShareTunImpl implements ShareTun
{
    private final Context mContext;
    private final NotificationManager mNotificationManager;

    public ShareTunImpl(Context context)
    {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void shareTun()
    {
        if (TunPreferences.isTunSharingExpired())
            return;

        if (TunPreferences.getSendDeviceDetailWasSuccessfull( mContext ))
            return;

        TunNotification.sendShareTunModule( mContext, mNotificationManager );
    }
}
