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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.lib.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/26/12
 * Time: 5:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrerequisitesActivity extends ListActivity
{
    enum Status
    {
        SUCCESS( R.drawable.ic_circle_green ),
        FAILED( R.drawable.ic_circle_red ),
        NOT_PROBED( R.drawable.ic_circle_gray );
        public final int imageResource;

        Status(int imageResource)
        {
            this.imageResource = imageResource;
        }
    }

    List<ListViewItem> probeResults = Collections.emptyList();

    public void probe()
    {
        probeResults = new ArrayList<ListViewItem>();
        addRecursivley( IocContext.get().probePrerequisites( getApplicationContext() ).getProbeResults() );
    }

    private void addRecursivley(List<? extends ListViewItem> listViewItems)
    {
        for(ListViewItem i : listViewItems)
        {
            probeResults.add( i );
            addRecursivley( i.getChildItems() );
        }
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        probe();
        setListAdapter(
                new BaseAdapter()
                {
                    public int getCount()
                    {
                        return probeResults.size();
                    }

                    public ListViewItem getItem(int i)
                    {
                        return probeResults.get( i );
                    }

                    public long getItemId(int i)
                    {
                        return i;
                    }

                    public View getView(int i, View view, ViewGroup viewGroup)
                    {
                        return getItem( i ).configureView( viewGroup.getContext() );
                    }
                }
        );
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        ((ListViewItem)l.getItemAtPosition( position )).onClick( this, v );
    }
}