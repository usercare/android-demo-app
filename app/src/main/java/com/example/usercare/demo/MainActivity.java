package com.example.usercare.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.usercare.demo.gcm.RegistrationIntentService;
import com.example.usercare.demo.purchase.IabHelper;
import com.example.usercare.demo.purchase.IabResult;
import com.example.usercare.demo.purchase.Inventory;
import com.example.usercare.demo.purchase.Purchase;
import com.usercare.callbacks.UserCareErrorCallback;
import com.usercare.callbacks.UserCareMessagingCallbacks;
import com.usercare.events.EventsTracker;
import com.usercare.gcm.UserCareGcmHandler;
import com.usercare.managers.UserCareAppStatusManager;
import com.usercare.messaging.MessagingActivity;
import com.usercare.messaging.entities.ActionEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements View.OnClickListener, UserCareMessagingCallbacks, UserCareErrorCallback {

    private static final String TAG = "GCM Demo";
    private static final String BASE64_ENCODED_KEY = "INPUT YOUR BASE64 ENDCODED KEY HERE";
    private static final String ITEM_SKU = "INPUT YOUR ITEM SKU HERE";
    private static final int PURCHASE_REQUEST_CODE = 100001;
    private static final String CUSTOMER_ID = "";
    private static final String API_KEY = "YOUR APP KEY";

    private Context mContext;
    private IabHelper mHelper;
    private UserCareAppStatusManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        TextView userInfo = (TextView) findViewById(R.id.userInfoTextView);
        userInfo.setText("version: " + BuildConfig.VERSION_NAME + " (build: " + getString(R.string.uc_build_number) + ")");

        setupPurchaseHelper();
        setupUserCareControllers();

        if (UserCareUtils.isGooglePlayServicesAvailable(this)) {
            if (UserCareGcmHandler.init(mContext).isPushTokenAvailable()) {
                registerAppForPushNotifications();
            }
        }

        startService(new Intent(mContext, ActionsListener.class));
    }

    private void setupPurchaseHelper() {
        final List<String> skus = new ArrayList<>();
        skus.add(ITEM_SKU);

        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, BASE64_ENCODED_KEY);
        mHelper.enableDebugLogging(true);

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                if (mHelper == null) return;

                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(true, skus, mGotInventoryListener);
            }
        });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            Purchase gasPurchase = inventory.getPurchase(ITEM_SKU);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                Log.d(TAG, "We have sku. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU), mConsumeFinishedListener);
                return;
            }
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    private void setupUserCareControllers() {
        findViewById(R.id.btn_update_usercare_status).setOnClickListener(this);
        findViewById(R.id.buyClickButton).setOnClickListener(this);
        findViewById(R.id.customEventButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_update_usercare_status:
                mManager = new UserCareAppStatusManager(this, CUSTOMER_ID, API_KEY);
//                mManager.setUserProperties("John", "Doe", "test@test.com");
                mManager.updateAppStatus();
                break;
            case R.id.buyClickButton:
                findViewById(R.id.buyClickButton).setEnabled(false);
                Log.d(TAG, "Button clicked.");
                Log.d(TAG, "Launching purchase flow");
                mHelper.launchPurchaseFlow(this, ITEM_SKU, PURCHASE_REQUEST_CODE, mPurchaseFinishedListener, "");
                break;
            case R.id.customEventButton:
                setupCustomEvent();
                break;
            default:
                break;
        }
    }

    private void setupCustomEvent() {
        Map<String, String> customEventMap = new HashMap<>();
        customEventMap.put("key1", "value1");
        customEventMap.put("key2", "value2");
        customEventMap.put("key3", "value3");
        EventsTracker.sendCustomEvent("custom_event", customEventMap);
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

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(ITEM_SKU)) {
                Log.d(TAG, "Purchase is " + ITEM_SKU);
                EventsTracker.sendPurchaseEvent(purchase.getSku(), purchase.getOrderId(), purchase.getPurchaseTime());
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            if (mHelper == null) return;

            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
            }
            else {
                complain("Error while consuming: " + result);
            }
            Log.d(TAG, "End consumption flow.");
        }
    };

    private void registerAppForPushNotifications() {
        if (UserCareUtils.isOnline(mContext)) {
            Intent intent = new Intent(mContext, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void complain(String message) {
        Log.e(TAG, "**** Purchase Error: " + message);
        EventsTracker.sendPurchaseFailedEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mManager != null) {
            mManager.clear();
        }
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
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

}