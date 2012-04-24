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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/11/12
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class TunModuleFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate( R.layout.setup_wizard_tun_module, container );
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated( view, savedInstanceState );
        initView();
        ((Button) view.findViewById( R.id.setup_wizard_tun_module_try_to_load_module )).setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {

                        TunLoaderProbe tunLoaderProbe = IocContext.get().getTunLoaderProbe();

                        if (tryCurrentTunLoader().isChecked() && tryCurrentTunLoader().getVisibility() == View.VISIBLE)
                            tunLoaderProbe.tryCurrentTunLoader();

                        if (scanDeviceForTun().isChecked())
                            tunLoaderProbe.scanDeviceForTun();

                        if (trySdcard().isChecked())
                            tunLoaderProbe.trySdCard();

                        tunLoaderProbe.makeSuccessfullyProbedTunLoaderTheDefault( new TunLoaderPreferences( getActivity().getApplicationContext() ) );

                        initView();
                    }
                }
        );
    }

    private CheckBox tryCurrentTunLoader()
    {
        return (CheckBox) findView( R.id.setup_wizard_tun_module_option_try_current_tun_loader );
    }

    private CheckBox scanDeviceForTun()
    {
        return (CheckBox) findView( R.id.setup_wizard_tun_module_option_scan_device_for_tun );
    }

    private CheckBox trySdcard()
    {
        return (CheckBox) findView( R.id.setup_wizard_tun_module_option_try_sdcard );
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged( hidden );
        initView();
    }

    private void initView()
    {
        TunInfo tunInfo = IocContext.get().getTunInfo( getActivity().getApplicationContext() );
        setFlag( R.id.setup_wizard_tun_module_has_device_node, tunInfo.isDeviceNodeAvailable(), "Available", "Not Available" );
        if (tunInfo.hasTunLoader())
        {
            TunLoader tunLoader = tunInfo.getTunLoader();
            findTextView( R.id.setup_wizard_tun_module_tun_loader ).setText( tunLoader.getName() );
            if (tunLoader.hasPathToModule())
            {
                findView( R.id.setup_wizard_tun_module_path_to_module_table_row ).setVisibility( View.VISIBLE );
                findTextView( R.id.setup_wizard_tun_module_path_to_module ).setText( tunLoader.getPathToModule().getPath() );
            }
            else
            {
                findView( R.id.setup_wizard_tun_module_path_to_module_table_row ).setVisibility( View.INVISIBLE );
            }
        }
        else
        {
            findTextView( R.id.setup_wizard_tun_module_tun_loader ).setText( "none" );
            findView( R.id.setup_wizard_tun_module_path_to_module_table_row ).setVisibility( View.INVISIBLE );
        }
        if (tunInfo.isDeviceNodeAvailable())
            findView( R.id.setup_wizard_tun_module_section_load_module ).setVisibility( View.INVISIBLE );
        else
            findView( R.id.setup_wizard_tun_module_section_load_module ).setVisibility( View.VISIBLE );
        if (tunInfo.hasTunLoader())
            findView( R.id.setup_wizard_tun_module_option_try_current_tun_loader ).setVisibility( View.VISIBLE );
        else
            findView( R.id.setup_wizard_tun_module_option_try_current_tun_loader ).setVisibility( View.GONE );
    }

    private void setFlag(int componentId, boolean flag)
    {
        setFlag( componentId, flag, "Yes", "No" );
    }

    private void setFlag(int componentId, boolean flag, String yes, String no)
    {
        TextView testView = findTextView( componentId );
        testView.setText( flag ? yes : no );
        testView.setTextColor( flag ? Color.GREEN : Color.RED );
    }

    private TextView findTextView(int componentId)
    {
        return (TextView) getView().findViewById( componentId );
    }

    private View findView(int componentId)
    {
        return getView().findViewById( componentId );
    }

}
