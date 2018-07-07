/*
 * Copyright (C) 2012 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file was contributed by two forty four a.m. LLC <http://www.twofortyfouram.com>
 * unter the terms of the Apache License, Version 2.0
 */

package uk.co.systemdevelopment.linphone.taskerplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import java.util.List;
import android.widget.Toast;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * This is the "fire" BroadcastReceiver for a Locale Plug-in setting.
 */
public final class FireReceiver extends BroadcastReceiver
{

	private static final String TAG = "FireReceiver";
	public static final String INTENT_NAME = "org.linphone.intent.ACCOUNTACTIVATE";
	public static final String PERM_NAME_USE = "android.permission.USE_SIP";

	/**
	 * @param context {@inheritDoc}.
	 * @param intent the incoming {@link com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING} Intent. This should contain the
	 *            {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was saved by {@link EditActivity} and later broadcast
	 *            by Locale.
	 */
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		/*
		 * Always be sure to be strict on input parameters! A malicious third-party app could always send an empty or otherwise
		 * malformed Intent. And since Locale applies settings in the background, the plug-in definitely shouldn't crash in the
		 * background.
		 */
		 
		 Log.d(TAG, "Received intent");

		/*
		 * Locale guarantees that the Intent action will be ACTION_FIRE_SETTING
		 */
		if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
		{
			Log.d(TAG, "Ignoring intent as [" + intent.getAction() + "] does not match [" + com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING + "]");
			return;
		}

		/*
		 * A hack to prevent a private serializable classloader attack
		 */
		BundleScrubber.scrub(intent);
		BundleScrubber.scrub(intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE));

		final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);

		boolean deactivate = bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOL_DEACTIVATE);

		long account_id = bundle.getLong(PluginBundleManager.BUNDLE_EXTRA_INT_ACCOUNT_ID);

		Log.i(TAG, "Retrieved Bundle: " + bundle.toString());

		Log.d(TAG, "Preparing to broadcast data to " + INTENT_NAME);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			int permissionCheck = context.checkSelfPermission(PERM_NAME_USE);
			if (permissionCheck == PackageManager.PERMISSION_DENIED)
			{
				Toast.makeText(context, "Use SIP not permitted, but is required to configure Linphone. Edit Action: Configuration, or enable in Application Manager: Linphone plugin: Permissions", Toast.LENGTH_LONG).show();
				return;
			}
		}
		Intent broadcastIntent = new Intent(INTENT_NAME);
		broadcastIntent.putExtra("id", account_id);
		broadcastIntent.putExtra("active", !deactivate);
		FireReceiver.sendImplicitBroadcast(context, broadcastIntent);
	}
	
	private static void sendImplicitBroadcast(Context ctxt, Intent i) {
	  PackageManager pm=ctxt.getPackageManager();
	  List<ResolveInfo> matches=pm.queryBroadcastReceivers(i, 0);

	  for (ResolveInfo resolveInfo : matches) {
		Intent explicit=new Intent(i);
		ComponentName cn=
		  new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
			resolveInfo.activityInfo.name);

		explicit.setComponent(cn);
		ctxt.sendBroadcast(explicit);
	  }
	}
}
