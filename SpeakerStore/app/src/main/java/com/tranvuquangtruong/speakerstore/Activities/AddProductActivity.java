package com.tranvuquangtruong.speakerstore.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import com.tranvuquangtruong.speakerstore.DBHelper;
import com.tranvuquangtruong.speakerstore.R;


public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private byte[] selectedImageBytes;
    private ImageView ivProductImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Khai báo ImageView
        ivProductImage = findViewById(R.id.ivProductImage);

        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to pick an image
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            }
        });

        // Xử lý sự kiện khi người dùng nhấn nút "Add"
        Button btnAddProduct = findViewById(R.id.btnAddProduct);
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy thông tin sản phẩm từ EditText
                EditText etProductName = findViewById(R.id.etProductName);
                EditText etProductPrice = findViewById(R.id.etProductPrice);
                EditText etProductQuantity = findViewById(R.id.etProductQuantity);
                ImageView imvProductImage = findViewById(R.id.ivProductImage);

                if (etProductName.getText().toString().isEmpty()
                        || etProductPrice.getText().toString().isEmpty() || etProductQuantity.getText().toString().isEmpty()
                        || imvProductImage.getDrawable()==null){
                    Toast.makeText(AddProductActivity.this, "Dữ liệu không được để trống", Toast.LENGTH_SHORT).show();
                }
                else {
                    String productName = etProductName.getText().toString().trim();
                    double productPrice = Double.parseDouble(etProductPrice.getText().toString().trim());
                    int productQuantity = Integer.parseInt(etProductQuantity.getText().toString().trim());
                    // Thêm sản phẩm mới vào Database
                    addProductToDatabase(productName, productPrice, productQuantity, selectedImageBytes);
                }

            }
        });

        // Set up the Toolbar (ActionBar) with a back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng bộ nhớ khi không cần thiết
        selectedImageBytes = null;
    }
    private void addProductToDatabase(String productName, double productPrice, int productQuantity, byte[] productImage ) {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, productName);
        values.put(DBHelper.COLUMN_PRICE, productPrice);
        values.put(DBHelper.COLUMN_QUANTITY, productQuantity);
        values.put(DBHelper.COLUMN_IMAGE, productImage);

        long newRowId = db.insert(DBHelper.TABLE_PRODUCTS, null, values);

        if (newRowId != -1) {
            // Thêm thành công, gửi kết quả trở lại MainActivity (nếu cần)
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            Toast.makeText(this, "Dữ liệu được thêm thành công", Toast.LENGTH_SHORT).show();
            EditText etProductName = findViewById(R.id.etProductName);
            EditText etProductPrice = findViewById(R.id.etProductPrice);
            EditText etProductQuantity = findViewById(R.id.etProductQuantity);
            etProductName.setText("");
            etProductPrice.setText("");
            etProductQuantity.setText("");
            ImageView ivProductImage = findViewById(R.id.ivProductImage);
            ivProductImage.setImageResource(0);
        } else {
            Toast.makeText(this, "Thêm dữ liệu thất bại", Toast.LENGTH_SHORT).show();
        }
        db.close();
        dbHelper.close();
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