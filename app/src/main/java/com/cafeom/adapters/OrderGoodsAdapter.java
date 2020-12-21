package com.cafeom.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeom.GoodsOrder;
import com.cafeom.R;
import com.cafeom.data.GoodsData;
import com.cafeom.databinding.ItemGoodsOrderReviewBinding;

public class OrderGoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private GoodsOrder mOrder;
    private int mSelectedIndex = -1;

    public OrderGoodsAdapter(GoodsOrder g) {
        mOrder = g;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGoodsOrderReviewBinding b = ItemGoodsOrderReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VH) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return mOrder.mOrderData.mGoodsOrder.size();
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemGoodsOrderReviewBinding bind;

        public VH(ItemGoodsOrderReviewBinding b) {
            super(b.getRoot());
            bind = b;
            bind.plus.setOnClickListener(this);
            bind.add.setOnClickListener(this);
            bind.cancel.setOnClickListener(this);
            bind.remove.setOnClickListener(this);
            bind.hc.setOnClickListener(this);
            bind.hc.setVisibility(View.GONE);
        }

        public void onBind(int index) {
            if (index == mSelectedIndex) {
                bind.parent.setBackgroundColor(Color.CYAN);
                bind.qtyLayout.setVisibility(View.VISIBLE);
                bind.qty.requestFocus();
            } else {
                bind.parent.setBackgroundColor(Color.WHITE);
                if (bind.qtyLayout.getVisibility() == View.VISIBLE) {
                    bind.qtyLayout.setVisibility(View.GONE);
                }
            }
            GoodsData g = mOrder.mOrderData.mGoodsOrder.get(index);
            bind.name.setText(g.f_goods);
            bind.qty.setText(g.f_qty);
            bind.qtyCorrection.setText(g.f_correct);
            bind.qtyShow.setText(g.f_qty);
            bind.unit.setText(g.f_unit);
            bind.unit1.setText(g.f_unit);
            bind.unit2.setText(g.f_unit);
            int hc_drawable = R.drawable.btn_border;
            switch (Integer.valueOf(g.f_stateid)) {
                case 1:
                    bind.qty.setEnabled(false);
                    bind.hc.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    bind.hc.setVisibility(View.VISIBLE);
                    hc_drawable = R.drawable.btn_border_colored;
                    break;
            }
            bind.hc.setBackground(bind.getRoot().getContext().getDrawable(hc_drawable));
        }

        @Override
        public void onClick(View view) {
            GoodsData g = mOrder.mOrderData.mGoodsOrder.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.plus:
                    notifyItemChanged(mSelectedIndex);
                    mSelectedIndex = getAdapterPosition();
                    notifyItemChanged(mSelectedIndex);
                    break;
                case R.id.cancel:
                    bind.qtyLayout.setVisibility(View.GONE);
                    break;
                case R.id.add:
                    if (Double.valueOf(bind.qty.getText().toString()) < 0.001) {
                        return;
                    }
                    g.f_qty = bind.qty.getText().toString();
                    g.f_correct = bind.qtyCorrection.getText().toString();
                    bind.qty.setText("");
                    bind.qtyLayout.setVisibility(View.GONE);
                    notifyItemChanged(mSelectedIndex);
                    mSelectedIndex = -1;
                    break;
                case R.id.remove:
                    mOrder.mOrderData.mGoodsOrder.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    break;
                case R.id.hc:
                    switch (Integer.valueOf(g.f_stateid)) {
                        case 1:
                            g.f_stateid = "2";
                            notifyItemChanged(getAdapterPosition());
                            break;
                        case 2:
                            g.f_stateid = "1";
                            notifyItemChanged(getAdapterPosition());
                            break;
                    }
                    break;
            }
        }
    }
}
