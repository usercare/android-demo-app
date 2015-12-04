package com.example.usercare.demo;

import com.usercare.gcm.UserCareInAppActionsListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Sample service implementation for handling in-app action callbacks
 */
public class ActionsListener extends UserCareInAppActionsListener {

    @Override
    public void onCreate() {
        super.onCreate();
        Set<String> actions = new HashSet<>();
        actions.add("grantBonus");
        actions.add("non-grantBonus");
        registerAppForActions(actions);
    }

    @Override
    public void onActionCallbackReceived(String callbackName, String callbackData) {
        // TODO: Handle as you wish
    }

}