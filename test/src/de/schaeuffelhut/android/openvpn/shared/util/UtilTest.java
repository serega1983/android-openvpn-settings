package de.schaeuffelhut.android.openvpn.shared.util;

import junit.framework.TestCase;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-11
 */
public class UtilTest extends TestCase
{
    public void test_isBlank_returns_true_for_null() throws Exception
    {
        assertTrue( Util.isBlank(null) );
    }

    public void test_isBlank_returns_true_for_empty_string() throws Exception
    {
        assertTrue( Util.isBlank("") );
    }

    public void test_isBlank_returns_true_for_space() throws Exception
    {
        assertTrue( Util.isBlank(" ") );
    }

    public void test_isBlank_returns_true_for_tab() throws Exception
    {
        assertTrue( Util.isBlank("\t") );
    }

    public void test_isBlank_returns_true_for_crlf() throws Exception
    {
        assertTrue( Util.isBlank("\r\n") );
    }

    public void test_isBlank_returns_true_for_white_spaces() throws Exception
    {
        assertTrue( Util.isBlank(" \t\r\n") );
    }

    public void test_isBlank_returns_false_for_a_text() throws Exception
    {
        assertFalse( Util.isBlank("a text") );
    }

    public void test_isBlank_returns_false_for_a_second_text() throws Exception
    {
        assertFalse( Util.isBlank("a second text") );
    }
}
