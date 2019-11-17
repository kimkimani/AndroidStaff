package ydkim2110.com.androidbarberstaffapp.Interface;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

import ydkim2110.com.androidbarberstaffapp.Model.MyNotification;

public interface INotificationLoadListener {
    void onNotificationLoadSuccess(List<MyNotification> myNotificationList, DocumentSnapshot lastDocument);
    void onNotificationLoadFailed(String message);
}
