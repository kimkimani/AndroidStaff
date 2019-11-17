package ydkim2110.com.androidbarberstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ydkim2110.com.androidbarberstaffapp.Adapter.MyNotificationAdapter;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Interface.INotificationLoadListener;
import ydkim2110.com.androidbarberstaffapp.Model.MyNotification;

public class NotificationActivity extends AppCompatActivity implements INotificationLoadListener {

    private static final String TAG = NotificationActivity.class.getSimpleName();

    @BindView(R.id.recycler_notification)
    RecyclerView recycler_notification;

    private CollectionReference notificationCollection;

    private INotificationLoadListener mINotificationLoadListener;

    private int total_item = 0;
    private int last_visible_item;
    private boolean isLoading = false;
    private boolean isMaxData = false;

    private DocumentSnapshot finalDoc;
    private MyNotificationAdapter mAdapter;
    private List<MyNotification> firstList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(this);

        init();
        initView();

        loadNotification(null);
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        recycler_notification.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_notification.setLayoutManager(layoutManager);
        recycler_notification.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        recycler_notification.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                total_item = layoutManager.getItemCount();
                last_visible_item = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && total_item <= (last_visible_item + Common.MAX_NOTIFICATION_PER_LOAD)) {
                    loadNotification(finalDoc);
                    isLoading = true;
                }
            }
        });
    }

    private void loadNotification(DocumentSnapshot lastDoc) {
        Log.d(TAG, "loadNotification: called!!");
        ///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel

        notificationCollection = FirebaseFirestore.getInstance()
                .collection("gender")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Hostel")
                .document(Common.currentBarber.getBarberId())
                .collection("Notifications");

        if (lastDoc == null) {
            Log.d(TAG, "loadNotification: lastDoc == null");
            notificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                    .limit(Common.MAX_NOTIFICATION_PER_LOAD)
                    .get()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mINotificationLoadListener.onNotificationLoadFailed(e.getMessage());
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, "onComplete: called!!");
                            if (task.isSuccessful()) {
                                List<MyNotification> myNotifications = new ArrayList<>();
                                DocumentSnapshot finalDoc = null;
                                for (DocumentSnapshot notiSnapshot : task.getResult()) {
                                    MyNotification myNotification = notiSnapshot.toObject(MyNotification.class);
                                    myNotifications.add(myNotification);
                                    finalDoc = notiSnapshot;
                                }
                                mINotificationLoadListener.onNotificationLoadSuccess(myNotifications, finalDoc);
                            }
                        }
                    });
        }
        else {
            if (!isMaxData) {
                notificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                        .startAfter(lastDoc)
                        .limit(Common.MAX_NOTIFICATION_PER_LOAD)
                        .get()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mINotificationLoadListener.onNotificationLoadFailed(e.getMessage());
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<MyNotification> myNotifications = new ArrayList<>();
                                    DocumentSnapshot finalDoc = null;
                                    for (DocumentSnapshot notiSnapshot : task.getResult()) {
                                        MyNotification myNotification = notiSnapshot.toObject(MyNotification.class);
                                        myNotifications.add(myNotification);
                                        finalDoc = notiSnapshot;
                                    }
                                    mINotificationLoadListener.onNotificationLoadSuccess(myNotifications, finalDoc);
                                }
                            }
                        });
            }
        }
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mINotificationLoadListener = this;
    }

    @Override
    public void onNotificationLoadSuccess(List<MyNotification> myNotificationList, DocumentSnapshot lastDocument) {
        Log.d(TAG, "onNotificationLoadSuccess: called!!");
        if (lastDocument != null) {
            if (lastDocument.equals(finalDoc)) {
                isMaxData = true;
            }
            else {
                finalDoc = lastDocument;
                isMaxData = false;
            }

            if (mAdapter == null && firstList.size() == 0) {
                mAdapter = new MyNotificationAdapter(this, myNotificationList);
                firstList = myNotificationList;
            }
            else {
                if (!myNotificationList.equals(firstList)) {
                    mAdapter.updateList(myNotificationList);
                }
            }

            recycler_notification.setAdapter(mAdapter);
        }
    }

    @Override
    public void onNotificationLoadFailed(String message) {
        Log.d(TAG, "onNotificationLoadFailed: called!!");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
