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


    public void test_optionalShellEscape_word() throws Exception
    {
        assertEquals( "word", Util.optionalShellEscape( "word" ) );
    }

    public void test_optionalShellEscape_word_underscore() throws Exception
    {
        assertEquals( "a_word", Util.optionalShellEscape( "a_word" ) );
    }

    public void test_optionalShellEscape_word_camelcase() throws Exception
    {
        assertEquals( "AnotherWord", Util.optionalShellEscape( "AnotherWord" ) );
    }

    public void test_optionalShellEscape_option() throws Exception
    {
        assertEquals( "-a", Util.optionalShellEscape( "-a" ) );
    }

    public void test_optionalShellEscape_path() throws Exception
    {
        assertEquals( "/system/bin", Util.optionalShellEscape( "/system/bin" ) );
    }

    public void test_optionalShellEscape_file_with_extension() throws Exception
    {
        assertEquals( "config.ovpn", Util.optionalShellEscape( "config.ovpn" ) );
    }

    public void test_optionalShellEscape_numeric_option_value() throws Exception
    {
        assertEquals( "15", Util.optionalShellEscape( "15" ) );
    }

    public void test_optionalShellEscape_empty() throws Exception
    {
        assertEquals( "''", Util.optionalShellEscape( "" ) );
    }

    public void test_optionalShellEscape_space() throws Exception
    {
        assertEquals( "' '", Util.optionalShellEscape( " " ) );
    }

    public void test_optionalShellEscape_path_with_space() throws Exception
    {
        assertEquals( "'/sdcard/config dir/my config.ovpn'", Util.optionalShellEscape( "/sdcard/config dir/my config.ovpn" ) );
    }

    public void test_optionalShellEscape_crazy() throws Exception
    {
        assertEquals(
                "'/sdcard/\\'config dir\\'/\"my\\\nconfig.ovpn\"'",
                Util.optionalShellEscape( "/sdcard/'config dir'/\"my\nconfig.ovpn\"" )
        );
    }
}
