package com.appventurez.bmwms.cbwtf.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.appventurez.bmwms.R;
import com.appventurez.bmwms.cbwtf.models.ReportsModel;
import com.appventurez.bmwms.classes.AppStrings;

import java.text.DecimalFormat;
import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder>{

    Context context;
    List<ReportsModel> data;
    Geocoder geocoder;

    public ReportsAdapter(Context context, List<ReportsModel> data) {
        this.context = context;
        this.data = data;
        geocoder = new Geocoder(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reports_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportsModel model = data.get(position);

        holder.hName.setText(model.getName());
        holder.hInitial.setText(model.getName().substring(0,1));
        holder.hCode.setText(model.getHospitalCode());

        double cw = Double.parseDouble(model.getCbwtfWeight().trim());
        double hw = Double.parseDouble(model.getHcfWeight().trim());
        double diff = Math.abs(cw-hw);

        holder.hcf_w.setText(new DecimalFormat("000.000").format(hw));
        holder.cbwtf_w.setText(new DecimalFormat("000.000").format(cw));
        holder.diff_w.setText(new DecimalFormat("000.000").format(diff));

        try{
            String[] hcfAddr = model.getHcfLatLong().split(",");
            List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(hcfAddr[0].trim()),Double.parseDouble(hcfAddr[1].trim()),5);
            holder.hcf_addr.setText(addresses.get(0).getAddressLine(0));

            String[] cbwtfAddr = model.getLatLongCbwtf().split(",");
            List<Address> addresses2 = geocoder.getFromLocation(Double.parseDouble(cbwtfAddr[0].trim()),Double.parseDouble(cbwtfAddr[1].trim()),5);
            holder.cbwtf_addr.setText(addresses2.get(0).getAddressLine(0));
        }catch (Exception e){
            e.printStackTrace();
        }

        holder.date.setText(model.getHandoverDate());

        Glide.with(context).load(R.drawable.red).into(holder.red);
        Glide.with(context).load(R.drawable.blue).into(holder.blue);
        Glide.with(context).load(R.drawable.yellow).into(holder.yellow);
        Glide.with(context).load(R.drawable.gray).into(holder.white);

        holder.red_w.setText(model.getRed());
        holder.blue_w.setText(model.getBlue());
        holder.yellow_w.setText(model.getYellow());
        holder.white_w.setText(model.getWhite());

        Glide.with(context).load(R.drawable.red).into(holder.red_c);
        Glide.with(context).load(R.drawable.blue).into(holder.blue_c);
        Glide.with(context).load(R.drawable.yellow).into(holder.yellow_c);
        Glide.with(context).load(R.drawable.gray).into(holder.white_c);

        holder.red_w_c.setText(model.getRedCbwtf());
        holder.blue_w_c.setText(model.getBlueCbwtf());
        holder.yellow_w_c.setText(model.getYellowCbwtf());
        holder.white_w_c.setText(model.getWhiteCbwtf());

        Glide.with(context).load(R.drawable.recycling).into(holder.total_packets_img);
        holder.total_packets_tv.setText(model.getTotalPackets());

        if (model.getHcfWeightPhoto() != null && !model.getHcfWeightPhoto().trim().isEmpty()) {
            holder.layoutHcfPhoto.setVisibility(View.VISIBLE);
            holder.hcfPhotoView.setOnClickListener(v -> showImageDialog(context, "HCF Weight Photo", model.getHcfWeightPhoto()));
        } else {
            holder.layoutHcfPhoto.setVisibility(View.GONE);
        }

        if (model.getCbwtfWeightPhoto() != null && !model.getCbwtfWeightPhoto().trim().isEmpty()) {
            holder.layoutCbwtfPhoto.setVisibility(View.VISIBLE);
            holder.cbwtfPhotoView.setOnClickListener(v -> showImageDialog(context, "CBWTF Weight Photo", model.getCbwtfWeightPhoto()));
        } else {
            holder.layoutCbwtfPhoto.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void onUpdate(List<ReportsModel> list){
        data = list;
        notifyDataSetChanged();
    }

    public void onClear(){
        data.clear();
        notifyDataSetChanged();
    }

    private void showImageDialog(Context context, String title, String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) return;

        String fullUrl;
        if (photoPath.startsWith("http://") || photoPath.startsWith("https://")) {
            fullUrl = photoPath;
        } else {
            String baseUrl = AppStrings.site_url;
            if (!baseUrl.endsWith("/")) baseUrl += "/";
            String cleanPath = photoPath.startsWith("/") ? photoPath.substring(1) : photoPath;
            fullUrl = baseUrl + cleanPath;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_image_viewer, null);
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
        }

        TextView titleTv = view.findViewById(R.id.dialog_image_title);
        ImageView imageView = view.findViewById(R.id.dialog_image_view);
        ImageView closeBtn = view.findViewById(R.id.dialog_image_close);
        ProgressBar progressBar = view.findViewById(R.id.dialog_image_progress);

        titleTv.setText(title);

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Glide.with(context)
                .load(fullUrl)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView hName,hCode,hcf_w,cbwtf_w,diff_w,hcf_addr
                ,cbwtf_addr,date,red_w,blue_w,yellow_w
                ,white_w,hInitial,red_w_c,blue_w_c,tv_cbwtf_weight
                ,yellow_w_c,white_w_c,total_packets_tv;
        ImageView red,blue,white,yellow,red_c,blue_c,white_c,yellow_c,total_packets_img,hcfPhotoView,cbwtfPhotoView;
        View layoutHcfPhoto, layoutCbwtfPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hName = itemView.findViewById(R.id.report_hospital_name);
            hCode = itemView.findViewById(R.id.report_hospital_code);
            hInitial = itemView.findViewById(R.id.report_hospital_name_initial);
            hcf_w = itemView.findViewById(R.id.report_hcf_weight);
            cbwtf_w = itemView.findViewById(R.id.report_cbwtf_weight);
            tv_cbwtf_weight = itemView.findViewById(R.id.tv_cbwtf_weight);
            diff_w = itemView.findViewById(R.id.report_difference_weight);
            hcf_addr = itemView.findViewById(R.id.report_hcf_address);
            cbwtf_addr = itemView.findViewById(R.id.report_cbwtf_address);
            date = itemView.findViewById(R.id.report_date);
            red_w = itemView.findViewById(R.id.report_red_weight);
            blue_w = itemView.findViewById(R.id.report_blue_weight);
            yellow_w = itemView.findViewById(R.id.report_yellow_weight);
            white_w = itemView.findViewById(R.id.report_white_weight);
            red = itemView.findViewById(R.id.report_red_iv);
            blue = itemView.findViewById(R.id.report_blue_iv);
            white = itemView.findViewById(R.id.report_white_iv);
            yellow = itemView.findViewById(R.id.report_yellow_iv);

            total_packets_tv = itemView.findViewById(R.id.report_total_waste_packet_tv);
            total_packets_img = itemView.findViewById(R.id.report_total_waste_packet_img);

            red_w_c = itemView.findViewById(R.id.report_red_weight_cbwtf);
            blue_w_c = itemView.findViewById(R.id.report_blue_weight_cbwtf);
            yellow_w_c = itemView.findViewById(R.id.report_yellow_weight_cbwtf);
            white_w_c = itemView.findViewById(R.id.report_white_weight_cbwtf);
            red_c = itemView.findViewById(R.id.report_red_iv_cbwtf);
            blue_c = itemView.findViewById(R.id.report_blue_iv_cbwtf);
            white_c = itemView.findViewById(R.id.report_white_iv_cbwtf);
            yellow_c = itemView.findViewById(R.id.report_yellow_iv_cbwtf);

            layoutHcfPhoto = itemView.findViewById(R.id.layout_hcf_photo);
            layoutCbwtfPhoto = itemView.findViewById(R.id.layout_cbwtf_photo);
            hcfPhotoView = itemView.findViewById(R.id.report_hcf_photo_view);
            cbwtfPhotoView = itemView.findViewById(R.id.report_cbwtf_photo_view);
        }
    }
}
