package ydkim2110.com.androidbarberstaffapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.DoneServiceActivity;
import ydkim2110.com.androidbarberstaffapp.Interface.IRecyclerItemSelectedListener;
import ydkim2110.com.androidbarberstaffapp.Model.BookingInfomation;
import ydkim2110.com.androidbarberstaffapp.R;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    private Context mContext;
    private List<BookingInfomation> mTimeSlotList;
    private List<CardView> mCardViewList;
    private LocalBroadcastManager mLocalBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        mContext = context;
        this.mTimeSlotList = new ArrayList<>();
        this.mCardViewList = new ArrayList<>();
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public MyTimeSlotAdapter(Context context, List<BookingInfomation> timeSlotList) {
        mContext = context;
        mTimeSlotList = timeSlotList;
        this.mCardViewList = new ArrayList<>();
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_time_slot, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(position)).toString());
        // If all position is available, just show list
        if (mTimeSlotList.size() == 0) {
            holder.card_time_slot.setCardBackgroundColor(mContext.getResources().getColor(android.R.color.white));
            holder.txt_time_slot_description.setText("Available");
            holder.txt_time_slot_description.setTextColor(mContext.getResources().getColor(android.R.color.black));
            holder.txt_time_slot.setTextColor(mContext.getResources().getColor(android.R.color.black));

            // Add Event nothing
            holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelected(View view, int position) {
                    // Fix crash if we not add this function
                }
            });
        // If have position is full (booked)
        } else {
            for (BookingInfomation slotValue : mTimeSlotList) {
                // Loop all time slot from server and set different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == position) { // IF slot == position

                    if (!slotValue.getDone()) {
                        // we will set tag for all time slot is full
                        // so base on tag, we can set all remain card background without change full time slot
                        holder.card_time_slot.setTag(Common.DISABLE_TAG);
                        holder.card_time_slot.setCardBackgroundColor(mContext.getResources().getColor(android.R.color.darker_gray));

                        holder.txt_time_slot_description.setText("Full");
                        holder.txt_time_slot_description.setTextColor(mContext.getResources().getColor(android.R.color.black));
                        holder.txt_time_slot.setTextColor(mContext.getResources().getColor(android.R.color.black));
                        //holder.card_time_slot.setEnabled(false);
                        holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int position) {
                                // Only add for gray time slot
                                //we will get Booking information and store in Common.currentBookingInformation
                                // After that, start DoneServiceActivity
///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel

                                FirebaseFirestore.getInstance()
                                        .collection("gender")
                                        .document(Common.state_name)
                                        .collection("Branch")
                                        .document(Common.selected_salon.getSalonId())
                                        .collection("Hostel")
                                        .document(Common.currentBarber.getBarberId())
                                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                        .document(slotValue.getSlot().toString())
                                        .get()
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (task.getResult().exists()) {
                                                        Common.currentBookingInformation = task.getResult().toObject(BookingInfomation.class);
                                                        Common.currentBookingInformation.setBookingId(task.getResult().getId());
                                                        mContext.startActivity(new Intent(mContext, DoneServiceActivity.class));
                                                    }
                                                }
                                            }
                                        });

                            }
                        });
                    }
                    else {
                        // If service is done
                        holder.card_time_slot.setTag(Common.DISABLE_TAG);
                        holder.card_time_slot.setCardBackgroundColor(mContext.getResources().getColor(android.R.color.holo_orange_dark));

                        holder.txt_time_slot_description.setText("Done");
                        holder.txt_time_slot_description.setTextColor(mContext.getResources().getColor(android.R.color.white));
                        holder.txt_time_slot.setTextColor(mContext.getResources().getColor(android.R.color.white));

                        holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int position) {
                                // Add here to fix crash
                            }
                        });
                    }
                }
                else {
                    // Fix Crash
                    if (holder.getIRecyclerItemSelectedListener() == null) {
                        // We only add event for view holder which is not implement click
                        // Because if we don't put this if condition
                        // All time slot with slot value higer current time slot will be override event
                        holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int position) {

                            }
                        });
                    }
                }
            }
        }

        // Add all cart to list (20 card because we have 20 time slot)
        if (!mCardViewList.contains(holder.card_time_slot)) {
            mCardViewList.add(holder.card_time_slot);
        }

    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        public void setIRecyclerItemSelectedListener(IRecyclerItemSelectedListener IRecyclerItemSelectedListener) {
            mIRecyclerItemSelectedListener = IRecyclerItemSelectedListener;
        }

        public IRecyclerItemSelectedListener getIRecyclerItemSelectedListener() {
            return mIRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_time_slot = itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = itemView.findViewById(R.id.txt_time_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mIRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}
