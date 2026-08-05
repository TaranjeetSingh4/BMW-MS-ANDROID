package com.appventurez.EcoGov.cbwtf.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.appventurez.EcoGov.R;
import com.appventurez.EcoGov.cbwtf.models.getCbwtfReportsModel.DataItem;

import java.text.DecimalFormat;
import java.util.List;

public class GetCbwtfReportsAdapter extends RecyclerView.Adapter<GetCbwtfReportsAdapter.ViewHolder>{
    Context context;
    List<DataItem> data;
    Geocoder geocoder;

    public GetCbwtfReportsAdapter(Context context, List<DataItem> data) {
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
        DataItem model = data.get(position);

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

//        holder.red_w.setText(model.getRed());
//        holder.blue_w.setText(model.getBlue());
//        holder.yellow_w.setText(model.getYellow());
//        holder.white_w.setText(model.getWhite());

        Glide.with(context).load(R.drawable.red).into(holder.red_c);
        Glide.with(context).load(R.drawable.blue).into(holder.blue_c);
        Glide.with(context).load(R.drawable.yellow).into(holder.yellow_c);
        Glide.with(context).load(R.drawable.gray).into(holder.white_c);
//
//        holder.red_w_c.setText(model.getRedCbwtf());
//        holder.blue_w_c.setText(model.getBlueCbwtf());
//        holder.yellow_w_c.setText(model.getYellowCbwtf());
//        holder.white_w_c.setText(model.getWhiteCbwtf());
//
//        Glide.with(context).load(R.drawable.recycling).into(holder.total_packets_img);
//        holder.total_packets_tv.setText(model.getTotalPackets());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void onUpdate(List<DataItem> list){
        data = list;
        notifyDataSetChanged();
    }

    public void onClear(){
        data.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView hName,hCode,hcf_w,cbwtf_w,diff_w,hcf_addr
                ,cbwtf_addr,date,red_w,blue_w,yellow_w
                ,white_w,hInitial,red_w_c,blue_w_c,tv_cbwtf_weight
                ,yellow_w_c,white_w_c,total_packets_tv;
        ImageView red,blue,white,yellow,red_c,blue_c,white_c,yellow_c,total_packets_img;

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
        }
    }
}
