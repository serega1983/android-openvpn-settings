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

package de.schaeuffelhut.android.openvpn.setup;

import android.test.InstrumentationTestCase;
import android.test.MoreAsserts;
import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/23/12
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TunLoaderProbeImplTest extends InstrumentationTestCase
{
    private ArrayList<String> tunLoaderEvent = new ArrayList<String>();
    private String successfullTunLoader = "not defined";
    private String called_makeDefault;

    private final TunInfoFake tunInfo = new TunInfoFake() {
        @Override
        public boolean isDeviceNodeAvailable()
        {
            return super.isDeviceNodeAvailable() || tunLoaderEvent.contains( successfullTunLoader );
        }
    };
    private TunLoaderProbeImpl tunLoaderProbe = new TunLoaderProbeImpl( tunInfo, new ITunLoaderFactory(){

        public TunLoader createModprobe()
        {
            return new MyTunLoaderFake( "modprobe" );
        }

        public TunLoader createInsmod(File pathToModule)
        {
            return new MyTunLoaderFake( "insmod " + pathToModule.getPath() );
        }

        public TunLoader createNullTunLoader()
        {
            return new TunLoaderFactoryImpl.NullTunLoader();
        }
    });


    private class MyTunLoaderFake extends TunLoaderFake
    {
        private String tunLoaderKey;

        public MyTunLoaderFake(String tunLoaderKey)
        {
            super( tunLoaderKey );
            this.tunLoaderKey = tunLoaderKey;
        }

        @Override
        public void loadModule()
        {
            tunLoaderEvent.add( tunLoaderKey );
        }

        @Override
        public void makeDefault(TunLoaderPreferences preferences)
        {
            super.makeDefault( preferences );
            called_makeDefault = getName();
        }
    }



    public void test_tryToLoadModule_tries_current_TunLoader_if_defined()
    {
        tunInfo.setTunLoader( new MyTunLoaderFake( "current" ) );
        tunLoaderProbe.tryCurrentTunLoader();
        tunLoaderProbe.tryToLoadModule();
        Assert.assertEquals( Arrays.asList("current"), tunLoaderEvent );
    }

    public void test_tryToLoadModule_tries_current_TunLoader_not_if_not_defined()
    {
        tunInfo.setTunLoader( null );
        tunLoaderProbe.tryCurrentTunLoader();
        tunLoaderProbe.tryToLoadModule();
        Assert.assertTrue( tunLoaderEvent.isEmpty() ) ;
    }


    public void test_tryToLoadModule_scans_device()
    {
        tunLoaderProbe.scanDeviceForTun();
        tunLoaderProbe.tryToLoadModule();
        Assert.assertEquals( Arrays.asList(
                "modprobe",
                "insmod /system/lib/modules/tun.ko",
                "insmod /lib/modules/tun.ko"
        ), tunLoaderEvent );
    }

    public void test_tryToLoadModule_checks_sdcard()
    {
        tunLoaderProbe.trySdCard();
        tunLoaderProbe.tryToLoadModule();
        Assert.assertEquals( Arrays.asList(
                "insmod /sdcard/tun.ko" //TODO: this is naive. The tun module needs to be copied to private storage
        ), tunLoaderEvent );
    }

    public void test_tryToLoadModule_does_nothing()
    {
        tunLoaderProbe.tryToLoadModule();
        Assert.assertTrue( tunLoaderEvent.isEmpty() );
    }

    public void test_tryToLoadModule_returns_NullTunLoader()
    {
        successfullTunLoader = "not defined";
        MoreAsserts.assertAssignableFrom( TunLoaderFactoryImpl.NullTunLoader.class, tunLoaderProbe.tryToLoadModule() );
    }

    public void test_tryToLoadModule_returns_successfull_loader()
    {
        successfullTunLoader = "current";
        tunInfo.setTunLoader( new MyTunLoaderFake( "current" ) );
        tunLoaderProbe.tryCurrentTunLoader();
        TunLoader tunLoader = tunLoaderProbe.tryToLoadModule();
        Assert.assertEquals( "current", tunLoader.getName() );
    }

    public void test_tryToLoadModule_throws_exception_if_module_has_already_been_loaded()
    {
        tunInfo.setDeviceNodeAvailable( true );
        try
        {
            tunLoaderProbe.tryToLoadModule();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            Assert.assertEquals( "Can not test for tun device node as it is already available.", e.getMessage() );
        }
    }

    public void test_makeSuccessfullyProbedTunLoaderTheDefault()
    {
        successfullTunLoader = "modprobe";
        tunLoaderProbe.scanDeviceForTun();
        tunLoaderProbe.makeSuccessfullyProbedTunLoaderTheDefault( new TunLoaderPreferences( getInstrumentation().getContext() ) );
        Assert.assertEquals( "modprobe", called_makeDefault );
    }
}
