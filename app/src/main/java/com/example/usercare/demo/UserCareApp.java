package com.example.usercare.demo;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.usercare.UserCare;

import io.fabric.sdk.android.Fabric;

public class UserCareApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		/**
		 * option 1
		 */
		UserCare.init(this)
				//.withEventsTracker("YOUR EVENTS API KEY HERE")
				.withExceptionHandler();
		/**
		 * option 2
		 */
		/*UserCare.init(this, new Configuration.Builder()
				// DeviceIdMode : ANDROID_ID_MODE, HARDWARE_SERIAL_MODE, ADVERTISING_MODE
				.setDeviceIdMode(DeviceIdMode.ANDROID_ID_MODE)
				.setApiKey("YOUR_API_KEY")
				.setCustomerId("YOUR_CUSTOMER_ID")
				.setAppId("YOUR_APP_ID")
				.setGcmPushToken("YOUR_GCM_TOKEN")
				.create());*/
	}

}