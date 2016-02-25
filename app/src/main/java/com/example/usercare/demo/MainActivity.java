package com.example.usercare.demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.example.usercare.demo.gcm.RegistrationIntentService;
import com.example.usercare.demo.purchase.IabHelper;
import com.usercare.callbacks.UserCareErrorCallback;
import com.usercare.callbacks.UserCareMessagingCallbacks;
import com.usercare.callbacks.UserCareSdkInitializationFinishedListener;
import com.usercare.events.EventsTracker;
import com.usercare.gcm.UserCareGcmHandler;
import com.usercare.managers.UserCareAppStatusManager;
import com.usercare.messaging.MessagingActivity;
import com.usercare.messaging.entities.ActionEntity;

public class MainActivity extends AppCompatActivity implements
        UserCareMessagingCallbacks, UserCareErrorCallback, UserCareSdkInitializationFinishedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String CUSTOMER_ID = "";
    private static final String APP_KEY = "YOUR APP KEY";

    private Context mContext;

    private PurchaseHelper mPurchaseHelper;
    private IabHelper mHelper;
    private UserCareAppStatusManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        setupBuildVersion();
        setupUserCareControllers();
        setupPurchaseHelper();

        if (savedInstanceState == null) {
            setupPushNotifications();
            setupInAppActions();
        }
    }

    private void setupUserCareControllers() {
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBottomSheet();
            }
        });
    }

    private void setupBottomSheet() {
        new BottomSheet.Builder(this, R.style.BottomSheet_StyleDialog).sheet(R.menu.menu_main).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case R.id.btn_update_usercare_status:
                        mManager = new UserCareAppStatusManager(MainActivity.this, CUSTOMER_ID, APP_KEY);
                        mManager.updateAppStatus();
                        break;
                    case R.id.buyClickButton:
//                        mPurchaseHelper.makePurchase();
                        sendPurchaseEvent();
                        break;
                    case R.id.customEventButton:
                        setupCustomEvent();
                        break;
                    case R.id.crashEventButton:
                        throw new NullPointerException();
                }
            }
        }).show();
    }

    private void sendPurchaseEvent() {
        EventsTracker eventsTracker = new EventsTracker(mContext);
        eventsTracker.setSkuDetails("com.example.usercare.demo.click", "Sample Title", "26.55USD", "USD");
        eventsTracker.sendPurchaseEvent("com.example.usercare.demo.click", "GPA.1384-6541-2372-70552", System.currentTimeMillis());
        Toast.makeText(this, "Purchase successful", Toast.LENGTH_LONG).show();
    }

    private void setupCustomEvent() {
        String jsonBody = "{\"key1\", \"value1\", \"key2\", \"value2\", \"key3\", \"value3\"}";
        new EventsTracker(mContext).sendCustomEvent("custom_event", jsonBody);
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
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
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
    }

    private void openActivityDirectly() {
        startActivity(new Intent(this, MessagingActivity.class)); // or MyTicketsActivity / FaqActivity / LandingPageActivity
    }

    @Override
    public void usercareSdkFailedWithError(int i, Throwable throwable) {
        Log.d(TAG, "usercareSdkFailedWithError statusCode = " + i);
        Log.d(TAG, "usercareSdkFailedWithError error = " + throwable.getMessage());
    }

    @Override
    public void usercareSdkFailedWithError(Throwable throwable) {
        Log.d(TAG, "usercareSdkFailedWithError error = " + throwable.getMessage());
    }

    @Override
    public void onActionMessageReceived(ActionEntity actionEntity) {
        Log.d(TAG, "actionEntity.getBonusText() = " + actionEntity.getBonusText());
        Log.d(TAG, "actionEntity.getBonusImageUrl() = " + actionEntity.getBonusImageUrl());
    }

    @Override
    public void onSystemMessageReceived(String s) {
        Log.d(TAG, "onSystemMessageReceived = " + s);
    }

    @Override
    public void onSupporterMessageReceived(String s) {
        Log.d(TAG, "onSupporterMessageReceived = " + s);
    }

    @Override
    public void usercareSdkInitializationFinished(boolean b) {
        Log.d(TAG, "usercareSdkInitializationFinished = " + b);
    }

}