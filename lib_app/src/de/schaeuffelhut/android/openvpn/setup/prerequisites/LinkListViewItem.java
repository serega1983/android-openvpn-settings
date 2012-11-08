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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.lib.app.R;

import java.util.Collections;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: fries
* Date: 5/6/12
* Time: 11:11 AM
* To change this template use File | Settings | File Templates.
*/
class LinkListViewItem implements ListViewItem
{
    private Uri uri;
    private int title;

    LinkListViewItem(int title, Uri uri)
    {
        this.uri = uri;
        this.title = title;
    }


    public View configureView(Context context)
    {
        LayoutInflater inflater = LayoutInflater.from( context );
        View view = inflater.inflate( R.layout.prerequisites_link, null, true );
        TextView textView = (TextView) view.findViewById( R.id.prerequisites_item_title );
        textView.setText( title );
        return view;
    }

    public void onClick(PrerequisitesActivity activity, View v)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData( uri );
        try
        {
            activity.startActivity( intent );
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText( activity, e.getMessage(), Toast.LENGTH_LONG ).show();
        }
    }

    public List<ListViewItem> getChildItems()
    {
        return Collections.emptyList();
    }
}
