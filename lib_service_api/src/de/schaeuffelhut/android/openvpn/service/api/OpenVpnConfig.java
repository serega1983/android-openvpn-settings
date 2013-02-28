package de.schaeuffelhut.android.openvpn.service.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-08-29
 */
public class OpenVpnConfig implements Parcelable
{
    private static final byte PROTOCOL_VERSION_1 = 1;
    private final File pathToConfigFile;

    public OpenVpnConfig(File pathToConfigFile)
    {
        this.pathToConfigFile = pathToConfigFile;
    }

    public File getFile()
    {
        return pathToConfigFile;
    }


    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeByte( PROTOCOL_VERSION_1 );
        parcel.writeString( pathToConfigFile.getPath() );
    }

    public static final Parcelable.Creator<OpenVpnConfig> CREATOR = new Parcelable.Creator<OpenVpnConfig>()
    {
        public OpenVpnConfig createFromParcel(Parcel in)
        {
            final byte protocolVersion = in.readByte();
            switch (protocolVersion)
            {
                case 1:
                    return new OpenVpnConfig( new File( in.readString() ) );
                default:
                    throw new RuntimeException( "Unexpected protocol version: " + protocolVersion ); // should be UnexpectedSwitchValueException
            }

        }

        public OpenVpnConfig[] newArray(int size)
        {
            return new OpenVpnConfig[size];
        }
    };
}
