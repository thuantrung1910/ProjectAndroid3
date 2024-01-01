package com.tranvuquangtruong.speakerstore.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.tranvuquangtruong.speakerstore.DBHelper;
import com.tranvuquangtruong.speakerstore.Models.ProductModel;
import com.tranvuquangtruong.speakerstore.R;

import java.text.DecimalFormat;

public class ShowProductDetailActivity extends AppCompatActivity {

    ImageView imvDetailImage;
    TextView tvDetailName,tvDetailPrice,tvDetailQuantity, tvDetailBrand;
    private DBHelper dbHelper;
    private int productId;
    private ProductModel selectedProduct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_product_detail);

        // Ánh xạ các thành phần giao diện
        tvDetailName = findViewById(R.id.tvProductDetailName);
        tvDetailPrice = findViewById(R.id.tvProductDetailPrice);
        tvDetailQuantity = findViewById(R.id.tvProductDetailQuantity);
        imvDetailImage = findViewById(R.id.imvProductDetailImage);

        // Khởi tạo DatabaseHelper
        dbHelper = new DBHelper(this);
        // Nhận ID sản phẩm từ Intent
        Intent intent = getIntent();
        if (intent.hasExtra("productId")) {
            productId = intent.getIntExtra("productId", -1);

            // Lấy thông tin sản phẩm từ cơ sở dữ liệu
            selectedProduct = dbHelper.getProductById(productId);

            // Hiển thị thông tin sản phẩm trong EditText và ImageView
            displayProductDetails(selectedProduct);

        }
        // Set up the Toolbar (ActionBar) with a back button
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void displayProductDetails(ProductModel product) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        // Hiển thị thông tin sản phẩm trong EditText và ImageView
        tvDetailName.setText(product.getName());
        tvDetailPrice.setText(decimalFormat.format(product.getPrice())+ " vnđ");
        tvDetailQuantity.setText(String.valueOf(product.getQuantity()));

        // Hiển thị hình ảnh sản phẩm (sử dụng hàm setImageViewWithBlob)
        setImageViewWithBlob(product.getImage());
    }
    private void setImageViewWithBlob(byte[] imageBytes) {
        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imvDetailImage.setImageBitmap(bitmap);
        }
    }

    // Handle the back button in the ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button click
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}