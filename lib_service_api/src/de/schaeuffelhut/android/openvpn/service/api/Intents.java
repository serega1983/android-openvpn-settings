package de.schaeuffelhut.android.openvpn.service.api;

import android.content.Intent;
import android.content.IntentFilter;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-31
 */
public enum Intents
{
    OPENVPN_STATE_CHANGED,
    OPENVPN_NEEDS_USERNAME_PASSWORD,
    DAEMON_STATE_CHANGED(){
        @Override
        public String getAction()
        {
            return "de.schaeuffelhut.android.openvpn.Intents.DAEMON_STATE_CHANGED"; //TODO: duplicates [lib_app]Intents.DAEMON_STATE_CHANGED;
        }
    },
    NETWORK_STATE_CHANGED(){
        @Override
        public String getAction()
        {
            return "de.schaeuffelhut.android.openvpn.Intents.NETWORK_STATE_CHANGED"; //TODO: duplicates [lib_app]Intents.NETWORK_STATE_CHANGED;
        }
    };

    private static final String INTENT_PREFIX = "de.schaeuffelhut.android.openvpn.Intents";

    public String getAction()
    {
        return INTENT_PREFIX + "." + this.name();
    }

    public Intent createIntent()
    {
        return new Intent( getAction() );
    }

    public Intent createLocalAppIntent()
    {
        Intent intent = createIntent();
        //intent.setPackage( INTENT_PREFIX ); //TODO: uncomment once minSdkVersion > 3, also uncomment in IntentsTest
        return intent;
    }

    public IntentFilter configure(IntentFilter intentFilter)
    {
        intentFilter.addAction( getAction() );
        return intentFilter;
    }

    public IntentFilter createIntentFilter()
    {
        IntentFilter intentFilter = new IntentFilter();
        configure( intentFilter );
        return intentFilter;
    }

    public boolean matches(Intent intent)
    {
        return getAction().equals( intent.getAction() );
    }
}
