package com.tranvuquangtruong.speakerstore.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tranvuquangtruong.speakerstore.Models.ProductModel;
import com.tranvuquangtruong.speakerstore.R;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<ProductModel> productList;
    private ProductAdapter productAdapter;
    private OnDetailClickListener onDetailClickListener;
    public ProductAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
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
    public interface OnDetailClickListener{
        void onDetailClickListener(int position,View view);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.gridview_item, parent, false);
        }

        ImageView imageView = view.findViewById(R.id.productImageView);
        TextView nameTextView = view.findViewById(R.id.productNameTextView);
        TextView priceTextView = view.findViewById(R.id.productPriceTextView);
        TextView quantityTextView = view.findViewById(R.id.productQuantityTextView);
        productAdapter = new ProductAdapter(context,productList);

        ProductModel product = productList.get(position);
        Bitmap bitmap = BitmapFactory.decodeByteArray(product.getImage(), 0, product.getImage().length);
        imageView.setImageBitmap(bitmap);
        nameTextView.setText(product.getName());
        priceTextView.setText(decimalFormat.format(product.getPrice()) + " vnđ");
        quantityTextView.setText("" + product.getQuantity());

        return view;
    }
    public void setOnDetailClickListener(ProductAdapter.OnDetailClickListener listener) {
        this.onDetailClickListener = listener;
    }
    public void clearData() {
        productList.clear();
        notifyDataSetChanged(); // Thông báo cho adapter rằng dữ liệu đã thay đổi
    }
}
