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

package de.schaeuffelhut.android.openvpn.tun;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.schaeuffelhut.android.openvpn.util.tun.TunPreferences;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

import de.schaeuffelhut.android.openvpn.Configuration;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.shared.util.Util;

public class ShareTunActivity extends Activity
{
	private final class SendViaHttp extends AsyncTask<Void, Void, Void>
	{

		private String md5CalculatedByClient = null;
		private String md5CalculatedByServer = null;
		private DefaultHttpClient httpClient = new DefaultHttpClient();
		private HttpPost post = new HttpPost( Configuration.TUN_COLLECTOR_URL );
		private MultipartEntity multipartEntity = new MultipartEntity();
		private DigestOutputStream digestOutputStream;

		protected void onPreExecute()
		{
			showDialog(DIALOG_UPLOAD_DEVICE_DETAILS);
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			try {
				sendDeviceDeatils();
				TunPreferences.setSendDeviceDetailWasSuccessfull( getApplicationContext(), postWasSuccessfull() );
			} catch (OperationFailed e) {
				Log.e( "OpenVPN-Settings", "Uploading device dteails failed", e );
				// TODO: log via ACRA
				BugSenseHandler.sendExceptionMessage( "DEBUG", "Uploading device dteails failed", e );
				TunPreferences.setSendDeviceDetailWasSuccessfull( getApplicationContext(), false );
			} catch (Exception e) {
				BugSenseHandler.sendExceptionMessage( "DEBUG", "Uploading device dteails failed", e );
			}			
			return null;
		}

		private void sendDeviceDeatils() throws OperationFailed 
		{
			initializeMd5Sum();
			attach( "openvpn-settings-version", Util.applicationVersionName( getApplicationContext() ) );
			attach( "kernel-version",           deviceDetails.kernelVersion );
			attach( "device-detail.txt",        deviceDetails.deviceDetails.getBytes(), "application/octet-stream", "device-detail.txt" );
			attach( "tun.ko",                   new File( deviceDetails.pathToTun ), "application/octet-stream" );
//			attach( "config.gz",                new File( "/proc/config.gz" ), "application/octet-stream" );
//			attach( "mounts",                   new File( "/proc/mounts" ), "application/octet-stream" );
			attachFinalMd5Sum(); // must be last

			final HttpResponse response = postDeviceDetailsToServer();
			final String responseAsString = reponseEntity(response);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				md5CalculatedByServer = responseAsString.trim().toUpperCase();
				Log.i( "OpenVPN-Settings", "md5 calculated by server: " + md5CalculatedByServer );
			} else {
				md5CalculatedByServer = null;
				Log.e( "OpenVPN-Settings", "failed to upload TUN module: " + response.getStatusLine() );
			}
		}

		private void initializeMd5Sum() throws OperationFailed {
			digestOutputStream = new DigestOutputStream(new NullOutputStream(), newMd5Digest());
		}

		private MessageDigest newMd5Digest() throws OperationFailed {
			final MessageDigest md5;
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new OperationFailed( "Get instance of MD5 message digest", e );
			}
			return md5;
		}

		private HttpResponse postDeviceDetailsToServer() throws OperationFailed {
			post.setEntity(multipartEntity);
			try {
				return httpClient.execute(post);
			} catch (ClientProtocolException e) {
				throw new OperationFailed( "Executing HTTP POST", e );
			} catch (IOException e) {
				throw new OperationFailed( "Executing HTTP POST", e );
			}
		}

		private String reponseEntity(final HttpResponse response) throws OperationFailed {
			try {
				return EntityUtils.toString(response.getEntity());
			} catch (IOException e) {
				throw new OperationFailed( "Reading reponst to HTTP POST", e );
			}
		}

		private void attach(String name, String value) {
			multipartEntity.addPart( name, StringBody.create( value, "text/plain", Charset.forName("UTF-8") ) );
			digest( value );
		}

		private void attach(String name, File file, String mimeType) {
			if ( file.exists() )
			{
				multipartEntity.addPart( name, new FileBody( file, mimeType ) );
				digest( file );
			}
		}
		
		private void attach(String name, byte[] bytes, String mimeType, String filename) {
			multipartEntity.addPart( name, new ByteArrayBody( bytes, mimeType, filename ) );
			digest( bytes );
		}

		private void attachFinalMd5Sum() {
			digest( Configuration.TUN_COLLECTOR_SECRET );
			md5CalculatedByClient = calculateMd5Sum();
			Log.i( "OpenVPN-Settings", "md5 calculated by client: " + md5CalculatedByClient );
			multipartEntity.addPart( "md5", StringBody.create( md5CalculatedByClient, "text/plain", Charset.forName("UTF-8") ) );
		}

		private String calculateMd5Sum() {
			IOUtils.closeQuietly( digestOutputStream );
			return String.format("%032x",new BigInteger(1, digestOutputStream.getMessageDigest().digest())).toUpperCase();
		}


		private void digest(byte[] bytes) {
			try {
				IOUtils.write(bytes, digestOutputStream);
			} catch (Exception e) {
				BugSenseHandler.sendExceptionMessage( "DEBUG", "Digesting bytes", e );
				// ignore, all data goes to a NullOutputStream
			}
		}

		private void digest(File file) {
			try {
				FileUtils.copyFile( file, digestOutputStream );
			} catch (Exception e) {
				BugSenseHandler.sendExceptionMessage( "DEBUG", "Digesting file", e );
				// ignore, all data goes to a NullOutputStream
			}
		}

		private void digest(String value) {
			try {
				digestOutputStream.write( value.getBytes( "UTF-8" ) );
			} catch (Exception e) {
				BugSenseHandler.sendExceptionMessage( "DEBUG", "Digesting String", e );
				// ignore, all data goes to a NullOutputStream
			}
		}

		boolean postWasSuccessfull() {
			if ( md5CalculatedByClient == null )
				return false;
			if ( md5CalculatedByServer == null )
				return false;
			if ( "".equals( md5CalculatedByClient.trim() ) )
				return false;
			if ( "".equals( md5CalculatedByServer.trim() ) )
				return false;

			return md5CalculatedByClient.equals( md5CalculatedByServer );
		}

		protected void onPostExecute(Void result) {
			removeDialog( DIALOG_UPLOAD_DEVICE_DETAILS );
			if ( postWasSuccessfull() )
				showDialog( DIALOG_UPLOAD_SUCCESS );
			else
				showDialog( DIALOG_UPLOAD_FAILED );
		}
	}

	private final static int DIALOG_UPLOAD_DEVICE_DETAILS = 1;
	private final static int DIALOG_UPLOAD_SUCCESS = 2;
	private final static int DIALOG_UPLOAD_FAILED = 3;

	DeviceDetails deviceDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.share_tun);

		deviceDetails = new DeviceDetails(getPreferences(Context.MODE_PRIVATE));

		kernelVersionTextView().setText(deviceDetails.kernelVersion);
		pathToTunTextView().setText(deviceDetails.pathToTun);
		detailsTextView().setText(deviceDetails.deviceDetails);

		yesIShareCheckBox().setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updateShareButtonState();
			}
		});
		includeDetailsCheckBox().setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updateShareButtonState();
			}
		});
		
		cancelButton().setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                TunNotification.cancelShareTunModule( getApplicationContext() );
				// TODO: remember user did cancel and don't call again (ar ask
				// user if we should call him again)
				finish();
			}
		});

		shareButton().setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				sendViaMail();
				// new SendViaHttp().execute();
			}

			private void sendViaMail() {

				new SendViaHttp().execute();

				// Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
				//
				// String uriText = "mailto:android.tun@schaeuffelhut.de" +
				// "?subject=" + URLEncoder.encode( "TUN Module: " +
				// deviceDetails.kernelVersion) +
				// "&body=" +
				// URLEncoder.encode("Dear Friedrich\n here is a tun.ko working for my device\n");
				// Uri uri = Uri.parse(uriText);
				// sendIntent.setData(uri);
				//
				// sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new
				// String[]{"android.tun@schaeuffelhut.de"});
				// sendIntent.putExtra(Intent.EXTRA_SUBJECT, "TUN Module: " +
				// deviceDetails.kernelVersion );
				// sendIntent.putExtra(Intent.EXTRA_STREAM,
				// Uri.parse("file://"+file) );
				// sendIntent.setType("application/zip");
				//
				// startActivity(Intent.createChooser(sendIntent,
				// "Send email"));
				// // startActivity( sendIntent );
			}
		});

		updateShareButtonState();
	}

    private void updateShareButtonState() {
		shareButton().setEnabled( yesIShareCheckBox().isChecked() && includeDetailsCheckBox().isChecked() );
	}

	private CheckBox yesIShareCheckBox() {
		return (CheckBox) findViewById(R.id.share_tun_moudule_yes_i_share);
	}

	private CheckBox includeDetailsCheckBox() {
		return (CheckBox) findViewById(R.id.share_tun_moudule_include_details);
	}

	private TextView kernelVersionTextView() {
		return (TextView) findViewById(R.id.share_tun_moudule_kernel_version);
	}

	private TextView pathToTunTextView() {
		return (TextView) findViewById(R.id.share_tun_moudule_path_to_tun);
	}

	private TextView detailsTextView() {
		return (TextView) findViewById(R.id.share_tun_moudule_details);
	}

	private Button cancelButton() {
		return (Button) findViewById(R.id.share_tun_moudule_cancel);
	}

	private Button shareButton() {
		return (Button) findViewById(R.id.share_tun_moudule_share);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_UPLOAD_DEVICE_DETAILS: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle("Uploading Device Details");
			return dialog;
		}
		case DIALOG_UPLOAD_SUCCESS: {
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("Thank you for sharing!")
					.setMessage("The upload was succesfull.")
					.setPositiveButton("Close",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									removeDialog(DIALOG_UPLOAD_SUCCESS);
									finish();
								}
							}).create();
		}
		case DIALOG_UPLOAD_FAILED: {
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Upload failed!")
					.setMessage("Please be so kind and try again later.")
					.setPositiveButton("Hmm...",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									removeDialog(DIALOG_UPLOAD_SUCCESS);
								}
							}).create();
		}
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}
