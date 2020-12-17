package com.cafeom.fragments;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cafeom.R;
import com.cafeom.adapters.SearchGoodsAdapter;
import com.cafeom.data.GoodsData;
import com.cafeom.databinding.FragmentFrGoodsOrderBinding;

import java.util.ArrayList;
import java.util.List;

public class FrSearchGoods extends FragmentGoodsOrder {

    private FragmentFrGoodsOrderBinding bind;
    private List<GoodsData> mGoods = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = FragmentFrGoodsOrderBinding.inflate(getLayoutInflater(), container, false);
        bind.search.setOnClickListener(this);
        bind.clearText.setOnClickListener(this);
        bind.orderFragment.setOnClickListener(this);
        bind.close.setOnClickListener(this);
        bind.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        bind.rv.setAdapter(new SearchGoodsAdapter(mGoods, mOrder));
        return bind.getRoot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                newSearch();
                break;
            case R.id.clearText:
                bind.searchString.setText("");
                break;
            case R.id.orderFragment:
                mOrder.orderFragment();
                break;
            case R.id.close:
                mOrder.reset();
                break;
        }
    }

    private void newSearch() {
        String s = bind.searchString.getText().toString();
        bind.searchString.setText("");
        mGoods.clear();
        for (GoodsData g: GoodsData.mInstance.data) {
            if (g.f_goods.toLowerCase().contains(s.toLowerCase())) {
                mGoods.add(g);
            }
        }
        bind.rv.getAdapter().notifyDataSetChanged();
    }
}