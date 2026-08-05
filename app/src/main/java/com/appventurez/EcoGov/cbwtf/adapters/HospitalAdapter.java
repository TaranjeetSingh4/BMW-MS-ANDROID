package com.appventurez.EcoGov.cbwtf.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.appventurez.EcoGov.R;
import com.appventurez.EcoGov.database.HospitalModel;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder>{

    Context context;
    List<HospitalModel> data;

    MyHospitalEventListener myHospitalEventListener;

    public interface MyHospitalEventListener{
        void updateUI();
    }

    public HospitalAdapter(Context context, List<HospitalModel> data,MyHospitalEventListener myHospitalEventListener) {
        this.context = context;
        this.data = data;
        this.myHospitalEventListener = myHospitalEventListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weight_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        HospitalModel model = data.get(position);

        if (model.getWaste_color().trim().equalsIgnoreCase("red")){
            holder.card.setStrokeColor(context.getResources().getColor(android.R.color.holo_red_light));
            holder.weight_name.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
            Glide.with(context).load(R.drawable.red).into(holder.type_img);
        }else if (model.getWaste_color().trim().equalsIgnoreCase("blue")){
            holder.card.setStrokeColor(context.getResources().getColor(android.R.color.holo_blue_light));
            holder.weight_name.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light));
            Glide.with(context).load(R.drawable.blue).into(holder.type_img);
        }else if (model.getWaste_color().trim().equalsIgnoreCase("yellow")){
            holder.card.setStrokeColor(context.getResources().getColor(android.R.color.holo_orange_light));
            holder.weight_name.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
            Glide.with(context).load(R.drawable.yellow).into(holder.type_img);
        }else if (model.getWaste_color().trim().equalsIgnoreCase("yellow c")){
            holder.card.setStrokeColor(context.getResources().getColor(android.R.color.holo_orange_light));
            holder.weight_name.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
            Glide.with(context).load(R.drawable.yellow).into(holder.type_img);
        }else if (model.getWaste_color().trim().equalsIgnoreCase("white")){
            holder.card.setStrokeColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.weight_name.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            Glide.with(context).load(R.drawable.gray).into(holder.type_img);
        }
        holder.weight_name.setText(model.getWaste_color().concat(" ").concat("Bag"));
        holder.waste_weight.setText(model.getWaste_weight().trim());
        holder.waste_weight_g.setText(model.getWaste_weight_g().trim());

        holder.delete_weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askDelete(position);
            }
        });

        holder.update_weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.waste_weight.getText().toString().trim().isEmpty() && holder.waste_weight_g.getText().toString().trim().isEmpty()){
                    data.set(position,new HospitalModel(model.getHcf_code(), model.hcf_name, model.waste_color,"000","000",model.getQr_code()));
                    notifyItemChanged(position);
                    myHospitalEventListener.updateUI();
                }else if (holder.waste_weight.getText().toString().trim().isEmpty() && !holder.waste_weight_g.getText().toString().trim().isEmpty()){
                    data.set(position,new HospitalModel(model.getHcf_code(), model.hcf_name, model.waste_color,"000",holder.waste_weight_g.getText().toString().trim(),model.getQr_code()));
                    notifyItemChanged(position);
                    myHospitalEventListener.updateUI();
                }else if (!holder.waste_weight.getText().toString().trim().isEmpty() && holder.waste_weight_g.getText().toString().trim().isEmpty()){
                    data.set(position,new HospitalModel(model.getHcf_code(), model.hcf_name, model.waste_color,holder.waste_weight.getText().toString().trim(),"000",model.getQr_code()));
                    notifyItemChanged(position);
                    myHospitalEventListener.updateUI();
                }else {
                    data.set(position,new HospitalModel(model.getHcf_code(), model.hcf_name, model.waste_color,holder.waste_weight.getText().toString().trim(),holder.waste_weight_g.getText().toString().trim(),model.getQr_code()));
                    notifyItemChanged(position);
                    myHospitalEventListener.updateUI();
                }
            }
        });

        myHospitalEventListener.updateUI();

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView weight_name;
        EditText waste_weight,waste_weight_g;
        ImageView delete_weight,type_img,update_weight;
        MaterialCardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            weight_name = itemView.findViewById(R.id.weight_name_tv);
            waste_weight = itemView.findViewById(R.id.weight_et);
            waste_weight_g = itemView.findViewById(R.id.weight_et_g);
            delete_weight = itemView.findViewById(R.id.weight_delete_button);
            card = itemView.findViewById(R.id.weight_card);
            type_img = itemView.findViewById(R.id.weight_type_img);
            update_weight = itemView.findViewById(R.id.weight_update_button);
        }
    }

    private void askDelete(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Do you want to Delete ?");
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            data.remove(position);
            notifyDataSetChanged();
            myHospitalEventListener.updateUI();
        });
        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
