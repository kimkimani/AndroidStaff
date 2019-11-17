package ydkim2110.com.androidbarberstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ydkim2110.com.androidbarberstaffapp.Adapter.MySalonAdapter;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Common.SpacesItemDecoration;
import ydkim2110.com.androidbarberstaffapp.Interface.IBranchLoadListener;
import ydkim2110.com.androidbarberstaffapp.Interface.IGetBarberListener;
import ydkim2110.com.androidbarberstaffapp.Interface.IOnLoadCountSalon;
import ydkim2110.com.androidbarberstaffapp.Interface.IUserLoginRememberListener;
import ydkim2110.com.androidbarberstaffapp.Model.Barber;
import ydkim2110.com.androidbarberstaffapp.Model.Salon;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SalonListActivity extends AppCompatActivity implements IOnLoadCountSalon, IBranchLoadListener, IGetBarberListener, IUserLoginRememberListener {

    private static final String TAG = SalonListActivity.class.getSimpleName();

    @BindView(R.id.txt_salon_count)
    TextView txt_salon_count;
    @BindView(R.id.recycler_salon)
    RecyclerView recycler_salon;
    @BindView(R.id.no_item)
    TextView no_item;

    IOnLoadCountSalon mIOnLoadCountSalon;
    IBranchLoadListener mIBranchLoadListener;

    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_list);
        Log.d(TAG, "onCreate: called!!");

        ButterKnife.bind(this);

        initView();

        init();

        loadSalonBaseOnCity(Common.state_name);
    }


    private void initView() {
        Log.d(TAG, "initView: called!!");
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_salon.addItemDecoration(new SpacesItemDecoration(8));
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        mIOnLoadCountSalon = this;
        mIBranchLoadListener = this;
    }

    private void loadSalonBaseOnCity(String name) {
        Log.d(TAG, "loadSalonBaseOnCity: called!!");

        mDialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(name)
                .collection("Branch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Salon> salons = new ArrayList<>();
                            mIOnLoadCountSalon.onLoadCountSalonSuccess(task.getResult().size());
                            for (DocumentSnapshot salonSnapshot : task.getResult()) {
                                Salon salon = salonSnapshot.toObject(Salon.class);
                                salon.setSalonId(salonSnapshot.getId());
                                salons.add(salon);
                            }
                            mIBranchLoadListener.onBranchLoadSuccess(salons);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIBranchLoadListener.onBranchLoadFailed(e.getMessage());
                    }
                });
    }

    @Override
    public void onLoadCountSalonSuccess(int count) {
        Log.d(TAG, "onLoadCountSalonSuccess: called!!");
        txt_salon_count.setText(new StringBuilder("All Salon (").append(count).append(")"));
    }

    @Override
    public void onBranchLoadSuccess(List<Salon> branchList) {
        Log.d(TAG, "onBranchLoadSuccess: called!!");
        if (branchList.size() == 0) {
            no_item.setVisibility(View.VISIBLE);
            recycler_salon.setVisibility(View.GONE);
        } else {
            no_item.setVisibility(View.GONE);
            recycler_salon.setVisibility(View.VISIBLE);
            MySalonAdapter mSalonAdapter = new MySalonAdapter(this, branchList, this, this);
            recycler_salon.setAdapter(mSalonAdapter);
        }

        mDialog.dismiss();
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Log.d(TAG, "onBranchLoadFailed: called!!");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void onGetBarberSuccess(Barber barber) {
        Log.d(TAG, "onGetBarberSuccess: called!!");
        Common.currentBarber = barber;
        Paper.book().write(Common.BARBER_KEY, new Gson().toJson(barber));
    }

    @Override
    public void onUserLoginSuccess(String user) {
        Log.d(TAG, "onUserLoginSuccess: called!!");
        // Save User
        Paper.init(this);
        Paper.book().write(Common.LOGGED_KEY, user);
        Paper.book().write(Common.STATE_KEY, Common.state_name);
        Paper.book().write(Common.SALON_KEY, new Gson().toJson(Common.selected_salon));
    }
}
