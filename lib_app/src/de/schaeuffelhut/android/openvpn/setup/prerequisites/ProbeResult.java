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

package de.schaeuffelhut.android.openvpn.setup.prerequisites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.schaeuffelhut.android.openvpn.lib.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/26/12
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProbeResult implements ListViewItem
{
    final PrerequisitesActivity.Status status;
    final String title;
    final String subtitle;
    final String log;
    private List<ListViewItem> childItems;

    ProbeResult(PrerequisitesActivity.Status status, String title, String subtitle, String log)
    {
        this( status, title, subtitle, log, Collections.<ListViewItem>emptyList() );
    }

    ProbeResult(PrerequisitesActivity.Status status, String title, String subtitle, String log, List<ListViewItem> childItems)
    {
        this.status = status;
        this.title = title;
        this.subtitle = subtitle;
        this.log = log;
        this.childItems = childItems;
    }

    public View configureView(Context context)
    {
        LayoutInflater inflater = LayoutInflater.from( context );
        View v = inflater.inflate( R.layout.prerequisites_probe, null, true );

        ImageView statusIcon = (ImageView) v.findViewById( R.id.prerequisites_item_status_icon );
        statusIcon.setImageResource( status.imageResource );
        TextView statusText = (TextView) v.findViewById( R.id.prerequisites_item_status_text );
        statusText.setText( status.name() );
        TextView titleText = (TextView) v.findViewById( R.id.prerequisites_item_title );
        titleText.setText( title );
        TextView subtitleText = (TextView) v.findViewById( R.id.prerequisites_item_subtitle );
        subtitleText.setText( subtitle );
        TextView logText = (TextView) v.findViewById( R.id.prerequisites_item_log_text );
        logText.setText( log );
//        logText.setVisibility( log == null || log.isEmpty() ? View.GONE : View.VISIBLE );
        logText.setVisibility( View.GONE );

        return v;
    }

    public void onClick(PrerequisitesActivity prerequisitesActivity, View v)
    {
        View view = v.findViewById( R.id.prerequisites_item_log_text );
        View view2 = v.findViewById( R.id.prerequisites_item_unhide_details );
        if (view.getVisibility() == View.VISIBLE)
        {
            view.setVisibility( View.GONE );
            view2.setVisibility( View.VISIBLE );
        }
        else
        {
            view.setVisibility( View.VISIBLE );
            view2.setVisibility( View.GONE );
        }

    }

    public List<ListViewItem> getChildItems()
    {
        return new ArrayList<ListViewItem>( childItems );
    }

}
