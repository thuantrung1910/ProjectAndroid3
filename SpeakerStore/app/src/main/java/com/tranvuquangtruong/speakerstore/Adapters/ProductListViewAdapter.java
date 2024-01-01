package com.tranvuquangtruong.speakerstore.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tranvuquangtruong.speakerstore.DBHelper;
import com.tranvuquangtruong.speakerstore.Models.ProductModel;
import com.tranvuquangtruong.speakerstore.R;

import java.text.DecimalFormat;
import java.util.List;

public class ProductListViewAdapter extends BaseAdapter {
    private Context context;
    private List<ProductModel> productList;
    private OnDeleteClickListener onDeleteClickListener;
    private OnEditClickListener onEditClickListener;
    private DBHelper dbHelper;
    public ProductListViewAdapter(Context mContext, List<ProductModel> productList) {
        this.context = mContext;
        this.productList = productList;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false);
        }

        ImageButton btnDelete = view.findViewById(R.id.btnDeleteProduct);
        ImageButton btnEdit = view.findViewById(R.id.btnEditProduct);
        TextView productNameTextView = view.findViewById(R.id.productNameTextViewDelete);
        TextView productPrice = view.findViewById(R.id.productPriceTextViewDelete);
        TextView productQuantity = view.findViewById(R.id.productQuantityTextViewDelete);
        ImageView productImageView = view.findViewById(R.id.productImageViewDelete);

        ProductModel product = productList.get(position);
        productNameTextView.setText(product.getName());
        productPrice.setText(""+decimalFormat.format(product.getPrice()));
        productQuantity.setText(product.getQuantity());

        Bitmap bitmap = BitmapFactory.decodeByteArray(product.getImage(), 0, product.getImage().length);
        productImageView.setImageBitmap(bitmap);

        // Thiết lập sự kiện khi người dùng nhấn vào nút Xóa
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(position));
        btnEdit.setOnClickListener(v ->{
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position, v);
            }
        } );

        return view;
    }
    // Interface để bắt sự kiện khi người dùng nhấn vào nút Xóa
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
    // Interface để bắt sự kiện khi người dùng nhấn vào nút Edit
    public interface OnEditClickListener {
        void onEditClick(int position, View view);
    }
    // Khai báo phương thức setOnDeleteClickListener
    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }
    // Phương thức để đăng ký người nghe sự kiện
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }
    // Phương thức để hiển thị Dialog xác nhận xóa
    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc muốn xóa sản phẩm này?");

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Gọi phương thức xóa sản phẩm từ cơ sở dữ liệu
            dbHelper = new DBHelper(context);
            boolean isDeleted = dbHelper.deleteProduct(productList.get(position).getId());

            if (isDeleted) {
                // Xóa sản phẩm thành công, cập nhật danh sách và thông báo
                productList.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
            } else {
                // Xóa sản phẩm không thành công, thông báo lỗi
                Toast.makeText(context, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    public void clearData() {
        productList.clear();
        notifyDataSetChanged(); // Thông báo cho adapter rằng dữ liệu đã thay đổi
    }
}
