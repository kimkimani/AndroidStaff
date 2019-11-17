package ydkim2110.com.androidbarberstaffapp.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import ydkim2110.com.androidbarberstaffapp.Adapter.MyShoppingItemAdapter;
import ydkim2110.com.androidbarberstaffapp.Common.SpacesItemDecoration;
import ydkim2110.com.androidbarberstaffapp.Interface.IOnShoppingItemSelected;
import ydkim2110.com.androidbarberstaffapp.Interface.IShoppingDataLoadListener;
import ydkim2110.com.androidbarberstaffapp.Model.ShoppingItem;
import ydkim2110.com.androidbarberstaffapp.R;

public class ShoppingFragment extends BottomSheetDialogFragment implements IShoppingDataLoadListener, IOnShoppingItemSelected {

    private static final String TAG = ShoppingFragment.class.getSimpleName();

    private Unbinder mUnbinder;

    private IOnShoppingItemSelected callBackToActivity;
    private IShoppingDataLoadListener mIShoppingDataLoadListener;

    private AlertDialog mDialog;
    private CollectionReference shoppingItemRef;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;

    @BindView(R.id.chip_wax)
    Chip chip_wax;
    @OnClick(R.id.chip_wax)
    void waxLoadClick() {
        setSelectedChip(chip_wax);
        loadShoppingItem("Beddings");
    }

    @BindView(R.id.chip_spray)
    Chip chip_spray;
    @OnClick(R.id.chip_spray)
    void sprayLoadClick() {
        setSelectedChip(chip_spray);
        loadShoppingItem("Kitchen");
    }

    @BindView(R.id.chip_hair_care)
    Chip chip_hair_care;
    @OnClick(R.id.chip_hair_care)
    void haireCareLoadClick() {
        setSelectedChip(chip_hair_care);
        loadShoppingItem("curtain ");
    }

    @BindView(R.id.chip_body_care)
    Chip chip_body_care;
    @OnClick(R.id.chip_body_care)
    void bodyCareLoadClick() {
        setSelectedChip(chip_body_care);
        loadShoppingItem("personal");
    }
    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;

    private static ShoppingFragment instance;

    public static ShoppingFragment getInstance(IOnShoppingItemSelected iOnShoppingItemSelected) {
        return instance == null ? new ShoppingFragment(iOnShoppingItemSelected) : instance;
    }

    private void loadShoppingItem(String itemMenu) {
        Log.d(TAG, "loadShoppingItem: called!!");

        mDialog.show();

        shoppingItemRef = FirebaseFirestore.getInstance()
                .collection("Shooping")
                .document(itemMenu)
                .collection("Items");

        // Get data
        shoppingItemRef.get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        mIShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ShoppingItem> shoppingItems = new ArrayList<>();
                            for (DocumentSnapshot itemSnapshot : task.getResult()) {
                                ShoppingItem shoppingItem = itemSnapshot.toObject(ShoppingItem.class);
                                // Remember add it if you don't want to get null!!
                                shoppingItem.setId(itemSnapshot.getId());
                                shoppingItems.add(shoppingItem);
                            }
                            mIShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
                            mDialog.dismiss();
                        }
                    }
                });
    }

    private void setSelectedChip(Chip chip) {
        Log.d(TAG, "setSelectedChip: called!!");
        // Set color
        for (int i=0; i<chipGroup.getChildCount(); i++) {
            Chip chipItem = (Chip) chipGroup.getChildAt(i);
            Log.d(TAG, "setSelectedChip: chip.getId(): "+chip.getId());
            Log.d(TAG, "setSelectedChip: chipItem.getId(): "+chipItem.getId());
            // If not selected
            if (chipItem.getId() != chip.getId()) {
                chipItem.setChipBackgroundColorResource(android.R.color.darker_gray);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
            // If selected
            else {
                chipItem.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                chipItem.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    public ShoppingFragment(IOnShoppingItemSelected callBackToActivity) {
        this.callBackToActivity = callBackToActivity;
    }

    public ShoppingFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_shopping, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        // Default load
        loadShoppingItem("Beddings");

        init();

        initView();

        return view;
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mIShoppingDataLoadListener = this;
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler_items.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        Log.d(TAG, "onShoppingDataLoadSuccess: called!!");
        MyShoppingItemAdapter adapter = new MyShoppingItemAdapter(getContext(), shoppingItemList, this);
        recycler_items.setAdapter(adapter);
    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Log.d(TAG, "onShoppingDataLoadFailed: called!!");
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        Log.d(TAG, "onShoppingItemSelected: called!!");
        callBackToActivity.onShoppingItemSelected(shoppingItem);
    }
}
