package com.cafeom.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeom.GoodsOrder;
import com.cafeom.R;
import com.cafeom.data.GoodsData;
import com.cafeom.databinding.ItemGoodsOrderBinding;

import java.util.List;

public class SearchGoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private GoodsOrder mOrder;
    private List<GoodsData> mData;
    private int mSelectedIndex = -1;

    public SearchGoodsAdapter(List<GoodsData> d, GoodsOrder g) {
        mData = d;
        mOrder = g;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGoodsOrderBinding b = ItemGoodsOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VH) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemGoodsOrderBinding bind;

        public VH(ItemGoodsOrderBinding b) {
            super(b.getRoot());
            bind = b;
            bind.plus.setOnClickListener(this);
            bind.add.setOnClickListener(this);
            bind.cancel.setOnClickListener(this);
            bind.qty.setImeOptions(EditorInfo.IME_ACTION_DONE);
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
            GoodsData g = mData.get(index);
            bind.name.setText(g.f_goods);
            bind.unit.setText(g.f_unit);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.plus:
                    notifyItemChanged(mSelectedIndex);
                    mSelectedIndex = getAdapterPosition();
                    notifyItemChanged(mSelectedIndex);
                    break;
                case R.id.cancel:
                    bind.qty.setText("");
                    bind.qtyLayout.setVisibility(View.GONE);
                    break;
                case R.id.add:
                    try {
                        if (Double.valueOf(bind.qty.getText().toString()) < 0.001) {
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    GoodsData g = mData.get(getAdapterPosition());
                    g.f_qty = bind.qty.getText().toString();
                    g.f_stateid = "0";
                    bind.qty.setText("");
                    bind.qtyLayout.setVisibility(View.GONE);
                    g.f_correct = "";
                    mOrder.mOrderData.mGoodsOrder.add(g);
                    break;
            }
        }
    }
}
