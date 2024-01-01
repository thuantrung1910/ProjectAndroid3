package com.tranvuquangtruong.speakerstore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tranvuquangtruong.speakerstore.Models.ProductModel;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "myProduct_db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_IMAGE = "image";


    private static final String CREATE_TABLE_PRODUCTS =
            "CREATE TABLE " + TABLE_PRODUCTS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_PRICE + " REAL," +
                    COLUMN_QUANTITY + " INTEGER," +
                    COLUMN_IMAGE + " BLOB" + ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PRODUCTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    public void insertData(int productId, String productName, double productPrice,
                           int productQuantity, byte[] productImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, productName);
        values.put(COLUMN_PRICE, productPrice);
        values.put(COLUMN_QUANTITY, productQuantity);
        values.put(COLUMN_IMAGE, productImage);

        try {
            db.insertOrThrow(TABLE_PRODUCTS, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + "=?";
        String[] whereArgs = {String.valueOf(productId)};

        int result = db.delete(TABLE_PRODUCTS, whereClause, whereArgs);
        return result > 0;
    }
    // Phương thức cập nhật thông tin sản phẩm
    public boolean updateProduct(int productId, String productName, double productPrice,
                                 int productQuantity, byte[] productImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, productName);
        values.put(COLUMN_PRICE, productPrice);
        values.put(COLUMN_QUANTITY, productQuantity);
        values.put(COLUMN_IMAGE, productImage);
        // Cập nhật dữ liệu trong cơ sở dữ liệu
        int rowsAffected = db.update(TABLE_PRODUCTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(productId)});

        // Đóng kết nối với cơ sở dữ liệu
        db.close();

        // Trả về true nếu có ít nhất một hàng được cập nhật
        return rowsAffected > 0;
    }

    // Phương thức lấy thông tin sản phẩm theo ID
    public ProductModel getProductById(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PRICE, COLUMN_QUANTITY, COLUMN_IMAGE},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(productId)},
                null,
                null,
                null,
                null
        );

        ProductModel product = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Kiểm tra sự tồn tại của cột trước khi truy cập dữ liệu
                int columnIndexId = cursor.getColumnIndex(COLUMN_ID);
                int columnIndexName = cursor.getColumnIndex(COLUMN_NAME);
                int columnIndexPrice = cursor.getColumnIndex(COLUMN_PRICE);
                int columnIndexQuantity = cursor.getColumnIndex(COLUMN_QUANTITY);
                int columnIndexImage = cursor.getColumnIndex(COLUMN_IMAGE);

                if (columnIndexId >= 0 && columnIndexName >= 0 && columnIndexPrice >= 0 &&
                        columnIndexQuantity >= 0 && columnIndexImage >= 0 ) {
                    // Lấy dữ liệu từ Cursor
                    product = new ProductModel(
                            cursor.getInt(columnIndexId),
                            cursor.getString(columnIndexName),
                            cursor.getDouble(columnIndexPrice),
                            cursor.getInt(columnIndexQuantity),
                            cursor.getBlob(columnIndexImage)
                    );
                }

                cursor.close();
            }
        }

        // Đóng kết nối với cơ sở dữ liệu
        db.close();

        return product;
    }

    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);

        boolean isEmpty = cursor.getCount() == 0;

        cursor.close();
        return isEmpty;
    }
}