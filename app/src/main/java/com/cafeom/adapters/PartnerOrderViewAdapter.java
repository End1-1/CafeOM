package com.cafeom.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeom.R;
import com.cafeom.data.GoodsData;
import com.cafeom.data.PartnerOrderData;
import com.cafeom.databinding.ItemPartnerOrderViewGoodsBinding;

import java.util.List;

public class PartnerOrderViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GoodsData> mData;

    public PartnerOrderViewAdapter(List<GoodsData> d) {
        mData = d;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPartnerOrderViewGoodsBinding b = ItemPartnerOrderViewGoodsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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

        private ItemPartnerOrderViewGoodsBinding bind;

        public VH(ItemPartnerOrderViewGoodsBinding b) {
            super(b.getRoot());
            bind = b;
            bind.delivery.setOnClickListener(this);
            bind.store.setOnClickListener(this);
        }

        public void onBind(int position) {
            GoodsData g = mData.get(position);
            bind.name.setText(g.f_goods);
            bind.qty.setText(g.f_qty);
            bind.unit.setText(g.f_unit);
            int id_delivery = R.drawable.delivery_i;
            int id_store = R.drawable.store_i;
            switch (Integer.valueOf(g.f_deliver)) {
                case 0:
                    break;
                case 1:
                    id_delivery = R.drawable.delivery_i;
                    id_store = R.drawable.store;
                    break;
                case 2:
                    id_delivery = R.drawable.delivery;
                    id_store = R.drawable.store_i;
                    break;
            }
            bind.delivery.setImageDrawable(bind.getRoot().getContext().getDrawable(id_delivery));
            bind.store.setImageDrawable(bind.getRoot().getContext().getDrawable(id_store));
        }

        @Override
        public void onClick(View view) {
            GoodsData g = mData.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.delivery:
                    g.f_deliver = g.f_deliver.equals("2") ? "0" : "2";
                    break;
                case R.id.store:
                    g.f_deliver = g.f_deliver.equals("1") ? "0" : "1";
                    break;
            }
            notifyItemChanged(getAdapterPosition());
        }
    }
}
