package ydkim2110.com.androidbarberstaffapp.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ydkim2110.com.androidbarberstaffapp.Adapter.MyConfirmShoppingItemAdapter;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Model.BarberServices;
import ydkim2110.com.androidbarberstaffapp.Model.CartItem;
import ydkim2110.com.androidbarberstaffapp.Model.EventBus.DismissFromBottomSheetEvent;
import ydkim2110.com.androidbarberstaffapp.Model.FCMResponse;
import ydkim2110.com.androidbarberstaffapp.Model.FCMSendData;
import ydkim2110.com.androidbarberstaffapp.Model.Invoice;
import ydkim2110.com.androidbarberstaffapp.Model.MyToken;
import ydkim2110.com.androidbarberstaffapp.R;
import ydkim2110.com.androidbarberstaffapp.Retrofit.IFCMService;
import ydkim2110.com.androidbarberstaffapp.Retrofit.RetrofitClient;

public class TotalPriceFragment extends BottomSheetDialogFragment {

    private static final String TAG = TotalPriceFragment.class.getSimpleName();

    private Unbinder mUnbinder;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;
    @BindView(R.id.recycler_view_shopping)
    RecyclerView recycler_view_shopping;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_barber_name)
    TextView txt_barber_name;
    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;
    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.btn_confirm)
    TextView btn_confirm;

    private HashSet<BarberServices> mServicesAdded;
    //private List<ShoppingItem> mShoppingItemList;
    private IFCMService mIFCMService;
    private AlertDialog mDialog;
    private String image_url;

    private static TotalPriceFragment instance;

    public static TotalPriceFragment getInstance() {
        return instance == null ? new TotalPriceFragment() : instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        mIFCMService = RetrofitClient.getInstance().create(IFCMService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_total_price, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        init();
        initView();

        getBundle(getArguments());

        setInformation();

        return view;
    }

    private void setInformation() {
        Log.d(TAG, "setInformation: called!!");
        txt_salon_name.setText(Common.selected_salon.getName());
        txt_barber_name.setText(Common.currentBarber.getName());
        txt_time.setText(Common.convertTimeSlotToString(Common.currentBookingInformation.getSlot().intValue()));
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

        if (mServicesAdded.size() > 0) {
            // Add to Chip Group
            int i = 0;
            for (BarberServices services : mServicesAdded) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, null);
                chip.setText(services.getName());
                chip.setTag(i);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mServicesAdded.remove(v.getTag());
                        chip_group_services.removeView(v);

                        calculatePrice();
                    }
                });

                chip_group_services.addView(chip);

                i++;
            }
        }

        if (Common.currentBookingInformation.getCartItemList() != null) {
            if (Common.currentBookingInformation.getCartItemList().size() > 0) {
                MyConfirmShoppingItemAdapter adapter = new MyConfirmShoppingItemAdapter(getContext(),
                        Common.currentBookingInformation.getCartItemList());
                recycler_view_shopping.setAdapter(adapter);
            }

            calculatePrice();
        }

    }

    private double calculatePrice() {
        Log.d(TAG, "calculatePrice: called!!");
        double price = Common.DEFAULT_PRICE;
        for (BarberServices services : mServicesAdded) {
            price += services.getPrice();
        }

        if (Common.currentBookingInformation.getCartItemList() != null) {

            for (CartItem cartItem : Common.currentBookingInformation.getCartItemList()) {
                price += (cartItem.getProductPrice()*cartItem.getProductQuantity());
            }
        }

        txt_total_price.setText(new StringBuilder(Common.MONEY_SIGN).append(price));

        return price;
    }

    private void getBundle(Bundle arguments) {
        Log.d(TAG, "getBundle: called!!");
        this.mServicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>() {
                        }.getType());

//        this.mShoppingItemList = new Gson()
//                .fromJson(arguments.getString(Common.SHOPPING_LIST),
//                        new TypeToken<List<ShoppingItem>>() {
//                        }.getType());

        image_url = arguments.getString(Common.IMAGE_DOWNLOADABLE_URL);
    }

    private void initView() {
        Log.d(TAG, "initView: called!");
        recycler_view_shopping.setHasFixedSize(true);
        recycler_view_shopping.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
                ///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel
                // Update bookingInformation, set done = true
                DocumentReference bookingSet = FirebaseFirestore.getInstance()
                        .collection("gender")
                        .document(Common.state_name)
                        .collection("Branch")
                        .document(Common.selected_salon.getSalonId())
                        .collection("Hostel")
                        .document(Common.currentBarber.getBarberId())
                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                        .document(Common.currentBookingInformation.getBookingId());

                bookingSet.get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().exists()) {
                                        // Update
                                        Map<String, Object> dataUpdate = new HashMap<>();
                                        dataUpdate.put("done", true);
                                        bookingSet.update(dataUpdate)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mDialog.dismiss();
                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // if update is done, create invoice
                                                            createInvoice();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mDialog.dismiss();
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void createInvoice() {
        Log.d(TAG, "createInvoice: called!!");
        mDialog.dismiss();
        //Create invoice
        ///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel
        CollectionReference invoiceRef = FirebaseFirestore.getInstance()
                .collection("gender")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Invoices");

        Invoice invoice = new Invoice();
        invoice.setBarberId(Common.currentBarber.getBarberId());
        invoice.setBarberName(Common.currentBarber.getName());

        invoice.setSalonId(Common.selected_salon.getSalonId());
        invoice.setSalonName(Common.selected_salon.getName());
        invoice.setSalonAddress(Common.selected_salon.getAddress());

        invoice.setCustomerName(Common.currentBookingInformation.getCustomerName());
        invoice.setCustomerPhone(Common.currentBookingInformation.getCustomerPhone());

        invoice.setImageUri(image_url);

        invoice.setBarberServices(new ArrayList<BarberServices>(mServicesAdded));
        invoice.setShoppingItemList(Common.currentBookingInformation.getCartItemList());
        invoice.setFinalPrice(calculatePrice());

        invoiceRef.document()
                .set(invoice)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sendNotificationUpdateToUser(Common.currentBookingInformation.getCustomerPhone());
                        }
                    }
                });

    }

    private void sendNotificationUpdateToUser(String customerPhone) {
        Log.d(TAG, "sendNotificationUpdateToUser: called!!");
        // Get Token of user first
        FirebaseFirestore.getInstance()
                .collection("Tokens")
                .whereEqualTo("userPhone", customerPhone)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0) {
                            MyToken myToken = new MyToken();
                            for (DocumentSnapshot tokenSnapshot : task.getResult()) {
                                myToken = tokenSnapshot.toObject(MyToken.class);
                            }

                            // Create notification to send
                            FCMSendData fcmSendData = new FCMSendData();
                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("update_done", "true");

                            /**
                             * we will send an notification with payload data is 'update_true' = true
                             * so, we will add more useful information
                             * state_name, salonId, salonName, barberId
                             * This information will help us query Barber from Client app
                             */

                            // Information need for Rating
                            dataSend.put(Common.RATING_STATE_KEY, Common.state_name);
                            dataSend.put(Common.RATING_SALON_ID, Common.selected_salon.getSalonId());
                            dataSend.put(Common.RATING_SALON_NAME, Common.selected_salon.getName());
                            dataSend.put(Common.RATING_BARBER_ID, Common.currentBarber.getBarberId());

                            fcmSendData.setTo(myToken.getToken());
                            fcmSendData.setData(dataSend);

                            mIFCMService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.newThread())
                                    .subscribe(new Consumer<FCMResponse>() {
                                        @Override
                                        public void accept(FCMResponse fcmResponse) throws Exception {
                                            mDialog.dismiss();
                                            dismiss();

                                            // we just post and event
                                            EventBus.getDefault().postSticky(new DismissFromBottomSheetEvent(true));

                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });


    }

    private void init() {
        Log.d(TAG, "init: called!!");
    }
}
