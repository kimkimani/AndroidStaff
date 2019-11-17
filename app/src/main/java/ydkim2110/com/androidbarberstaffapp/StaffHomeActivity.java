package ydkim2110.com.androidbarberstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ydkim2110.com.androidbarberstaffapp.Adapter.MyTimeSlotAdapter;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Common.SpacesItemDecoration;
import ydkim2110.com.androidbarberstaffapp.Interface.INotificationCountListener;
import ydkim2110.com.androidbarberstaffapp.Interface.ITimeSlotLoadListener;
import ydkim2110.com.androidbarberstaffapp.Model.BookingInfomation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener, INotificationCountListener {

    private static final String TAG = "StaffHomeActivity";

    @BindView(R.id.activity_main)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;

    ActionBarDrawerToggle mActionBarDrawerToggle;

    DocumentReference barberDoc;
    ITimeSlotLoadListener mITimeSlotLoadListener;
    android.app.AlertDialog mDialog;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;

    private TextView txt_notification_badge;
    private TextView txt_barber_name;

    CollectionReference notificationCollection;
    CollectionReference currentBookDateCollection;

    EventListener<QuerySnapshot> notificationEvent;
    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration notificationListener;
    ListenerRegistration bookingRealtimeListener;

    INotificationCountListener mINotificationCountListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(this);

        init();

        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.staff_home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_new_notification);

        txt_notification_badge = menuItem.getActionView().findViewById(R.id.notification_badge);

        loadNotification();

        menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.action_new_notification) {
            startActivity(new Intent(StaffHomeActivity.this, NotificationActivity.class));
            txt_notification_badge.setText("");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void loadNotification() {
        Log.d(TAG, "loadNotification: called!!");

        notificationCollection.whereEqualTo("read", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            mINotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StaffHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mITimeSlotLoadListener = this;
        mINotificationCountListener = this;
        initNotificationRealtimeUpdate();
        initBookingRealtimeUpdate();
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open,
                R.string.close);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_exit) {
                    logOut();
                }
                return true;
            }
        });

        View headerView = mNavigationView.getHeaderView(0);
        txt_barber_name = headerView.findViewById(R.id.txt_barber_name);
        txt_barber_name.setText(Common.currentBarber.getName());

        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);

        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recycler_time_slot.setLayoutManager(layoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .configure()
                .end()
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date; // This cod will not load again if you select new day same with day selected
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                            Common.simpleDateFormat.format(date.getTime())) ;
                }
            }
        });
    }

    private void loadAvailableTimeSlotOfBarber(String barberId, String bookDate) {
        Log.d(TAG, "loadAvailableTimeSlotOfBarber: called!!");

        mDialog.show();

        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        // Get information of booking
                        // If not created, return empty;
                        ///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("gender")
                                .document(Common.state_name)
                                .collection("Branch")
                                .document(Common.selected_salon.getSalonId())
                                .collection("Hostel")
                                .document(barberId)
                                .collection(bookDate);

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty()) {
                                        mITimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    } else {
                                        List<BookingInfomation> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            timeSlots.add(document.toObject(BookingInfomation.class));
                                        }
                                        mITimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mITimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                            }
                        });
                    }
                }
            }
        });
    }

    private void initBookingRealtimeUpdate() {
        Log.d(TAG, "initBookingRealtimeUpdate: called!!");
///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel
        barberDoc = FirebaseFirestore.getInstance()
                .collection("gender")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Hostel")
                .document(Common.currentBarber.getBarberId());

        // Get current date
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE,0);

        bookingEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                // If have any new booking, update adapter
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                        Common.simpleDateFormat.format(date.getTime()));
            }
        };

        currentBookDateCollection = barberDoc.collection(Common.simpleDateFormat.format(date.getTime()));

        bookingRealtimeListener = currentBookDateCollection.addSnapshotListener(bookingEvent);
    }

    private void initNotificationRealtimeUpdate() {
        Log.d(TAG, "initNotificationRealtimeUpdate: called!!");
///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel

        notificationCollection = FirebaseFirestore.getInstance()
                .collection("gender")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Hostel")
                .document(Common.currentBarber.getBarberId())
                .collection("Notifications");

        notificationEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots.size() > 0) {
                    loadNotification();
                }
            }
        };

        // Only listen and count all notification unread
        notificationListener = notificationCollection.whereEqualTo("read", false)
                .addSnapshotListener(notificationEvent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(StaffHomeActivity.this, "Fake function exit",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void logOut() {
        Log.d(TAG, "logOut: called!!");

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Just delete all remember key and start MainActivity
                        Paper.init(StaffHomeActivity.this);
                        Paper.book().delete(Common.STATE_KEY);
                        Paper.book().delete(Common.BARBER_KEY);
                        Paper.book().delete(Common.SALON_KEY);
                        Paper.book().delete(Common.LOGGED_KEY);

                        Intent mainIntent = new Intent(StaffHomeActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        finish();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<BookingInfomation> timeSlotList) {
        Log.d(TAG, "onTimeSlotLoadSuccess: called!!");
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlotList);
        recycler_time_slot.setAdapter(adapter);
        mDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Log.d(TAG, "onTimeSlotLoadFailed: called!!");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        Log.d(TAG, "onTimeSlotLoadEmpty: called!!");
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);
        mDialog.dismiss();
    }

    @Override
    public void onNotificationCountSuccess(int count) {
        Log.d(TAG, "onNotificationCountSuccess: called!!");
        if (count == 0) {
            txt_notification_badge.setVisibility(View.INVISIBLE);
        }
        else {
            txt_notification_badge.setVisibility(View.VISIBLE);
            if (count <= 9) {
                txt_notification_badge.setText(String.valueOf(count));
            }
            else {
                txt_notification_badge.setText("9+");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealtimeUpdate();
        initNotificationRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        if (notificationListener != null) {
            notificationListener.remove();
        }
        if (bookingRealtimeListener != null) {
            bookingRealtimeListener.remove();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (notificationListener != null) {
            notificationListener.remove();
        }
        if (bookingRealtimeListener != null) {
            bookingRealtimeListener.remove();
        }
        super.onDestroy();
    }
}
