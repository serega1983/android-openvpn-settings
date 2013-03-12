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

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnDaemonState;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnNetworkState;
import de.schaeuffelhut.android.openvpn.shared.util.NotificationUtil;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-01
 */
public class Notification2
{
    public static final ComponentName DEFAULT_ACTIVITY_FOR_PASSPHRASE_REQUEST = new ComponentName( "de.schaeuffelhut.android.openvpn", "de.schaeuffelhut.android.openvpn.EnterPassphrase" );
    public static final ComponentName DEFAULT_ACTIVITY_FOR_CREDENTIALS_REQUEST = new ComponentName( "de.schaeuffelhut.android.openvpn", "de.schaeuffelhut.android.openvpn.EnterUserPassword" );
    private final Context mContext;
    private final File mConfigFile;
    private final int mNotificationId;
    private final NotificationManager mNotificationManager;
    private final Handler mUiThreadHandler;
    private final OpenVpnStateListenerDispatcher listenerDispatcher;
    private final ComponentName activityForPassphraseRequest;
    private final ComponentName activityForCredentialsRequest;
    private final ComponentName activityForOngoingNotification;

    public Notification2(
            Context context, File configFile, int notificationId,
            OpenVpnStateListenerDispatcher listenerDispatcher
    ) {
        this.mContext = context;
        this.mConfigFile = configFile;
        this.mNotificationId = notificationId;
        this.mNotificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE);
        this.mUiThreadHandler = new Handler();
        this.listenerDispatcher = listenerDispatcher;

        PluginPreferences pluginPreferences = new PluginPreferences( context, "default" );
        this.activityForPassphraseRequest   = pluginPreferences.getActivityHandlingPassphraseRequest();
        this.activityForCredentialsRequest  = pluginPreferences.getActivityHandlingCredentialsRequest();
        this.activityForOngoingNotification = pluginPreferences.getActivityHandlingOngoingNotification();
    }


    public void daemonStateChangedToStartUp()
    {
        mContext.sendStickyBroadcast(
                Intents.daemonStateChanged(
                        mConfigFile.getAbsolutePath(),
                        Intents.DAEMON_STATE_STARTUP
                )
        );
        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.STARTUP );
    }

    void daemonStateChangedToEnabled()
    {
        mContext.sendStickyBroadcast(
                Intents.daemonStateChanged(
                        mConfigFile.getAbsolutePath(),
                        Intents.DAEMON_STATE_ENABLED
                )
        );
        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
    }

    void daemonStateChangedToDisabled()
    {
        mContext.sendStickyBroadcast(
                Intents.daemonStateChanged(
                        mConfigFile.getAbsolutePath(),
                        Intents.DAEMON_STATE_DISABLED
                )
        );
        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.DISABLED );
    }

    void sendPassphraseRequired()
    {
        Notifications.sendPassphraseRequired( mNotificationId, mContext, mNotificationManager, mConfigFile, activityForPassphraseRequest );
        listenerDispatcher.onRequestPassphrase();
    }

    void sendUsernamePasswordRequired()
    {
        Notifications.sendUsernamePasswordRequired( mNotificationId, mContext, mConfigFile, mNotificationManager, activityForCredentialsRequest );
        listenerDispatcher.onRequestCredentials();
    }

    void notifyConnected()
    {
        Notifications.notifyConnected( mNotificationId, mContext, mNotificationManager, mConfigFile, activityForOngoingNotification );
    }

    void notifyDisconnected()
    {
        Notifications.notifyDisconnected( mNotificationId, mContext, mNotificationManager, mConfigFile, "Connecting", activityForOngoingNotification );
    }

    void notifyBytes(String smallInOutPerSecString, long received, long sent)
    {
        Notifications.notifyBytes( mNotificationId, mContext, mNotificationManager, mConfigFile, smallInOutPerSecString, activityForOngoingNotification );
        listenerDispatcher.onByteCountChanged( received, sent ); //TODO: insert real byte count
    }

    void cancel()
    {
        NotificationUtil.cancel( mNotificationId, mContext );
    }

    void networkStateChanged(int oldState, int newState)
    {
        networkStateChanged( oldState, newState, System.currentTimeMillis(), null, null, null, null, null, null );
    }

    //TODO: change method signature to take a Bundle instead of nullable parameters
    void networkStateChanged(int oldState, int newState, long time, String info0ExtraName, String info0ExtraValue, String info1ExtraName, String info1ExtraValue, String info2ExtraName, String info2ExtraValue)
    {
        Intent intent = Intents.networkStateChanged(
                mConfigFile.getAbsolutePath(),
                newState,
                oldState,
                time
        );

        if (info0ExtraName != null)
            intent.putExtra( info0ExtraName, info0ExtraValue );
        if (info1ExtraName != null)
            intent.putExtra( info1ExtraName, info1ExtraValue );
        if (info2ExtraName != null)
            intent.putExtra( info2ExtraName, info2ExtraValue );

        mContext.sendStickyBroadcast( intent );

        String cause = Intents.EXTRA_NETWORK_CAUSE.equals( info0ExtraName ) ? info0ExtraValue : "";
        String localIp = Intents.EXTRA_NETWORK_LOCALIP.equals( info1ExtraName ) ? info1ExtraValue : "";
        String remoteIp = Intents.EXTRA_NETWORK_REMOTEIP.equals( info2ExtraName ) ? info2ExtraValue : "";
        listenerDispatcher.onNetworkStateChanged(
                OpenVpnNetworkState.values()[oldState],
                OpenVpnNetworkState.values()[newState],
                System.currentTimeMillis(),
                cause, localIp, remoteIp
        );
    }

    void toastMessage(final String message)
    {
        mUiThreadHandler.post( new Runnable()
        {
            public void run()
            {
                Toast.makeText( mContext, message, Toast.LENGTH_LONG ).show();
            }
        } );
    }
}
