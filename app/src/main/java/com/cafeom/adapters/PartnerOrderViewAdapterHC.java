package com.cafeom.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeom.R;
import com.cafeom.data.GoodsData;
import com.cafeom.databinding.ItemPartnerOrderViewGoodsBinding;
import com.cafeom.databinding.ItemPartnerOrderViewGoodsHcBinding;

import java.util.List;

public class PartnerOrderViewAdapterHC extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GoodsData> mData;

    public PartnerOrderViewAdapterHC(List<GoodsData> d) {
        mData = d;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPartnerOrderViewGoodsHcBinding b = ItemPartnerOrderViewGoodsHcBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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

        private ItemPartnerOrderViewGoodsHcBinding bind;

        public VH(ItemPartnerOrderViewGoodsHcBinding b) {
            super(b.getRoot());
            bind = b;
            bind.store.setOnClickListener(this);
        }

        public void onBind(int position) {
            GoodsData g = mData.get(position);
            bind.name.setText(g.f_goods);
            bind.qty.setText(g.f_qty);
            bind.unit.setText(g.f_unit);
            int id_store = R.drawable.store_i;
            switch (Integer.valueOf(g.f_deliver)) {
                case -1:
                case -2:
                case 0:
                    id_store = R.drawable.store_i;
                    break;
                case 1:
                case 2:
                    id_store = R.drawable.store;
                    break;
            }
            bind.store.setImageDrawable(bind.getRoot().getContext().getDrawable(id_store));
        }

        @Override
        public void onClick(View view) {
            GoodsData g = mData.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.store:
                    g.f_deliver = String.valueOf(Integer.valueOf(g.f_deliver) * -1);
                    if (Integer.valueOf(g.f_deliver) == 0) {
                        g.f_deliver = "-1";
                    }
                    break;
            }
            notifyItemChanged(getAdapterPosition());
        }
    }
}
