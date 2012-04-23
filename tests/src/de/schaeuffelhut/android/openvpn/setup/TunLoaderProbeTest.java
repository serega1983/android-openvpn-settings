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

import junit.framework.Assert;
import junit.framework.TestCase;

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
public class TunLoaderProbeTest extends TestCase
{
    private boolean called_createCurrent;
    private boolean called_createModprobe;
    private boolean called_createInsmod;
    private ArrayList<String> tunLoaderEvent = new ArrayList<String>();
    private TunLoaderProbe tunLoaderProbe = new TunLoaderProbe( new ITunLoaderFactory(){

        public TunLoader createCurrent()
        {
            called_createCurrent = true;
            return new MyTunLoaderFake( "current" );
        }

        public TunLoader createModprobe()
        {
            called_createModprobe = true;
            return new MyTunLoaderFake( "modprobe" );
        }

        public TunLoader createInsmod(File pathToModule)
        {
            called_createInsmod = true;
            return new MyTunLoaderFake( "insmod " + pathToModule.getPath() );
        }
    });


    private class MyTunLoaderFake extends TunLoaderFake
    {
        private String tunLoaderKey;

        public MyTunLoaderFake(String tunLoaderKey)
        {
            super( "fake" );
            this.tunLoaderKey = tunLoaderKey;
        }

        @Override
        public void loadModule()
        {
            tunLoaderEvent.add( tunLoaderKey );
        }
    }



    public void test_tryToLoadModule_tries_current_TunLoader()
    {
        tunLoaderProbe.tryCurrentTunLoader();
        tunLoaderProbe.tryToLoadModule();
        Assert.assertEquals( Arrays.asList("current"), tunLoaderEvent );
    }


    public void test_tryToLoadModule_scans_device()
    {
        tunLoaderProbe.scanDeviceForTun();
        tunLoaderProbe.tryToLoadModule();
        Assert.assertTrue( called_createInsmod );
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
        Assert.assertTrue( called_createInsmod );
        Assert.assertEquals( Arrays.asList(
                "insmod /sdcard/tun.ko" //TODO: this is naive. The tun module needs to be copied to private storage
        ), tunLoaderEvent );
    }

    public void test_tryToLoadModule_does_nothing()
    {
        tunLoaderProbe.tryToLoadModule();
        Assert.assertTrue( tunLoaderEvent.isEmpty() );
    }
}

