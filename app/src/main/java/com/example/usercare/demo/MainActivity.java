package com.example.usercare.demo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usercare.demo.gcm.RegistrationIntentService;
import com.example.usercare.demo.purchase.IabHelper;
import com.google.gson.annotations.SerializedName;
import com.usercare.UserCare;
import com.usercare.cache.UserCareCacheSettings;
import com.usercare.callbacks.error.ErrorEntity;
import com.usercare.callbacks.message.MessageEntity;
import com.usercare.events.EventsTracker;
import com.usercare.gcm.UserCareGcmHandler;
import com.usercare.managers.UserCareAppStatusManager;
import com.usercare.managers.UserCareCallbackManager;
import com.usercare.messaging.ActionEntity;
import com.usercare.messaging.LiveChatSystemMessage;
import com.usercare.messaging.MessagingActivity;
import com.usercare.messaging.UserCareMessagingClient;
import com.usercare.network.socket.OnSocketConnectedListener;
import com.usercare.network.socket.SocketIOClientListener;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private Context mContext;

	private PurchaseHelper mPurchaseHelper;
	private IabHelper mHelper;
	private UserCareAppStatusManager mManager;
	private UserCareMessagingClient mUserCareMessagingClient;
	private UserCareCacheSettings mUserCareCacheSettings;

	private DrawerLayout mDrawerLayout;
	private FrameLayout mSideFrameLayout;

	private int mNewMessageCounter = 0;
	private com.usercare.system.Configuration configuration;
	private Subscription sdkInitializationSubscription;
	private Subscription sdkErrorSubscription;
	private Subscription sdkMassageSubscription;
	private NestedScrollView bottomSheet;
	private BottomSheetBehavior<NestedScrollView> bottomSheetBehavior;
	private View.OnClickListener bottomSheetClickListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
		setContentView(R.layout.activity_main);
		mContext = getApplicationContext();
		configuration = UserCare.getInstance().getConfiguration();

		mUserCareCacheSettings = new UserCareCacheSettings(mContext);
		setupBuildVersion();
		setupSidebar();
		setupUserCareControllers();
		setupPurchaseHelper();
		initBottomSheet();

		if (savedInstanceState == null) {
			setupPushNotifications();
			setupInAppActions();
		}
		initCallBacksSDK();
		getAppStatusManager().setChatTags(null);
	}

	@NonNull
	private UserCareAppStatusManager getAppStatusManager() {
		if (mManager == null) {
			mManager = new UserCareAppStatusManager(MainActivity.this, configuration.getCustomerId(), configuration.getAppId());
		}
		return mManager;
	}

	private void initCallBacksSDK() {

		// Just for test callBacks:
		UserCareCallbackManager userCareCallbackManager = UserCareCallbackManager.getInstance();

		sdkMassageSubscription = userCareCallbackManager.getSdkMessagingSubscription(new Observer<Object>() {
			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(Throwable e) {
				Log.e(TAG, " Messagin subscription error : " + e.toString());
			}

			@Override
			public void onNext(Object object) {
				if (object != null) {
					if (object instanceof ActionEntity) {
						ActionEntity actionEntity = null;
						try {
							actionEntity = (ActionEntity) object;
						} catch (ClassCastException e) {
							Log.e(TAG, " Couldn't cast Object class to ActionEntity : " + e.toString());
						}
						if (actionEntity != null) {
							Log.d(TAG, " Message callBack : ActionText = " + actionEntity.getActionText()
									+ " ActionTimestamp = " + actionEntity.getActionTimestamp());
						}
					} else {
						MessageEntity messageEntity = null;
						try {
							messageEntity = (MessageEntity) object;
						} catch (ClassCastException e) {
							Log.e(TAG, " Couldn't cast Object class to MessageEntity : " + e.toString());
						}
						if (messageEntity != null) {
							String logMsg = " Message callBack : MessageText = " + messageEntity.getMessage()
									+ "; ActionTimestamp = " + messageEntity.getTimestamp() + "; Message Type = " + messageEntity.getType();
							if (messageEntity.getChatMessageContent() != null) {
								logMsg = logMsg.concat(" ").concat(messageEntity.getChatMessageContent().toString());
							}
							Log.d(TAG, logMsg);
						}

					}
				}
			}
		}, Schedulers.computation());

		sdkInitializationSubscription = userCareCallbackManager.getSdkInitializationSubscription(new Observer<Boolean>() {
			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(Throwable e) {
				Log.e(TAG, " Error sdk initialization subscription : " + e.toString());
			}

			@Override
			public void onNext(Boolean result) {
				Log.d(TAG, " SdkInitialization result = " + result);
				if (mManager != null) {
					Log.d(TAG," Is tickets exist: " + mManager.isTicketsExist());
				}
			}
		});

		sdkErrorSubscription = userCareCallbackManager.getSdkErrorSubscription(new Observer<Throwable>() {
			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(Throwable e) {
				Log.e(TAG, " Error sdk error subscription " + e.toString());
			}

			@Override
			public void onNext(Throwable throwable) {
				if (throwable != null) {
					if (throwable instanceof ErrorEntity) {
						ErrorEntity errorEntity = (ErrorEntity) throwable;
						Log.d(TAG, " Error sdk result = " + errorEntity.toString() + " ; status code = " + errorEntity.getStatusCode());
					} else {
						Log.d(TAG, " Error sdk result = " + throwable.toString());
					}
				}
			}
		});
	}

	private void setupUserCareControllers() {

		findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setupBottomSheet();
			}
		});
		findViewById(R.id.testSettingsButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawerLayout.openDrawer(GravityCompat.END);
			}
		});
		findViewById(R.id.crossButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawerLayout.closeDrawer(GravityCompat.END);
			}
		});
	}

	public void onRadioButtonClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		switch (view.getId()) {
			case R.id.radioButtonAll:
				if (checked)
					mUserCareCacheSettings.setCacheMode(UserCareCacheSettings.CACHE_MODE_ALL);
				break;
			case R.id.radioButtonNothing:
				if (checked)
					mUserCareCacheSettings.setCacheMode(UserCareCacheSettings.CACHE_MODE_NOTHING);
				break;
			case R.id.radioButtonFaqs:
				if (checked)
					mUserCareCacheSettings.setCacheMode(UserCareCacheSettings.CACHE_MODE_FAQS);
				break;
			default:
				break;
		}
	}

	private void initBottomSheet() {
		bottomSheet = (NestedScrollView) findViewById(R.id.bottom_sheet);
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

		bottomSheetClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (view != null) {
					bottomSheetClicked(view.getId());
				}
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
			}
		};

		findViewById(R.id.btn_update_usercare_status).setOnClickListener(bottomSheetClickListener);
		findViewById(R.id.buyClickButton).setOnClickListener(bottomSheetClickListener);
		findViewById(R.id.customEventButton).setOnClickListener(bottomSheetClickListener);
		findViewById(R.id.crashEventButton).setOnClickListener(bottomSheetClickListener);
		findViewById(R.id.sendChatMessageButton).setOnClickListener(bottomSheetClickListener);
		findViewById(R.id.clearCachedData).setOnClickListener(bottomSheetClickListener);
	}

	private void bottomSheetClicked(int bottomSheetBtnId) {
		switch (bottomSheetBtnId) {
			case R.id.btn_update_usercare_status:
				mManager = getAppStatusManager();
				//mManager.setUserProperties("John", "Doe", "test@test.com");
				//mManager.setUserProperty("custom_property_key", "custom_property_value");
				mManager.setNeedCheckPushNotifications(true);
				mManager.updateAppStatus();
				/**
				 * Custom local option disable Settings user in Message View on open
				 */
				//mManager.setShouldShowUserSettingsView(false);
				/**
				 * Custom local option hide Settings button in Message View
				 */
				//mManager.setShouldShowSettingsBtn(false);
				break;
			case R.id.buyClickButton:
				// mPurchaseHelper.makePurchase();
				sendPurchaseEvent();
				break;
			case R.id.customEventButton:
				setupCustomEvent();
				break;
			case R.id.crashEventButton:
				throw new NullPointerException();
			case R.id.sendChatMessageButton:
				setupMessagingClient();
				break;
			case R.id.clearCachedData:
				mUserCareCacheSettings.clearApplicationData();
				break;
		}
	}

	private void setupBottomSheet() {
		if (bottomSheetBehavior != null) {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		}
	}

	private void setupMessagingClient() {
		if (mUserCareMessagingClient == null) {
			mUserCareMessagingClient = new UserCareMessagingClient(mContext, mSocketIOClientListener, mOnSocketConnectedListener);
		}
		if (!mUserCareMessagingClient.isConnected()) {
			mUserCareMessagingClient.connect();
		} else {
			mUserCareMessagingClient.sendMessage("New message " + String.valueOf(mNewMessageCounter++));
		}
	}

	private SocketIOClientListener mSocketIOClientListener = new SocketIOClientListener() {

		@Override
		public void messageSent(LiveChatSystemMessage message) {
			Log.i(TAG, "messageSent =" + message.getText() + " " + message.getFromRoles() + " " + message.getTimestamp());
		}

		@Override
		public void newMessage(LiveChatSystemMessage message) {
			switch (message.getType()) {
				case LiveChatSystemMessage.TYPE_REGULAR_SYSTEM_MESSAGE:
					Log.i(TAG, "TYPE_REGULAR_SYSTEM_MESSAGE = " + message.getText() + " " + message.getFromRoles() + " " + message.getTimestamp());
					break;
				case LiveChatSystemMessage.TYPE_BONUS_PRESENTED_MESSAGE:
					Log.i(TAG, "TYPE_BONUS_PRESENTED_MESSAGE = " + message.getText() + " " + message.getImageUrl() + " " + message.getTimestamp());
					break;
				case LiveChatSystemMessage.TYPE_AGENT_JOINED_MESSAGE:
				case LiveChatSystemMessage.TYPE_AGENT_LEFT_MESSAGE:
					Log.i(TAG, "TYPE_AGENT_LEFT_MESSAGE / TYPE_AGENT_JOINED_MESSAGE = " + message.getText() + " " + message.getTimestamp());
					break;
				case LiveChatSystemMessage.TYPE_TICKET_CLOSED:
					mUserCareMessagingClient.resetConversationId();
					Log.i(TAG, "TYPE_TICKET_CLOSED = " + message.getText() + " " + message.getTimestamp());
					break;
				case LiveChatSystemMessage.TYPE_CANNED_RESPONSE:
					Log.i(TAG, "TYPE_CANNED_RESPONSE = " + message.getText() + " " + message.getTimestamp());
					break;
				default:
					break;
			}
		}
	};

	private OnSocketConnectedListener mOnSocketConnectedListener = new OnSocketConnectedListener() {
		@Override
		public void onSocketConnected() {
			if (mUserCareMessagingClient.isConnected()) {
				mUserCareMessagingClient.sendMessage("New message " + String.valueOf(mNewMessageCounter++));
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		if (mUserCareMessagingClient != null) {
			mUserCareMessagingClient.disconnect();
		}
	}

	private void sendPurchaseEvent() {
		List<String> tags = new ArrayList<>();
		tags.add("Purchase event sent");
		mManager.setChatTags(tags);
		EventsTracker eventsTracker = new EventsTracker(mContext);
		eventsTracker.setSkuDetails("com.example.usercare.demo.click", "Sample Title", "26.55USD", "USD");
		eventsTracker.sendPurchaseEvent("com.example.usercare.demo.click", "GPA.1384-6541-2372-70552", System.currentTimeMillis());
		Toast.makeText(this, "Purchase successful", Toast.LENGTH_LONG).show();
	}

	private void setupCustomEvent() {
		Observable<Boolean> customEventObs = new EventsTracker(mContext)
				.sendCustomEventRx("custom_event_gson_rx", CustomGsonEvenet.getDemo());
		if (customEventObs != null) {
			customEventObs.subscribeOn(Schedulers.computation())
					.subscribe(new Observer<Boolean>() {
						@Override
						public void onCompleted() {
							Log.d(TAG, " onCompleted ");
						}

						@Override
						public void onError(Throwable e) {
							e.printStackTrace();
						}

						@Override
						public void onNext(Boolean aBoolean) {
							Log.d(TAG, "onNext aBoolean = " + aBoolean);
						}
					});
		}
	}

	private void setupBuildVersion() {
		TextView userInfoTextView = (TextView) findViewById(R.id.userInfoTextView);
		userInfoTextView.setText("\nversion: " + BuildConfig.VERSION_NAME + " (build: " + getString(R.string.uc_build_number) + ")");
	}

	private void setupPushNotifications() {
		if (UserCareUtils.isGooglePlayServicesAvailable(this)) {
			if (UserCareGcmHandler.init(mContext).isPushTokenAvailable()) {
				registerAppForPushNotifications();
			}
		}
	}

	private void setupInAppActions() {
		startService(new Intent(mContext, ActionsListener.class));
	}

	private void registerAppForPushNotifications() {
		if (UserCareUtils.isOnline(mContext)) {
			Intent intent = new Intent(mContext, RegistrationIntentService.class);
			startService(intent);
		}
	}

	private void setupSidebar() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		mSideFrameLayout = (FrameLayout) findViewById(R.id.sideContainerLayout);
		setupSidebarWidth();
	}

	private void setupSidebarWidth() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		ViewGroup.LayoutParams params = mSideFrameLayout.getLayoutParams();
		params.width = size.x;
		mSideFrameLayout.setLayoutParams(params);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setupSidebarWidth();
	}

	private void setupPurchaseHelper() {
		mPurchaseHelper = new PurchaseHelper(this);
		mHelper = mPurchaseHelper.setupPurchaseHelper();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
		if (mHelper == null) return;

		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			Log.d(TAG, "onActivityResult handled by IABUtil.");
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
			mDrawerLayout.closeDrawer(GravityCompat.END);
		} else if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mManager != null) {
			mManager.clear();
		}
		if (mPurchaseHelper != null) {
			mPurchaseHelper.destroy();
		}
		if (sdkInitializationSubscription != null && !sdkInitializationSubscription.isUnsubscribed()) {
			sdkInitializationSubscription.unsubscribe();
		}

		if (sdkErrorSubscription != null && !sdkErrorSubscription.isUnsubscribed()) {
			sdkErrorSubscription.unsubscribe();
		}

		if (sdkMassageSubscription != null && !sdkMassageSubscription.isUnsubscribed()) {
			sdkMassageSubscription.unsubscribe();
		}

		bottomSheet = null;
		bottomSheetBehavior = null;
		bottomSheetClickListener = null;
		sdkErrorSubscription = null;
		sdkInitializationSubscription = null;
		sdkMassageSubscription = null;
	}

	private void openActivityDirectly() {
		startActivity(new Intent(this, MessagingActivity.class)); // or MyTicketsActivity / FaqActivity / LandingPageActivity
	}

	private static class CustomGsonEvenet {

		@SerializedName("event_id")
		private int eventId;

		@SerializedName("event_message")
		private String eventMessage;

		static CustomGsonEvenet getDemo() {
			CustomGsonEvenet demo = new CustomGsonEvenet();
			demo.eventId = 25574;
			demo.eventMessage = "CustomGsonEvenet Demo message for test";
			return demo;
		}
	}
}