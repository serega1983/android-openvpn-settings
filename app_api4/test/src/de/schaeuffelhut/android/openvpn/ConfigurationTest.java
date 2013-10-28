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

package de.schaeuffelhut.android.openvpn;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-25
 */
public class ConfigurationTest extends TestCase
{
    public void test_getInstance_returns_not_null() throws Exception
    {
        assertNotNull( Configuration.getInstance() );
    }

    public void test_new_does_not_fail_on_null() throws Exception
    {
        try
        {
            new Configuration( null );
        }
        catch (NullPointerException e)
        {
            fail( "Configuration.loadFrom should ignore a null argument" );
        }
    }

    public void test_new_closes_file() throws Exception
    {
        final AtomicBoolean closeCalled = new AtomicBoolean( false );
        InputStream is = new InputStream()
        {
            @Override
            public int read() throws IOException
            {
                return -1;
            }

            @Override
            public void close() throws IOException
            {
                closeCalled.set( true );
            }
        };
        new Configuration( is );
        assertTrue( closeCalled.get() );
    }

    public void test_new_closes_file_even_on_exception() throws Exception
    {
        final AtomicBoolean closeCalled = new AtomicBoolean( false );
        InputStream is = new InputStream()
        {
            @Override
            public int read() throws IOException
            {
                throw new IOException();
            }

            @Override
            public void close() throws IOException
            {
                closeCalled.set( true );
            }
        };
        new Configuration( is );
        assertTrue( closeCalled.get() );
    }

    /*
     * getBugSenseApiKey
     */

    public void test_getBugSenseApiKey_123() throws Exception
    {
        test_getBugSenseApiKey( "123" );
    }

    public void test_getBugSenseApiKey_567580ahjg897() throws Exception
    {
        test_getBugSenseApiKey( "567580ahjg897" );
    }

    private void test_getBugSenseApiKey(String value)
    {
        InputStream baos = new ByteArrayInputStream( ("BugSenseApiKey=" + value).getBytes() );
        assertEquals( value, new Configuration( baos ).getBugSenseApiKey() );
    }

    public void test_getBugSenseApiKey_with_no_property()
    {
        InputStream baos = new ByteArrayInputStream( "".getBytes() );
        assertEquals( "disabled", new Configuration( baos ).getBugSenseApiKey() );
    }

    public void test_getBugSenseApiKey_from_empty_property()
    {
        InputStream baos = new ByteArrayInputStream( ("BugSenseApiKey=").getBytes() );
        assertEquals( "disabled", new Configuration( baos ).getBugSenseApiKey() );
    }

    /*
     * isBugSenseEnabled
     */

    public void test_isBugSenseEnabled_123() throws Exception
    {
        test_isBugSenseEnabled( "123" );
    }

    public void test_isBugSenseEnabled_567580ahjg897() throws Exception
    {
        test_isBugSenseEnabled( "567580ahjg897" );
    }

    private void test_isBugSenseEnabled(String value)
    {
        InputStream baos = new ByteArrayInputStream( ("BugSenseApiKey=" + value).getBytes() );
        assertTrue( new Configuration( baos ).isBugSenseEnabled() );
    }

    public void test_isBugSenseEnabled_with_no_property()
    {
        InputStream baos = new ByteArrayInputStream( "".getBytes() );
        assertFalse( new Configuration( baos ).isBugSenseEnabled() );
    }

    public void test_isBugSenseEnabled_from_empty_property()
    {
        InputStream baos = new ByteArrayInputStream( ("BugSenseApiKey=").getBytes() );
        assertFalse( new Configuration( baos ).isBugSenseEnabled() );
    }

}
