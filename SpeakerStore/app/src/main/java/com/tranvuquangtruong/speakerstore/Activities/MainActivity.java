package com.tranvuquangtruong.speakerstore.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.tranvuquangtruong.speakerstore.DBHelper;
import com.tranvuquangtruong.speakerstore.Adapters.ProductAdapter;
import com.tranvuquangtruong.speakerstore.Models.ProductModel;
import com.tranvuquangtruong.speakerstore.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private DBHelper dbHelper;
    private Button btnAdd;
    private List<ProductModel> productList;
    private ProductAdapter productAdapter;
    private ActivityResultLauncher<Intent> addProductLauncher;
    private  ActivityResultLauncher<Intent> deleteProductLauncher;
    private static boolean isNewActivityOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.gridView);
        dbHelper = new DBHelper(this);
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        gridView.setAdapter(productAdapter);
        btnAdd = findViewById(R.id.btnAddMain);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddProductActivity.class);
                startActivity(intent);
            }
        });
        Button btnEdit = findViewById(R.id.buttonEditMain);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeleteProductActivity.class);
                deleteProductLauncher.launch(intent);
            }
        });
        Button btnReload = findViewById(R.id.buttonReload);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshGridViewEmpty();
                loadProductsFromDatabase();
            }
        });
        // kiểm tra database trống
        if (dbHelper.isDatabaseEmpty())
        {
            // Thêm dữ liệu săn vào database
            addProductToDatabase();
        }

        // Load dữ liệu từ SQLite và cập nhật GridView
        loadProductsFromDatabase();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the result if needed
                    }
                }
        );
        deleteProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the result if needed
                    }
                }
        );
        gridView.setAdapter(productAdapter);
        gridView.setOnItemClickListener(((parent, view, position, id) -> {
            ProductModel selectProductModel = productList.get(position);
            openDetailProductActivity(selectProductModel.getId());
        }));

    }



    private void loadProductsFromDatabase( ) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DBHelper.COLUMN_ID, DBHelper.COLUMN_NAME, DBHelper.COLUMN_PRICE, DBHelper.COLUMN_QUANTITY, DBHelper.COLUMN_IMAGE};
        Cursor cursor = db.query(DBHelper.TABLE_PRODUCTS, columns, null, null, null, null, null);

        if (cursor != null) {
            int columnIndexId = cursor.getColumnIndex(DBHelper.COLUMN_ID);
            int columnIndexName = cursor.getColumnIndex(DBHelper.COLUMN_NAME);
            int columnIndexPrice = cursor.getColumnIndex(DBHelper.COLUMN_PRICE);
            int columnIndexQuantity = cursor.getColumnIndex(DBHelper.COLUMN_QUANTITY);
            int columnIndexImage = cursor.getColumnIndex(DBHelper.COLUMN_IMAGE);

            if (cursor.moveToFirst()) {
                do {
                    // Check if columnIndex is valid (not -1) before using it
                    if (columnIndexId != -1 && columnIndexName != -1 && columnIndexPrice != -1
                            && columnIndexQuantity != -1 && columnIndexImage != -1 ) {

                        int id = cursor.getInt(columnIndexId);
                        String name = cursor.getString(columnIndexName);
                        double price = cursor.getDouble(columnIndexPrice);
                        int quantity = cursor.getInt(columnIndexQuantity);
                        byte[] image = cursor.getBlob(columnIndexImage);

                        ProductModel product = new ProductModel(id,name, price, quantity, image);
                        productList.add(product);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

            // Cập nhật Adapter khi có dữ liệu mới
            productAdapter.notifyDataSetChanged();
        }
    }

    private void addProductToDatabase() {
        byte[] imageData1 = convertDrawableToByteArray(this,R.drawable.daikin);
        byte[] imageData2 = convertDrawableToByteArray(this,R.drawable.funiki);
        byte[] imageData3 = convertDrawableToByteArray(this,R.drawable.casper);
        dbHelper.insertData(1,"Loa Bluetooth JBL Partybox 710",2000000,
                5, imageData1);
        dbHelper.insertData(2,"Loa Bluetooth Sony SRS-XP500 ",5000000,
                5, imageData2);
        dbHelper.insertData(3,"Loa Tháp Samsung MX-T40/XV ",6500000,
                5, imageData3);
    }
    // Chuyển đổi ảnh từ tài nguyên Drawable sang mảng byte[]
    public static byte[] convertDrawableToByteArray(Context context, int drawableId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    private void refreshGridViewEmpty() {
        // Cập nhật adapter với dữ liệu mới (trống)
        productAdapter.clearData();
    }

    private void openDetailProductActivity(int productID) {
        // Mở trang EditProductActivity và chuyển ID sản phẩm cần chỉnh sửa
        Intent intent = new Intent(this, ShowProductDetailActivity.class);
        intent.putExtra("productId", productID);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database when the activity is destroyed
        dbHelper.close();
    }
    protected void onResume() {
        super.onResume();
        // Cập nhật dữ liệu trong adapter
        productAdapter.clearData(); // Cập nhật dữ liệu adapter (hoặc load lại dữ liệu từ database)
        loadProductsFromDatabase();
    }

}