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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import de.schaeuffelhut.android.openvpn.R;

public class SystemSetupWizard extends FragmentActivity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.setup_wizard );

        showWizardFragment( getPrerquisitesFragment() );

        ((Button) findViewById( R.id.setup_wizard_back )).setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View view)
            {
                showWizardFragment( getPrerquisitesFragment() );
            }
        } );
        ((Button) findViewById( R.id.setup_wizard_next )).setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View view)
            {
                showWizardFragment( getTunModuleFragment() );
            }
        } );
    }

    private void showWizardFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        hideAllWizardFragments( fragmentTransaction );
        fragmentTransaction.show( fragment );
        fragmentTransaction.commit();
    }

    private void hideAllWizardFragments(FragmentTransaction fragmentTransaction)
    {
        fragmentTransaction.hide( getPrerquisitesFragment() );
        fragmentTransaction.hide( getTunModuleFragment() );
    }

    private PrerequisitesFragment getPrerquisitesFragment()
    {
        return (PrerequisitesFragment) getSupportFragmentManager().findFragmentById( R.id.setup_wizard_prerequisites_fragment );
    }

    private TunModuleFragment getTunModuleFragment()
    {
        return (TunModuleFragment) getSupportFragmentManager().findFragmentById( R.id.setup_wizard_tun_module_fragment );
    }
}

