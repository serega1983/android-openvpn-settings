package de.schaeuffelhut.android.openvpn.service.api;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public abstract class OpenVpnState implements Parcelable
{
    private static final byte TYPE_STARTED_VERSION_1 = (byte) 1;
    private static final byte TYPE_STOPPED_VERSION_1 = (byte) 2;
    private static final Stopped STOPPED_INSTANCE = new Stopped( OpenVpnDaemonState.DISABLED );

    private OpenVpnState()
    {
    }

    @Deprecated
    // TODO: either write test or remove this method
    public static OpenVpnState fromStickyBroadcast(Context context)
    {
        return fromStickyBroadcast( context, OpenVpnPasswordRequest.NONE );
    }

    @Deprecated
    // TODO: either write test or remove this method
    public static OpenVpnState fromStickyBroadcast(Context context, OpenVpnPasswordRequest passwordRequest)
    {
        Intent daemonStateIntent = context.registerReceiver( null, new IntentFilter( "de.schaeuffelhut.android.openvpn.Intents.DAEMON_STATE_CHANGED" ) );
        if ( daemonStateIntent == null )
            return STOPPED_INSTANCE;

        OpenVpnDaemonState daemonState = OpenVpnDaemonState.values()[ daemonStateIntent.getIntExtra( "daemon-state", 0 ) ];
        if ( daemonState.isStopped() )
            return new Stopped( daemonState );

        Intent networkStateIntent = context.registerReceiver( null, new IntentFilter( "de.schaeuffelhut.android.openvpn.Intents.NETWORK_STATE_CHANGED" ) );
        if ( networkStateIntent == null )
            return STOPPED_INSTANCE;

        return new Started(
                daemonState, OpenVpnNetworkState.values()[networkStateIntent.getIntExtra( "network-state", 0 )],
                passwordRequest,
                networkStateIntent.getStringExtra( "config" ),
                networkStateIntent.getStringExtra( "network-localip" ),
                networkStateIntent.getStringExtra( "network-remoteip" ),
                0, 0, 0
        );
    }

    public static OpenVpnState stopped()
    {
        return STOPPED_INSTANCE;
    }


    public abstract boolean isStarted();
    public abstract OpenVpnDaemonState getDaemonState();
    public abstract OpenVpnNetworkState getNetworkState();
    public abstract OpenVpnPasswordRequest getPasswordRequest();
    public abstract String  getConnectedTo();
    public abstract String getLocalIp();
    public abstract String getRemoteIp();
    public abstract long getBytesSent();
    public abstract long getBytesReceived();
    public abstract int getConnectedSeconds();


    final static class Started extends OpenVpnState
    {
        private final OpenVpnDaemonState daemonState;
        private final OpenVpnNetworkState networkState;
        private final OpenVpnPasswordRequest passwordRequest;
        private final String connectedTo;
        private final String localIp;
        private final String remoteIp;
        private final long bytesSent;
        private final long bytesReceived;
        private final int contectedSeconds;

        @Deprecated
        Started()
        {
            this.daemonState = OpenVpnDaemonState.UNKNOWN;
            this.networkState = OpenVpnNetworkState.UNKNOWN;
            this.passwordRequest = OpenVpnPasswordRequest.NONE;
            this.connectedTo = "";
            this.localIp = "";
            this.remoteIp = "";
            this.bytesSent = 0;
            this.bytesReceived = 0;
            this.contectedSeconds = 0;
        }

        Started(OpenVpnDaemonState daemonState, OpenVpnNetworkState networkState, OpenVpnPasswordRequest passwordRequest, String connectedTo, String localIp, String remoteIp, long bytesSent, long bytesReceived, int connectedSeconds)
        {
            this.daemonState = daemonState;
            this.networkState = networkState;
            this.passwordRequest = passwordRequest;
            this.connectedTo = connectedTo;
            this.localIp = localIp;
            this.remoteIp = remoteIp;
            this.bytesSent = bytesSent;
            this.bytesReceived = bytesReceived;
            this.contectedSeconds = connectedSeconds;
        }

        Started(Parcel in)
        {
            this(
                    (OpenVpnDaemonState)in.readParcelable( OpenVpnNetworkState.class.getClassLoader() ), // state
                    (OpenVpnNetworkState)in.readParcelable( OpenVpnNetworkState.class.getClassLoader() ), // state
                    (OpenVpnPasswordRequest)in.readParcelable( OpenVpnPasswordRequest.class.getClassLoader() ), // state
                    in.readString(), // connected to
                    in.readString(), // local IP
                    in.readString(), // remote IP
                    in.readLong(), // bytes sent
                    in.readLong(), // bytes received
                    in.readInt() // connected seconds
            );
        }

        @Override
        public boolean isStarted()
        {
            return true;
        }

        @Override
        public OpenVpnDaemonState getDaemonState()
        {
            return daemonState;
        }

        @Override
        public OpenVpnNetworkState getNetworkState()
        {
            return networkState;
        }

        @Override
        public OpenVpnPasswordRequest getPasswordRequest()
        {
            return passwordRequest;
        }

        @Override
        public String getConnectedTo()
        {
            return connectedTo;
        }

        @Override
        public String getLocalIp()
        {
            return localIp;
        }

        @Override
        public String getRemoteIp()
        {
            return remoteIp;
        }

        @Override
        public long getBytesSent()
        {
            return bytesSent;
        }

        @Override
        public long getBytesReceived()
        {
            return bytesReceived;
        }

        @Override
        public int getConnectedSeconds()
        {
            return contectedSeconds;
        }

        public void writeToParcel(Parcel parcel, int flags)
        {
            parcel.writeByte( TYPE_STARTED_VERSION_1 );
            parcel.writeParcelable( daemonState, 0 );
            parcel.writeParcelable( networkState, 0 );
            parcel.writeParcelable( passwordRequest, 0 );
            parcel.writeString( connectedTo );
            parcel.writeString( localIp );
            parcel.writeString( remoteIp );
            parcel.writeLong( bytesSent );
            parcel.writeLong( bytesReceived );
            parcel.writeInt( contectedSeconds );
        }
    }

    final static class Stopped extends OpenVpnState
    {
        private final OpenVpnDaemonState daemonState;

        public Stopped(OpenVpnDaemonState daemonState)
        {
            if ( daemonState.isStarted() )
                throw new IllegalArgumentException( "state: " + daemonState );
            this.daemonState = daemonState;
        }

        @Override
        public boolean isStarted()
        {
            return false;
        }

        @Override
        public OpenVpnDaemonState getDaemonState()
        {
            return daemonState;
        }

        @Override
        public OpenVpnNetworkState getNetworkState()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        @Override
        public OpenVpnPasswordRequest getPasswordRequest()
        {
            return  OpenVpnPasswordRequest.NONE;
        }

        @Override
        public String getConnectedTo()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        @Override
        public String getLocalIp()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        @Override
        public String getRemoteIp()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        @Override
        public long getBytesSent()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        @Override
        public long getBytesReceived()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        @Override
        public int getConnectedSeconds()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        public void writeToParcel(Parcel parcel, int flags)
        {
            parcel.writeByte( TYPE_STOPPED_VERSION_1 );
            parcel.writeParcelable( daemonState, 0 );
        }
    }

    public final int describeContents()
    {
        return 0;
    }

    public abstract void writeToParcel(Parcel parcel, int flags);

    public static final Parcelable.Creator<OpenVpnState> CREATOR = new Parcelable.Creator<OpenVpnState>()
    {
        public OpenVpnState createFromParcel(Parcel in)
        {
            final byte objectType = in.readByte();
            switch (objectType)
            {
                case TYPE_STARTED_VERSION_1:
                    return new Started( in );
                case TYPE_STOPPED_VERSION_1:
                    return new Stopped( (OpenVpnDaemonState)in.readParcelable( OpenVpnDaemonState.class.getClassLoader() ) );
                default:
                    throw new RuntimeException( "Unexpected protocol version: " + objectType ); // should be UnexpectedSwitchValueException
            }
        }

        public OpenVpnState[] newArray(int size)
        {
            return new OpenVpnState[size];
        }
    };
}
