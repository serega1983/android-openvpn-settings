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
    private static final Stopped STOPPED_INSTANCE = new Stopped();

    private OpenVpnState()
    {
    }

    @Deprecated
    // TODO: either write test or remove this class
    public static OpenVpnState fromStickyBroadcast(Context context)
    {
        Intent stateIntent = context.registerReceiver( null, new IntentFilter( "de.schaeuffelhut.android.openvpn.Intents.DAEMON_STATE_CHANGED" ) );
        if (stateIntent == null)
            return new Stopped();
        if (stateIntent.getIntExtra( "daemon-state", 0 ) < 1)
            return new Stopped();
        if (stateIntent.getIntExtra( "daemon-state", 0 ) > 2)
            return new Stopped();

        Intent networkState = context.registerReceiver( null, new IntentFilter( "de.schaeuffelhut.android.openvpn.Intents.NETWORK_STATE_CHANGED" ) );
        if ( networkState == null )
            return new Stopped();

        return new Started(
                OpenVpnNetworkState.values()[networkState.getIntExtra( "network-state", 0 )],
                networkState.getStringExtra( "config" ),
                networkState.getStringExtra( "network-localip" ),
                0, 0, 0
        );
    }

    public static OpenVpnState stopped()
    {
        return STOPPED_INSTANCE;
    }


    public abstract boolean isStarted();
    public abstract OpenVpnNetworkState getNetworkState();  // TODO: OpenVpnNetworkState getNetworkState()
    public abstract String  getConnectedTo();
    public abstract String  getIp();
    public abstract long getBytesSent();
    public abstract long getBytesReceived();
    public abstract int getConnectedSeconds();


    final static class Started extends OpenVpnState
    {
        private final OpenVpnNetworkState networkState;
        private final String connectedTo;
        private final String ip;
        private final long bytesSent;
        private final long bytesReceived;
        private final int contectedSeconds;

        @Deprecated
        Started()
        {
            this.networkState = OpenVpnNetworkState.UNKNOWN;
            this.connectedTo = "";
            this.ip = "";
            this.bytesSent = 0;
            this.bytesReceived = 0;
            this.contectedSeconds = 0;
        }

        Started(OpenVpnNetworkState networkState, String connectedTo, String ip, long bytesSent, long bytesReceived, int connectedSeconds)
        {
            this.networkState = networkState;
            this.connectedTo = connectedTo;
            this.ip = ip;
            this.bytesSent = bytesSent;
            this.bytesReceived = bytesReceived;
            this.contectedSeconds = connectedSeconds;
        }

        Started(Parcel in)
        {
            this(
                    (OpenVpnNetworkState)in.readParcelable( OpenVpnNetworkState.class.getClassLoader() ), // state
                    in.readString(), // connected to
                    in.readString(), // ip
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
        public OpenVpnNetworkState getNetworkState()
        {
            return networkState;
        }

        @Override
        public String getConnectedTo()
        {
            return connectedTo;
        }

        @Override
        public String getIp()
        {
            return ip;
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
            parcel.writeParcelable( networkState, 0 ); //TOOD: could also be an integer, e.g. enum.ordinal
            parcel.writeString( connectedTo );
            parcel.writeString( ip );
            parcel.writeLong( bytesSent );
            parcel.writeLong( bytesReceived );
            parcel.writeInt( contectedSeconds );
        }
    }

    final static class Stopped extends OpenVpnState
    {
        @Override
        public boolean isStarted()
        {
            return false;
        }

        @Override
        public OpenVpnNetworkState getNetworkState()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        @Override
        public String getConnectedTo()
        {
            throw new IllegalStateException( "Service is stopped" );
        }

        @Override
        public String getIp()
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
            //TODO: implement method stub
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
                    return new Stopped();
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
