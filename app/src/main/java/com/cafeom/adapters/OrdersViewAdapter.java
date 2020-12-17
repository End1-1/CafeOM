package com.cafeom.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeom.GoodsOrder;
import com.cafeom.R;
import com.cafeom.data.OrderData;
import com.cafeom.databinding.ItemOrdersViewBinding;

public class OrdersViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int mSelectedIndex = -1;
    private GoodsOrder mOrder;

    public OrdersViewAdapter(GoodsOrder o) {
        mOrder = o;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrdersViewBinding b = ItemOrdersViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VH) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return OrderData.mInstance.data.size();
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemOrdersViewBinding bind;

        public VH(ItemOrdersViewBinding b) {
            super(b.getRoot());
            bind = b;
            bind.getRoot().setOnClickListener(this);
            bind.show.setOnClickListener(this);
        }

        public void onBind(int position) {
            OrderData o = OrderData.mInstance.data.get(position);
            if (position == mSelectedIndex) {
                bind.show.setVisibility(View.VISIBLE);
            } else {
                bind.show.setVisibility(View.GONE);
            }
            bind.branch.setText(String.format("%s/%s", o.f_branchname, o.f_storename));
            bind.date.setText(o.f_date);
            bind.time.setText(o.f_time);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.show:
                    OrderData o = OrderData.mInstance.data.get(getAdapterPosition());
                    mOrder.openOrder(o.f_id);
                    break;
                default:
                    notifyItemChanged(mSelectedIndex);
                    mSelectedIndex = getAdapterPosition();
                    notifyItemChanged(mSelectedIndex);
                    break;
            }
        }
    }
}
