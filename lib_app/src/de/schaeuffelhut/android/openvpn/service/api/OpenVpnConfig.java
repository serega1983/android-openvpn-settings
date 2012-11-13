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
        parcel.writeString( pathToConfigFile.getPath() );
    }

    public static final Parcelable.Creator<OpenVpnConfig> CREATOR = new Parcelable.Creator<OpenVpnConfig>()
    {
        public OpenVpnConfig createFromParcel(Parcel in)
        {
            return new OpenVpnConfig( new File( in.readString() ) );
        }

        public OpenVpnConfig[] newArray(int size)
        {
            return new OpenVpnConfig[size];
        }
    };
}
