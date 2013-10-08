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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnDaemonState;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnNetworkState;
import de.schaeuffelhut.android.openvpn.shared.util.NotificationUtil;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-01
 */
public class Notification
{
    public static final ComponentName DEFAULT_ACTIVITY_FOR_PASSPHRASE_REQUEST = new ComponentName( "de.schaeuffelhut.android.openvpn", "de.schaeuffelhut.android.openvpn.EnterPassphrase" );
    public static final ComponentName DEFAULT_ACTIVITY_FOR_CREDENTIALS_REQUEST = new ComponentName( "de.schaeuffelhut.android.openvpn", "de.schaeuffelhut.android.openvpn.EnterUserPassword" );
    private static final String NO_MESSAGE = null;
    private final Context mContext;
    private final File mConfigFile;
    private final int mNotificationId;
    private final NotificationManager mNotificationManager;
    private final Handler mUiThreadHandler;
    private final OpenVpnStateListenerDispatcher listenerDispatcher;
    private final ComponentName activityForPassphraseRequest;
    private final ComponentName activityForCredentialsRequest;
    private final ComponentName activityForOngoingNotification;

    public Notification(
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
        android.app.Notification notification = new android.app.Notification(
                R.drawable.vpn_disconnected_attention,
                "Passphrase required",
                System.currentTimeMillis()
        );
        notification.flags |= android.app.Notification.FLAG_NO_CLEAR;
        notification.flags |= android.app.Notification.FLAG_ONGOING_EVENT;


        Intent intent = new Intent(null, Uri.fromFile( mConfigFile )).setComponent( activityForPassphraseRequest );

        notification.setLatestEventInfo(
                mContext,
                "Passphrase required",
                String.format( "for configuration %s", Preferences.getConfigName( mContext, mConfigFile ) ),
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        0
                )
        );

        mNotificationManager.notify( mNotificationId, notification );

        sendNeedPassword( intent );
        listenerDispatcher.onRequestPassphrase();
    }

    void sendUsernamePasswordRequired()
    {
        android.app.Notification notification = new android.app.Notification(
                R.drawable.vpn_disconnected_attention,
                "Username/Password required",
                System.currentTimeMillis()
        );
        notification.flags |= android.app.Notification.FLAG_NO_CLEAR;
        notification.flags |= android.app.Notification.FLAG_ONGOING_EVENT;

        Intent intent = new Intent(null, Uri.fromFile( mConfigFile )). setComponent( activityForCredentialsRequest );

        notification.setLatestEventInfo(
                mContext,
                "Username/Password required",
                String.format( "for configuration %s", Preferences.getConfigName( mContext, mConfigFile ) ),
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        0
                )
        );

        mNotificationManager.notify( mNotificationId, notification );

        sendNeedPassword( intent );
        listenerDispatcher.onRequestCredentials();
    }

    private void sendNeedPassword(Intent intent)
    {
        Intent needPassword = new Intent( Intents.BROADCAST_NEED_PASSWORD );
        //needPassword.setPackage( Intents.NS ); //TODO: uncomment once minSdkVersion > 3
        needPassword.putExtra( "ACTION", intent );
        mContext.sendBroadcast( needPassword );
    }


    void notifyConnected()
    {
        notifyConnected( NO_MESSAGE );
    }

    private void notifyConnected(String msg)
    {
        android.app.Notification notification = new android.app.Notification(
                R.drawable.vpn_connected,
                Preferences.getConfigName( mContext, mConfigFile ) + ": Connected",
                System.currentTimeMillis()
        );
        notification.flags |= android.app.Notification.FLAG_NO_CLEAR;
        notification.flags |= android.app.Notification.FLAG_ONGOING_EVENT;
        notification.flags |= android.app.Notification.FLAG_ONLY_ALERT_ONCE;

        Intent intent = new Intent().setComponent( activityForOngoingNotification );

        notification.setLatestEventInfo(
                mContext,
                "OpenVPN, " + Preferences.getConfigName( mContext, mConfigFile ),
                TextUtils.isEmpty( msg ) ? "Connected" : msg,
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        0
                )
        );

        mNotificationManager.notify( mNotificationId, notification );
    }

    void notifyDisconnected()
    {
        android.app.Notification notification = new android.app.Notification(
                R.drawable.vpn_disconnected,
                Preferences.getConfigName( mContext, mConfigFile ) +": " + "Connecting",
                System.currentTimeMillis()
        );
        notification.flags |= android.app.Notification.FLAG_NO_CLEAR;
        notification.flags |= android.app.Notification.FLAG_ONGOING_EVENT;

        Intent intent = new Intent().setComponent( activityForOngoingNotification );
//		intent.putExtra( EnterPassphrase.EXTRA_FILENAME, configFile.getAbsolutePath() );

        notification.setLatestEventInfo(
                mContext,
                "OpenVPN, " + Preferences.getConfigName( mContext, mConfigFile ),
                "Connecting",
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        0
                )
        );

        mNotificationManager.notify( mNotificationId, notification );
    }

    void notifyBytes(String smallInOutPerSecString, long received, long sent)
    {
        // To update latestEventInfo only, exactly the same notification type must be used.
        // Otherwise the user will get permanent notifications in his title-bar
        notifyConnected( smallInOutPerSecString );

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
