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

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Config;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.service.api.IOpenVpnStateListener;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnDaemonState;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnNetworkState;

import java.util.ArrayList;

/**
 * Encapsulates registration of {@code IOpenVpnStateListener}s, as well as handling dispatching to
 * registered listeners.
 *
 * Most if this code is taken from the {@code GpsLocationProvider} found in the AOSP
 * in file {@code frameworks/base/location/java/com/android/internal/location/GpsLocationProvider.java}.
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-14
 */
class OpenVpnStateListenerDispatcher implements IOpenVpnStateListener
{
    private final String TAG = "OpenVPN-Settings";

    private class Listener implements IBinder.DeathRecipient
    {
        final IOpenVpnStateListener mListener;

        private Listener(IOpenVpnStateListener listener)
        {
            this.mListener = listener;
        }

        public void binderDied()
        {
            if (Config.LOGD) Log.d( TAG, "OpenVpnStateListener died" );

            synchronized (mListeners)
            {
                mListeners.remove( this );
            }
            if (mListener != null)
            {
                mListener.asBinder().unlinkToDeath( this, 0 );
            }
        }
    }

    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addOpenVpnStateListener(IOpenVpnStateListener listener) throws RemoteException
    {
        synchronized (mListeners)
        {
            if (listener == null)
            {
                throw new NullPointerException( "listener is null in addOpenVpnStateListener" );
            }

            IBinder binder = listener.asBinder();
            int size = mListeners.size();
            for (int i = 0; i < size; i++)
            {
                Listener test = mListeners.get( i );
                if (binder.equals( test.mListener.asBinder() ))
                {
                    // listener already added
                    return;
                }
            }

            Listener l = new Listener( listener );
            binder.linkToDeath( l, 0 );
            mListeners.add( l );
        }
    }

    public void removeOpenVpnStateListener(IOpenVpnStateListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException( "listener is null in removeOpenVpnStateListener" );
        }

        synchronized (mListeners)
        {
            IBinder binder = listener.asBinder();
            Listener l = null;
            int size = mListeners.size();
            for (int i = 0; i < size && l == null; i++)
            {
                Listener test = mListeners.get( i );
                if (binder.equals( test.mListener.asBinder() ))
                {
                    l = test;
                }
            }

            if (l != null)
            {
                mListeners.remove( l );
                binder.unlinkToDeath( l, 0 );
            }
        }

    }

    private static interface Method
    {
        void execute(IOpenVpnStateListener listener) throws RemoteException;
    }

    private void notifyListeners(Method method)
    {
        synchronized (mListeners)
        {
            int size = mListeners.size();
            for (int i = 0; i < size; i++)
            {
                Listener listener = mListeners.get( i );
                try
                {
                    method.execute( listener.mListener );
                }
                catch (RemoteException e)
                {
                    Log.w( TAG, "RemoteException in notifyListeners" );
                    mListeners.remove( listener );
                    // adjust for size of list changing
                    size--;
                }
            }
        }
    }

    public void onDaemonStateChanged(final OpenVpnDaemonState toState)
    {
        notifyListeners( new Method()
        {
            public void execute(IOpenVpnStateListener listener) throws RemoteException
            {
                listener.onDaemonStateChanged( toState );
            }
        } );
    }

    public void onRequestPassphrase()
    {
        notifyListeners( new Method()
        {
            public void execute(IOpenVpnStateListener listener) throws RemoteException
            {
                listener.onRequestPassphrase();
            }
        } );
    }

    public void onRequestCredentials()
    {
        notifyListeners( new Method()
        {
            public void execute(IOpenVpnStateListener listener) throws RemoteException
            {
                listener.onRequestCredentials();
            }
        } );
    }

    public void onNetworkStateChanged(final OpenVpnNetworkState fromState, final OpenVpnNetworkState toState, final long time, final String cause, final String localIp, final String remoteIp)
    {
        notifyListeners( new Method()
        {
            public void execute(IOpenVpnStateListener listener) throws RemoteException
            {
                listener.onNetworkStateChanged( fromState, toState, time, cause, localIp, remoteIp );
            }
        } );
    }

    public void onByteCountChanged(final long received, final long sent)
    {
        notifyListeners( new Method()
        {
            public void execute(IOpenVpnStateListener listener) throws RemoteException
            {
                listener.onByteCountChanged( received, sent );
            }
        } );
    }

    public IBinder asBinder()
    {
        throw new UnsupportedOperationException();
    }
}
