package de.schaeuffelhut.android.openvpn.service.api;

import android.content.Intent;
import android.content.IntentFilter;
import android.test.InstrumentationTestCase;
import de.schaeuffelhut.android.openvpn.service.api.Intents;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-31
 */
public class IntentsTest extends InstrumentationTestCase
{
    private static final String INTENT_PREFIX = "de.schaeuffelhut.android.openvpn.Intents";

    public void test_prefixes_of_all_intents___also_called_test_createIntent()
    {
        for (Intents intent : Intents.values())
            assertEquals( INTENT_PREFIX + "." + intent.name(), intent.createIntent().getAction() );
    }

    public void test_createLocalAppIntent()
    {
        for (Intents intentName : Intents.values())
        {
            Intent intent = intentName.createLocalAppIntent();
            assertEquals( INTENT_PREFIX + "." + intentName.name(), intent.getAction() );
            //assertEquals( INTENT_PREFIX, intent.getPackage() );  //TODO: uncomment once minSdkVersion > 3, also uncomment in Intents
        }
    }

    public void test_configure_IntentFilter()
    {
        for (Intents intentName : Intents.values())
        {
            IntentFilter intentFilter = new IntentFilter();
            intentName.configure( intentFilter );
            verifyIntentFilter( intentName, intentFilter );
        }
    }

    public void test_configure_return_IntentFilter_passed_in()
    {
        for (Intents intentName : Intents.values())
        {
            IntentFilter intentFilter = new IntentFilter();
            assertSame( intentFilter, intentName.configure( intentFilter ) );
        }
    }

    public void test_createIntentFilter()
    {
        for (Intents intentName : Intents.values())
            verifyIntentFilter( intentName, intentName.createIntentFilter() );
    }

    private void verifyIntentFilter(Intents intentName, IntentFilter intentFilter)
    {
        assertTrue( intentFilter.hasAction( intentName.createIntent().getAction() ) );
    }


    public void test_matches_intent()
    {
        for (Intents intentName : Intents.values())
            assertTrue( intentName.matches( intentName.createIntent() ) );
    }

    public void test_matches_intent_with_random_intent()
    {
        Intent intentToMatch = new Intent( "org.example.intents." + System.currentTimeMillis() );
        for (Intents intentName : Intents.values())
            assertFalse( intentName.matches( intentToMatch ) );
    }
}
