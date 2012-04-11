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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.R;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/11/12
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrerequisitesFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate( R.layout.setup_wizard_prerequisites, container );
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged( hidden );
        setFlag( R.id.setup_wizard_prerequisites_hasRoot, IocContext.get().getPrerequisites().hasRootShell() );
        setFlag( R.id.setup_wizard_prerequisites_hasTunDevice, IocContext.get().getPrerequisites().hasTunDevice() );
        setFlag( R.id.setup_wizard_prerequisites_hasTunModule, IocContext.get().getPrerequisites().hasTunKernelModule() );
        setFlag( R.id.setup_wizard_prerequisites_hasInsmod, IocContext.get().getPrerequisites().hasInsmod() );
        setFlag( R.id.setup_wizard_prerequisites_hasBusybox, IocContext.get().getPrerequisites().hasBusyBox() );
        setFlag( R.id.setup_wizard_prerequisites_hasOpenVpn, IocContext.get().getPrerequisites().hasOpenVPN() );
    }


    private void setFlag(int componentId, boolean flag)
    {
        TextView testView = (TextView) getView().findViewById( componentId );
        testView.setText( flag ? "Yes" : "No" );
        testView.setTextColor( flag ? Color.GREEN : Color.RED );
    }

}
