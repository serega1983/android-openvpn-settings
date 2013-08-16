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
package de.schaeuffelhut.android.openvpn;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.shared.util.Util;

public final class HtmlDialog {
	private HtmlDialog() {
	}

	public final static Dialog makeHelpDialog(Context context) {
		
		String string = Util.getAssetAsString(context, "help.html");
		string = string.replace( "$version", Util.applicationVersionName(context) );
		
		return makeDialog(context, R.string.help_dialog_title, string);
	}

	public final static Dialog makeChangeLogDialog(Context context)
	{
		String string = Util.getAssetAsString(context, "ChangeLog.html");
		return makeDialog(context, R.string.changelog_dialog_title, string);
	}

	private static Dialog makeDialog(Context context, int title, String html)
	{
		Spanned message = Html.fromHtml( html );
		return makeDialog(context, title, message);
	}

	private static Dialog makeDialog(Context context, int title, Spanned message)
	{
		TextView textView = new TextView(context);
		textView.setText(message, BufferType.SPANNABLE );
		textView.setMovementMethod( LinkMovementMethod.getInstance() );

		ScrollView scrollView = new ScrollView(context);
		scrollView.addView( textView );

		final Dialog dialog = new AlertDialog.Builder(context).
		setTitle( title ).
		setView( scrollView ).
		setNeutralButton( "OK", null ).
		setCancelable(true).
		setIcon( android.R.drawable.ic_dialog_info).
		create();
		return dialog;
	}
}
