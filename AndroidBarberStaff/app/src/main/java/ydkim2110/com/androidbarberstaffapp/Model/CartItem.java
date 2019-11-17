package ydkim2110.com.androidbarberstaffapp.Model;


import androidx.annotation.NonNull;

/**
 * CartItem
 * @author Kim Yong dae
 * @version 1.0.0
 * @since 2019-06-29 오전 9:52
**/
public class CartItem {

    private String productId;

    private String productName;

    private String productImage;

    private Long productPrice;

    private int productQuantity;

    private String userPhone;

    @NonNull
    public String getProductId() {
        return productId;
    }

    public void setProductId(@NonNull String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public Long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Long productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}