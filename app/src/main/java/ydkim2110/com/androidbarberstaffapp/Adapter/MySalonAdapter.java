package ydkim2110.com.androidbarberstaffapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import dmax.dialog.SpotsDialog;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Common.CustomLoginDialog;
import ydkim2110.com.androidbarberstaffapp.Interface.IDialogClickListener;
import ydkim2110.com.androidbarberstaffapp.Interface.IGetBarberListener;
import ydkim2110.com.androidbarberstaffapp.Interface.IRecyclerItemSelectedListener;
import ydkim2110.com.androidbarberstaffapp.Interface.IUserLoginRememberListener;
import ydkim2110.com.androidbarberstaffapp.Model.Barber;
import ydkim2110.com.androidbarberstaffapp.Model.Salon;
import ydkim2110.com.androidbarberstaffapp.R;
import ydkim2110.com.androidbarberstaffapp.StaffHomeActivity;


public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> implements IDialogClickListener {

    private static final String TAG = MySalonAdapter.class.getSimpleName();

    private Context mContext;
    private List<Salon> salonList;
    private List<CardView> cardViewList;

    // This interface will tell us when user logged success, and we will write this user to memory by paper, in MainActivity
    // We just check variable by KEY, if KEY available, that mean next time user no need to login again
    IUserLoginRememberListener mIUserLoginRememberListener;
    // This interface will return us an User object(Barber) from Firebase Store
    // We will user Gson to serialize this object to String and save it
    // Because we need Barber ID to get all time slot
    IGetBarberListener mIGetBarberListener;

    int lastPosition = -1;

    public MySalonAdapter(Context context, List<Salon> salonList, IUserLoginRememberListener iUserLoginRememberListener, IGetBarberListener iGetBarberListener) {
        this.mContext = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
        this.mIUserLoginRememberListener = iUserLoginRememberListener;
        this.mIGetBarberListener = iGetBarberListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_salon, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_salon_name.setText(salonList.get(position).getName());
        holder.txt_salon_address.setText(salonList.get(position).getAddress());

        setAnimation(holder.itemView, position);

        if(!cardViewList.contains(holder.card_salon)) {
            cardViewList.add(holder.card_salon);
        }

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {

                Common.selected_salon = salonList.get(position);
                showLoginDialog();
            }
        });
    }

    private void showLoginDialog() {
        Log.d(TAG, "showLoginDialog: called!!");

        CustomLoginDialog.getInstance()
                .showLoginDialog("STAFF LOGIN","LOGIN", "CANCEL",
                        mContext,this);
    }

    private void setAnimation(View itemView, int position) {
        Log.d(TAG, "setAnimation: called!!");

        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    android.R.anim.slide_in_left);
            itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    @Override
    public void onClickPositiveButton(DialogInterface dialogInterface, String userName, String password) {
        Log.d(TAG, "onClickPositiveButton: called!!");

        // Show Loading dialog
        AlertDialog loading = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(mContext)
                .build();

        loading.show();
        ///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel
        FirebaseFirestore.getInstance()
                .collection("gender")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Hostel")
                .whereEqualTo("username", userName)
                .whereEqualTo("password", password)
                .limit(1)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                dialogInterface.dismiss();

                                loading.dismiss();

                                mIUserLoginRememberListener.onUserLoginSuccess(userName);

                                // Create Barber
                                Barber barber = new Barber();
                                for (DocumentSnapshot barberSnapshot : task.getResult()) {
                                    barber = barberSnapshot.toObject(Barber.class);
                                    barber.setBarberId(barberSnapshot.getId());
                                }

                                mIGetBarberListener.onGetBarberSuccess(barber);

                                // We will navigate Staff Home and clear all previous activity
                                Intent staffHome = new Intent(mContext, StaffHomeActivity.class);
                                staffHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                staffHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(staffHome);
                            }
                            else {
                                loading.dismiss();
                                Toast.makeText(mContext, "Wrong username / password or wrong salon",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onClickNegativeButton(DialogInterface dialogInterface) {
        Log.d(TAG, "onClickNegativeButton: called!!");

        dialogInterface.dismiss();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txt_salon_name;
        private TextView txt_salon_address;
        private CardView card_salon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_salon = itemView.findViewById(R.id.card_salon);
            txt_salon_name = itemView.findViewById(R.id.txt_salon_name);
            txt_salon_address = itemView.findViewById(R.id.txt_salon_address);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}
