package ydkim2110.com.androidbarberstaffapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Fragments.ShoppingFragment;
import ydkim2110.com.androidbarberstaffapp.Fragments.TotalPriceFragment;
import ydkim2110.com.androidbarberstaffapp.Interface.IBarberServicesLoadListener;
import ydkim2110.com.androidbarberstaffapp.Interface.IOnShoppingItemSelected;
import ydkim2110.com.androidbarberstaffapp.Model.BarberServices;
import ydkim2110.com.androidbarberstaffapp.Model.CartItem;
import ydkim2110.com.androidbarberstaffapp.Model.EventBus.DismissFromBottomSheetEvent;
import ydkim2110.com.androidbarberstaffapp.Model.ShoppingItem;

public class DoneServiceActivity extends AppCompatActivity implements IBarberServicesLoadListener, IOnShoppingItemSelected{

    private static final String TAG = DoneServiceActivity.class.getSimpleName();
    private static final int MY_CAMERA_REQUEST_CODE = 1000;

    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;
    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;
    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;
    @BindView(R.id.chip_group_shopping)
    ChipGroup chip_group_shopping;
    @BindView(R.id.edt_services)
    AppCompatAutoCompleteTextView edt_services;
    @BindView(R.id.img_customer_hair)
    ImageView img_customer_hair;
    @BindView(R.id.add_shopping)
    ImageView add_shopping;
    @BindView(R.id.btn_finish)
    Button btn_finish;
    @BindView(R.id.rdi_no_picture)
    RadioButton rdi_no_picture;
    @BindView(R.id.rdi_picture)
    RadioButton rdi_picture;

    private AlertDialog mDialog;

    private IBarberServicesLoadListener mIBarberServicesLoadListener;

    private HashSet<BarberServices> mServicesAdded = new HashSet<>();

    /**
     * delete all static variable in TotalPriceFragment and DoneServiceActivity (shoppingItemList)
     * Start with DoneServiceActivity, we will convert all ShoppingItemList to List<CartItem> form Booking Information
     *
     * @since : 2019-06-29 오전 9:55
     **/
    //private List<ShoppingItem> mShoppingItems = new ArrayList<>();

    private LayoutInflater mInflater;

    private Uri fileUri;

    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_service);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(this);

        init();
        initView();
        setCustomerInformation();
        loadBarberServices();
    }

    private void initView() {
        Log.d(TAG, "initView: called");
        getSupportActionBar().setTitle("Checkout");

        rdi_no_picture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    img_customer_hair.setVisibility(View.VISIBLE);
                    btn_finish.setEnabled(false);
                }
            }
        });

        rdi_no_picture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    img_customer_hair.setVisibility(View.GONE);
                    btn_finish.setEnabled(true);
                }
            }
        });

        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rdi_no_picture.isChecked()) {
                    mDialog.dismiss();

                    TotalPriceFragment fragment = TotalPriceFragment.getInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(mServicesAdded));
                    //bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(mShoppingItems));
                    fragment.setArguments(bundle);
                    fragment.show(getSupportFragmentManager(), "Price");
                } else {
                    uploadPicture(fileUri);
                }
            }
        });

        img_customer_hair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                fileUri = getOutputMediaFileUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);

            }
        });

        add_shopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShoppingFragment shoppingFragment = ShoppingFragment.getInstance(DoneServiceActivity.this);
                shoppingFragment.show(getSupportFragmentManager(), "Shopping");
            }
        });
    }

    private void uploadPicture(Uri fileUri) {
        if (fileUri != null) {
            mDialog.show();
            String fileName = Common.getFileName(getContentResolver(), fileUri);
            String path = new StringBuilder("Customer_Picture/")
                    .append(fileName)
                    .toString();

            mStorageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = mStorageReference.putFile(fileUri);

            // create task
            Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        Toast.makeText(DoneServiceActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                    }
                    return mStorageReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String url = task.getResult().toString()
                                .substring(0, task.getResult().toString().indexOf("&token"));
                        Log.d(TAG, "onComplete: Download Link: " + url);

                        mDialog.dismiss();

                        TotalPriceFragment fragment = TotalPriceFragment.getInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(mServicesAdded));
                        //bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(mShoppingItems));
                        bundle.putString(Common.IMAGE_DOWNLOADABLE_URL, url);
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), "Price");

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(DoneServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(this, "Image is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getOutputMediaFileUri() {
        Log.d(TAG, "getOutputMediaFileUri: called!!");
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        Log.d(TAG, "getOutputMediaFile: called!!");
        File mediaStroageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "BarberStaffApp");
        if (!mediaStroageDir.exists()) {
            if (!mediaStroageDir.mkdir()) {
                return null;
            }
        }

        String time_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStroageDir.getPath() + File.separator + "IMG_" +
                time_stamp + "_" + new Random().nextInt() + ".jpg");

        return mediaFile;
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        mInflater = LayoutInflater.from(this);

        mIBarberServicesLoadListener = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called!!");
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: MY_CAMERA_REQUEST_CODE: called!!");
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: RESULT_OK: called!!");
                Bitmap bitmap = null;
                ExifInterface ei = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                    ei = new ExifInterface(getContentResolver().openInputStream(fileUri));

                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotateBitmap = null;
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotateBitmap = rotateImage(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateBitmap = rotateImage(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateBitmap = rotateImage(bitmap, 270);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotateBitmap = bitmap;
                            break;
                    }

                    img_customer_hair.setImageBitmap(rotateBitmap);
                    btn_finish.setEnabled(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onActivityResult: error: " + e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onActivityResult: error: " + e.getMessage());
                }

            }
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, int i) {
        Log.d(TAG, "rotateImage: called!!");
        Matrix matrix = new Matrix();
        matrix.postRotate(i);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void loadBarberServices() {
        Log.d(TAG, "loadBarberServices: called!!");
        mDialog.show();
        ///gender/gents/Branch/4jydSfTfDi3o26owKCFp/Hostel

        FirebaseFirestore.getInstance()
                .collection("gender")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Services")
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BarberServices> barberServices = new ArrayList<>();
                            for (DocumentSnapshot barberSnapShot : task.getResult()) {
                                BarberServices services = barberSnapShot.toObject(BarberServices.class);
                                barberServices.add(services);
                            }
                            mIBarberServicesLoadListener.onBarberServicesLoadSuccess(barberServices);
                        }
                    }
                });
    }

    private void setCustomerInformation() {
        Log.d(TAG, "setCustomerInformation: called!!");
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());
    }

    @Override
    public void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList) {
        Log.d(TAG, "onBarberServicesLoadSuccess: called!!");
        List<String> nameServices = new ArrayList<>();
        // sort alphabet
        Collections.sort(barberServicesList, new Comparator<BarberServices>() {
            @Override
            public int compare(BarberServices o1, BarberServices o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        // Add all name of services after sort
        for (BarberServices barberServices : barberServicesList) {
            nameServices.add(barberServices.getName());
        }

        // create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, nameServices);
        // will start working from first character
        edt_services.setThreshold(1);
        edt_services.setAdapter(adapter);
        edt_services.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Add to Chip Group
                int index = nameServices.indexOf(edt_services.getText().toString().trim());

                if (!mServicesAdded.contains(barberServicesList.get(index))) {
                    // we don't want to have duplicate service in list so we use HashSet
                    mServicesAdded.add(barberServicesList.get(index));
                    Chip item = (Chip) mInflater.inflate(R.layout.chip_item, null);
                    item.setText(edt_services.getText().toString());
                    item.setTag(index);
                    edt_services.setText("");

                    item.setOnCloseIconClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            chip_group_services.removeView(v);
                            mServicesAdded.remove((int) item.getTag());
                        }
                    });

                    chip_group_services.addView(item);
                } else {
                    edt_services.setText("");
                }
            }
        });

        loadExtraItems();
    }

    @Override
    public void onBarberServicesLoadFailed(String message) {
        Log.d(TAG, "onBarberServicesLoadFailed: called!!");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        // Here we will create an List to hold Shopping Item
        //mShoppingItems.add(shoppingItem);
        //Log.d(TAG, "onShoppingItemSelected: " + mShoppingItems.size());

        // Create new Cart item
        CartItem cartItem = new CartItem();
        cartItem.setProductId(shoppingItem.getId());
        cartItem.setProductImage(shoppingItem.getImage());
        cartItem.setProductName(shoppingItem.getName());
        cartItem.setProductPrice(shoppingItem.getPrice());
        cartItem.setProductQuantity(1);
        cartItem.setUserPhone(Common.currentBookingInformation.getCustomerPhone());

        // If user submit with empty cart
        if (Common.currentBookingInformation.getCartItemList() == null) {
            Common.currentBookingInformation.setCartItemList(new ArrayList<CartItem>());
        }

        // we will use this flag to update cart item quantity increase by 1
        // If already have item with same name in cart
        boolean flag = false;

        for (int i = 0; i < Common.currentBookingInformation.getCartItemList().size(); i++) {
            if (Common.currentBookingInformation.getCartItemList().get(i).getProductName().equals(shoppingItem.getName())) {
                // Enable flag
                flag = true;
                CartItem itemUpdate = Common.currentBookingInformation.getCartItemList().get(i);
                itemUpdate.setProductQuantity(itemUpdate.getProductQuantity() + 1);
                // Update List
                Common.currentBookingInformation.getCartItemList().set(i, itemUpdate);
            }
        }

        // If flag = false -> new item added
        if (!flag) {
            Common.currentBookingInformation.getCartItemList().add(cartItem);

            Chip item = (Chip) mInflater.inflate(R.layout.chip_item, null);
            item.setText(new StringBuilder(cartItem.getProductName())
                    .append(" x")
                    .append(cartItem.getProductQuantity()));
            item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));

            item.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "new add button listener: called!!");
                    chip_group_shopping.removeView(v);
                    Common.currentBookingInformation.getCartItemList().remove((int)item.getTag());
                }
            });

            chip_group_shopping.addView(item);
        }
        // flag = true, item update
        else {
            chip_group_shopping.removeAllViews();
            loadExtraItems();
        }
    }

    private void loadExtraItems() {
        Log.d(TAG, "loadExtraItems: called!!");
        if (Common.currentBookingInformation.getCartItemList() != null) {
            for (CartItem cartItem : Common.currentBookingInformation.getCartItemList()) {
                Chip item = (Chip) mInflater.inflate(R.layout.chip_item, null);
                item.setText(new StringBuilder(cartItem.getProductName())
                        .append(" x")
                        .append(cartItem.getProductQuantity()));
                item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));

                item.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chip_group_shopping.removeView(v);
                        Common.currentBookingInformation.getCartItemList().remove((int)item.getTag());
                    }
                });

                chip_group_shopping.addView(item);
            }
        }

        mDialog.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void dismissDialog(DismissFromBottomSheetEvent event) {
        if (event.isButtonClick()) {
            finish();
        }
    }
}
