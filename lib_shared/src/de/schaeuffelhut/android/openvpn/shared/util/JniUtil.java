package de.schaeuffelhut.android.openvpn.shared.util;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-02-28
 */
public class JniUtil
{
    /*
     * Update JNI header:
     * 1. Build module lib_shared
     * 2. cd lib_shared/jni
     * 3. /opt/oracle/jdk1.6/bin/javah -classpath ../../out/production/lib_shared de.schaeuffelhut.android.openvpn.shared.util.JniUtil
     */

    static
    {
        System.loadLibrary( "jniutil" );
    }

    public static void closeQuietly(FileDescriptor fileDescriptor)
    {
        closeImpl( asInt( fileDescriptor ) );
    }

    public static void close(FileDescriptor fileDescriptor) throws IOException
    {
        close( asInt( fileDescriptor ) );
    }

    /*
     * (non-Javadoc)
     * Copied from AOSP dalvik/libcore/luni/src/main/java/org/apache/harmony/luni/platform/OSFileSystem.java,
     * method close(int fileDescriptor)
     * @see org.apache.harmony.luni.platform.OSFileSystem#close(int)
     */
    private static void close(int fileDescriptor) throws IOException
    {
        int rc = closeImpl( fileDescriptor );
        if (rc == -1)
        {
            throw new IOException();
        }
    }

    private static native int closeImpl(int fd);

    public static int asInt(FileDescriptor fd)
    {
        try
        {
            Method getInt = FileDescriptor.class.getDeclaredMethod( "getInt$" );
            return (Integer) getInt.invoke( fd );
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException( e );
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException( e );
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException( e );
        }
    }
}
