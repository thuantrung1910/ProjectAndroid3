package com.tranvuquangtruong.speakerstore.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tranvuquangtruong.speakerstore.DBHelper;
import com.tranvuquangtruong.speakerstore.Models.ProductModel;
import com.tranvuquangtruong.speakerstore.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class EditProductActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private byte[] selectedImageBytes;
    private ImageView ivProductImage;
    private EditText etProductName, etProductPrice, etProductQuantity;
    private Button btnAccept;
    private int productId;
    private DBHelper dbHelper;
    private ProductModel selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Ánh xạ các thành phần giao diện
        etProductName = findViewById(R.id.etProductNameEdit);
        etProductPrice = findViewById(R.id.etProductPriceEdit);
        etProductQuantity = findViewById(R.id.etProductQuantityEdit);
        ivProductImage = findViewById(R.id.ivProductImageEdit);
        btnAccept = findViewById(R.id.btnAcceptEdit);

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

            // Thiết lập sự kiện click cho nút Accept
            btnAccept.setOnClickListener(v -> {
                // Thực hiện cập nhật thông tin sản phẩm
                updateProduct();
            });
        }

        Button btnSelectImage = findViewById(R.id.btnSelectImageEdit);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to pick an image
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            }
        });

        LinearLayout menuSortBrand = findViewById(R.id.layoutSortBrandEdit);
        registerForContextMenu(menuSortBrand);
        menuSortBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị menu context khi TextView được nhấn
                openContextMenu(menuSortBrand);
            }
        });

        // Set up the Toolbar (ActionBar) with a back button
        Toolbar toolbar = findViewById(R.id.toolbarEdit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void displayProductDetails(ProductModel product) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        // Hiển thị thông tin sản phẩm trong EditText và ImageView
        etProductName.setText(product.getName());
        etProductPrice.setText(decimalFormat.format(product.getPrice()));
        etProductQuantity.setText(String.valueOf(product.getQuantity()));

        // Hiển thị hình ảnh sản phẩm (sử dụng hàm setImageViewWithBlob)
        setImageViewWithBlob(product.getImage());
    }
    private void setImageViewWithBlob(byte[] imageBytes) {
        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ivProductImage.setImageBitmap(bitmap);
        }
    }
    private byte[] getImageBytesFromImageView(ImageView imageView) {
        // Chuyển đổi hình ảnh từ ImageView thành Bitmap
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        // Chuẩn bị đối tượng ByteArrayOutputStream để lưu trữ dữ liệu byte của hình ảnh
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Chuyển đổi Bitmap thành mảng byte
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    private void updateProduct() {
        String productPriceConverted = etProductPrice.getText().toString().trim();
        // Lấy thông tin mới từ EditText
        String updatedProductName = etProductName.getText().toString().trim();
        double updatedProductPrice = Double.parseDouble(chuyenDoiVanBan(productPriceConverted));
        int updatedProductQuantity = Integer.parseInt(etProductQuantity.getText().toString().trim());
        byte[] updatedProductImage = getImageBytesFromImageView(ivProductImage);

        // Cập nhật thông tin sản phẩm trong cơ sở dữ liệu
        boolean success = dbHelper.updateProduct(
                productId,
                updatedProductName,
                updatedProductPrice,
                updatedProductQuantity,
                updatedProductImage
        );

        if (success) {
            // Hiển thị thông báo hoặc chuyển về trang trước đó khi cập nhật thành công
            Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
            // Gửi kết quả cập nhật về ProductListActivity
            sendUpdateResultToProductListActivity();
            finish(); // Kết thúc Activity và quay về trang trước đó
        } else {
            // Hiển thị thông báo lỗi khi cập nhật không thành công
            Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show();
        }
    }
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    handleImageResult(data.getData());
                }
            }
        }
    });
    private void handleImageResult(Uri selectedImageUri) {
        try {
            // Convert the selected image to a byte array
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(selectedImageUri);
            selectedImageBytes = getBytes(inputStream);
            inputStream.close();

            // Display the selected image on ImageView
            ivProductImage.setImageBitmap(BitmapFactory.decodeByteArray(selectedImageBytes, 0, selectedImageBytes.length));
        } catch (IOException e) {
            e.printStackTrace();
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
    // Xử lý kết quả trả về từ Intent.ACTION_PICK
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Lấy đường dẫn của hình ảnh
            Uri selectedImageUri = data.getData();

            try {
                // Chuyển đổi hình ảnh thành mảng byte
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                selectedImageBytes = getBytes(inputStream);
                inputStream.close();

                // Hiển thị hình ảnh đã chọn trên ImageView
                ivProductImage.setImageBitmap(BitmapFactory.decodeByteArray(selectedImageBytes, 0, selectedImageBytes.length));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    private void sendUpdateResultToProductListActivity() {
        // Gửi thông báo cập nhật thành công về ProductListActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedProductId", selectedProduct.getId());
        setResult(RESULT_OK);
    }
    private String chuyenDoiVanBan(String chuoiCoDauPhay) {
        // Chuyển đổi từ chuỗi có dấu phẩy sang chuỗi không có dấu phẩy
        return chuoiCoDauPhay.replace(",", "");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng bộ nhớ khi không cần thiết
        selectedImageBytes = null;
    }
}