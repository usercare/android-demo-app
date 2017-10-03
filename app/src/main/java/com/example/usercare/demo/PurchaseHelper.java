package com.example.usercare.demo;

import android.app.Activity;
import android.util.Log;

import com.example.usercare.demo.purchase.IabHelper;
import com.example.usercare.demo.purchase.IabResult;
import com.example.usercare.demo.purchase.Inventory;
import com.example.usercare.demo.purchase.Purchase;
import com.usercare.UserCare;
import com.usercare.events.EventsManager;

import java.util.ArrayList;
import java.util.List;

public class PurchaseHelper {

    private static final String TAG = PurchaseHelper.class.getSimpleName();
    private static final String BASE64_ENCODED_KEY = "INPUT YOUR BASE64 ENCODED KEY HERE";
    private static final String ITEM_SKU = "android.test.purchased";
    private static final int PURCHASE_REQUEST_CODE = 100001;

    private Activity mActivity;
    private IabHelper mHelper;
    private final EventsManager eventsManager;

    public PurchaseHelper(Activity activity) {
        mActivity = activity;
        this.eventsManager = UserCare.getInstance().getDependencyManager().getSingleAppComponent().getEventsManager();
    }

    public IabHelper setupPurchaseHelper() {
        final List<String> skus = new ArrayList<>();
        skus.add(ITEM_SKU);

        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(mActivity, BASE64_ENCODED_KEY);
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
        return mHelper;
    }

    public void makePurchase() {
        Log.d(TAG, "Button clicked.");
        Log.d(TAG, "Launching purchase flow");
        mHelper.launchPurchaseFlow(mActivity, ITEM_SKU, PURCHASE_REQUEST_CODE, mPurchaseFinishedListener, "");
    }

    /** Verifies the developer payload of a purchase. */
    private boolean verifyDeveloperPayload(Purchase p) {
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

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result, purchase);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.", purchase);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(ITEM_SKU)) {
                Log.d(TAG, "Purchase is " + ITEM_SKU);
                eventsManager.sendPurchaseEvent(purchase.getSku(), purchase.getOrderId(), purchase.getPurchaseTime());
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
        }
    };

    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
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

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
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

    private void complain(String message) {
        complain(message, null);
    }

    private void complain(String message, Purchase purchase) {
        Log.e(TAG, "**** Purchase Error: " + message);
        if (purchase != null) {
            eventsManager.sendPurchaseFailedEvent(purchase.getSku(), purchase.getOrderId(), purchase.getPurchaseTime());
        }
    }

    public void destroy() {
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

}