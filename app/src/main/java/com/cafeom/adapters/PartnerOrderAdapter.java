package com.cafeom.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeom.Cnf;
import com.cafeom.Dialog;
import com.cafeom.R;
import com.cafeom.WebService;
import com.cafeom.data.DataParser;
import com.cafeom.data.GoodsData;
import com.cafeom.data.PartnerOrderData;
import com.cafeom.databinding.ItemPartnerOrderBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

public class PartnerOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int mSelectedIndex = -1;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPartnerOrderBinding b = ItemPartnerOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VH) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return PartnerOrderData.mInstance.data.size();
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemPartnerOrderBinding bind;

        public VH(ItemPartnerOrderBinding b) {
            super(b.getRoot());
            bind = b;
            bind.getRoot().setOnClickListener(this);
            bind.show.setOnClickListener(this);
            bind.upload.setOnClickListener(this);
            bind.rv.setLayoutManager(new LinearLayoutManager(b.getRoot().getContext()));
        }

        public void onBind(int position) {
            String partner = PartnerOrderData.mInstance.mPartners.get(position);
            bind.partner.setText(partner);
            if (position == mSelectedIndex) {
                bind.getRoot().setBackgroundColor(Color.CYAN);
                bind.show.setVisibility(View.VISIBLE);
                bind.upload.setVisibility(View.VISIBLE);
            } else {
                bind.getRoot().setBackgroundColor(Color.WHITE);
                bind.show.setVisibility(View.GONE);
                bind.lrv.setVisibility(View.GONE);
                bind.upload.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            final String partner = PartnerOrderData.mInstance.mPartners.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.show:
                    List<GoodsData> goods = PartnerOrderData.mInstance.data.get(partner);
                    bind.lrv.setVisibility(View.VISIBLE);
                    bind.rv.setAdapter(new PartnerOrderViewAdapter(goods));
                    break;
                case R.id.upload:
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    List<GoodsData> goods = PartnerOrderData.mInstance.data.get(partner);
                                    JsonArray ja = new JsonArray();
                                    String viber = "";
                                    for (GoodsData g: goods) {
                                        if (Integer.valueOf(g.f_deliver) > 0) {
                                            ja.add(DataParser.gson().toJsonTree(g));
                                            if (g.f_deliver.equals("2")) {
                                                viber += g.f_goods + ": " + g.f_qty + "\r\n";
                                            }
                                        }
                                    }
                                    if (ja.size() == 0) {
                                        Dialog.alertDialog(bind.getRoot().getContext(), R.string.Empty, R.string.NothigToSend);
                                    } else {

                                        if (!viber.isEmpty()) {
                                            boolean found = false;
                                            Intent share = new Intent(android.content.Intent.ACTION_SEND);
                                            share.setType("text/plain");

                                            // gets the list of intents that can be loaded.
                                            List<ResolveInfo> resInfo = bind.getRoot().getContext().getPackageManager()
                                                    .queryIntentActivities(share, 0);
                                            if (!resInfo.isEmpty()) {
                                                for (ResolveInfo info : resInfo) {
                                                    if (info.activityInfo.packageName.toLowerCase(
                                                            Locale.getDefault()).contains("com.viber.voip")
                                                            || info.activityInfo.name.toLowerCase(
                                                            Locale.getDefault()).contains("com.viber.voip")) {
                                                        share.putExtra(Intent.EXTRA_TEXT, viber);
                                                        share.setPackage(info.activityInfo.packageName);
                                                        found = true;
                                                        VH.this.bind.getRoot().getContext().startActivity(Intent.createChooser(share, "Select"));
                                                        break;
                                                    }
                                                }
                                                if (!found) {
                                                    Uri marketUri = Uri.parse("market://details?id="
                                                            + "com.viber.voip");
                                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                                                    VH.this.bind.getRoot().getContext().startActivity(marketIntent);
                                                }
                                            }
                                        }

                                        WebService.Request r = new WebService.Request(Cnf.getString("net_server"), 5, WebService.mMethodPOST);
                                        r.mParamers.put("sid", Cnf.getString("sid"));
                                        r.mParamers.put("goods", ja.toString());
                                        r.mParamers.put("view", "uploadtopartner");
                                        r.go();
                                        PartnerOrderData.mInstance.data.clear();
                                        mSelectedIndex = -1;
                                        notifyDataSetChanged();
                                    }
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    Dialog.alertDialog(bind.getRoot().getContext(), R.string.Empty, bind.getRoot().getContext().getString(R.string.SendToPartner), dialogClickListener);
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