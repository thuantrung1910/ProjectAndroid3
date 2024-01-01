package com.tranvuquangtruong.speakerstore.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.tranvuquangtruong.speakerstore.DBHelper;
import com.tranvuquangtruong.speakerstore.Adapters.ProductListViewAdapter;
import com.tranvuquangtruong.speakerstore.Models.ProductModel;
import com.tranvuquangtruong.speakerstore.R;

import java.util.ArrayList;
import java.util.List;

public class DeleteProductActivity extends AppCompatActivity {

    private static final int INTERVAL_MILLIS = 5000; // Thời gian giữa các lần lặp, 5000 milliseconds = 5 seconds
    private GridView gridView;

    private List<ProductModel> productModelList;
    private DBHelper dbHelper;
    private ProductListViewAdapter productAdapter;
    private ActivityResultLauncher<Intent> editProductLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_product);

        gridView = findViewById(R.id.gvDeleteProduct);
        dbHelper = new DBHelper(this);
        productModelList = new ArrayList<>();
        productAdapter = new ProductListViewAdapter(this, productModelList);
        gridView.setAdapter(productAdapter);
        // Load dữ liệu từ SQLite và cập nhật ListView
        loadProductsFromDatabase();

        // Thiết lập sự kiện khi nhấn vào nút Xóa
        productAdapter.setOnDeleteClickListener(position -> {
            // Xử lý xóa sản phẩm từ cơ sở dữ liệu
            dbHelper = new DBHelper(DeleteProductActivity.this);
            boolean isDeleted = dbHelper.deleteProduct(productModelList.get(position).getId());

            if (isDeleted) {
                // Xóa sản phẩm thành công, cập nhật danh sách và thông báo
                productModelList.remove(position);
                productAdapter.notifyDataSetChanged();
                Toast.makeText(DeleteProductActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
            } else {
                // Xóa sản phẩm không thành công, thông báo lỗi
                Toast.makeText(DeleteProductActivity.this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        productAdapter.setOnEditClickListener((position, view) -> {
            // Lấy sản phẩm được chọn
            ProductModel selectedProduct = (ProductModel) productAdapter.getItem(position);
            // Mở trang EditProductActivity để chỉnh sửa thông tin sản phẩm
            openEditProductActivity(selectedProduct.getId());

        });

        editProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the result if needed
                    }
                }
        );

        // Set up the Toolbar (ActionBar) with a back button
        Toolbar toolbar = findViewById(R.id.toolbarDelete);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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


    private void loadProductsFromDatabase( ) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.TABLE_PRODUCTS;
        Cursor cursor = db.rawQuery(query,null);

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
                        productModelList.add(product);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

            // Cập nhật Adapter khi có dữ liệu mới
            productAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database when the activity is destroyed
        dbHelper.close();
    }
    private void openEditProductActivity(int productId) {
        // Mở trang EditProductActivity và chuyển ID sản phẩm cần chỉnh sửa
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("productId", productId);
        startActivity(intent);
    }


}