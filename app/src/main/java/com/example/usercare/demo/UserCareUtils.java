package com.example.usercare.demo;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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
		if (activity != null && !activity.isFinishing()) {
			int connectionResult = isGooglePlayServicesAvailable(activity.getApplicationContext());
			if (connectionResult != ConnectionResult.SUCCESS) {
				GoogleApiAvailability gApiAvailability = GoogleApiAvailability.getInstance();
				if (gApiAvailability.isUserResolvableError(connectionResult)) {
					gApiAvailability.getErrorDialog(activity, connectionResult, -1).show();
				} else {
					activity.finish();
				}
				return false;
			}
		}
		return true;
	}

	public static int isGooglePlayServicesAvailable(Context context) {
		if (context != null) {
			GoogleApiAvailability gApiAvailability = GoogleApiAvailability.getInstance();
			return gApiAvailability.isGooglePlayServicesAvailable(context);
		}
		return ConnectionResult.DEVELOPER_ERROR;
	}

}