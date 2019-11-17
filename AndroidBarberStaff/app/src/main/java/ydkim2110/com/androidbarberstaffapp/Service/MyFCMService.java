package ydkim2110.com.androidbarberstaffapp.Service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import ydkim2110.com.androidbarberstaffapp.Common.Common;

public class MyFCMService extends FirebaseMessagingService {

    private static final String TAG = MyFCMService.class.getSimpleName();

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Common.updateToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: called!!");
        Common.showNotification(this,
                new Random().nextInt(),
                remoteMessage.getData().get(Common.TITLE_KEY),
                remoteMessage.getData().get(Common.CONTENT_KEY),
                null);
    }
}
