package com.example.usercare.demo;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

public final class UserCareUtils {

	public static boolean isOnline(Context context) {
		boolean connection = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			if (((netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) || (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
				connection = true;
			}
		}
		return connection;
	}

	public static boolean isGooglePlayServicesAvailable(Activity activity) {
		if (activity == null)
			return false;
		GoogleApiAvailability gApiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = gApiAvailability.isGooglePlayServicesAvailable(activity.getApplicationContext());
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 9000).show();
			} else {
				activity.finish();
			}
			return false;
		}
		return true;
	}

}