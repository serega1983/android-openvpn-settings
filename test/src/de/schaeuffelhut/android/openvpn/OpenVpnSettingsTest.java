package de.schaeuffelhut.android.openvpn;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class de.schaeuffelhut.android.openvpn.OpenVpnSettingsTest \
 * de.schaeuffelhut.android.openvpn.tests/android.test.InstrumentationTestRunner
 */
public class OpenVpnSettingsTest extends ActivityInstrumentationTestCase2<OpenVpnSettings> {

    public OpenVpnSettingsTest() {
        super("de.schaeuffelhut.android.openvpn", OpenVpnSettings.class);
    }

    public void test1() throws InterruptedException
    {
        fail("Test was disabled");
//        OpenVpnSettings activity = getActivity();
//        Thread.sleep(2500);
//        activity.finish();
    }
}
