package com.example.usercare.demo;

import android.util.Log;

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
        actions.add("MyGrantBonusUserCare_01");
        actions.add("MyGrantBonusUserCare.01.LongLongLongLongLongLongName");
        actions.add("x-~!@#$%^&*()name");
        registerAppForActions(actions);
    }

    @Override
    public void onActionCallbackReceived(String callbackName, String callbackData) {
        // TODO: Handle as necessary
        Log.d(ActionsListener.class.getSimpleName(),"ActionsListener -> onActionCallbackReceived : callbackName = " + callbackName + " callbackData = " + callbackData);
    }

}