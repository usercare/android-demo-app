package com.example.usercare.demo;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.crashlytics.android.core.CrashlyticsListener;
import com.usercare.UserCare;
import com.usercare.crash.crashlytics.CrashlyticsManager;
import com.usercare.crashlytics.CrashlyticsController;

import io.fabric.sdk.android.Fabric;

public class UserCareApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		/**
		 * option 1
		 */
		UserCare.init(this)
				//.withEventsTracker("YOUR EVENTS API KEY HERE")
				.withExceptionHandler();
		/**
		 * option 2
		 */
/*		UserCare.init(this, new Configuration.Builder()
				// DeviceIdMode : ANDROID_ID_MODE, HARDWARE_SERIAL_MODE, ADVERTISING_MODE
				.setDeviceIdMode(DeviceIdMode.ANDROID_ID_MODE)
				.setApiKey("YOUR_UC_API_KEY")  // or in manifest
				.setCustomerId("YOUR_CUSTOMER_ID")
				.setAppId("YOUR_UC_APP_ID")  // or in manifest
				.setGcmPushToken("YOUR_GCM_TOKEN")
				.setInitializeListener(new UserCareAppStatusNetworkManager.InitializeListener() {
					@Override
					public UserCareAppSettings onInitDevice(UserCareAppSettings userCareAppSettings) {
						// some developer customization (UI) ....
						if (!userCareAppSettings.isValid()) {
							// check valid your changes. If not , please fix it ... (just look at LogCat)
						}
						return userCareAppSettings;
					}
				})
				.create());*/

		// Init Crashlytics. Optional
		CrashlyticsListener crashlyticsListener = new CrashlyticsListener() {
			@Override
			public void crashlyticsDidDetectCrashDuringPreviousExecution() {
				CrashlyticsManager.crashlyticsDidDetectCrashDuringPreviousExecution();
			}
		};
		CrashlyticsCore crashlyticsCore = new CrashlyticsCore
				.Builder()
				.listener(crashlyticsListener)
				.build();
		Fabric.with(getApplicationContext(), new Crashlytics.Builder().core(crashlyticsCore).build());
		CrashlyticsController.setCrashId(CrashlyticsManager.generateCrashlyticsIdKey());
	}

}