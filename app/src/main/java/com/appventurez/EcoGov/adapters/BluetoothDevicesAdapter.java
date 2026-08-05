package com.appventurez.EcoGov.adapters;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import com.google.android.material.card.MaterialCardView;
import com.appventurez.EcoGov.R;
import com.appventurez.EcoGov.models.BluetoothDevicesModel;

import java.util.List;

public class BluetoothDevicesAdapter extends RecyclerView.Adapter<BluetoothDevicesAdapter.ViewHolder>{

    Context context;
    List<BluetoothDevicesModel> data;

    EventListener eventListener;

    public interface EventListener {
        void onClickBluetoothDevice(BluetoothDevice bluetoothDevice,int status);
    }

    public BluetoothDevicesAdapter(Context context, List<BluetoothDevicesModel> data, EventListener eventListener) {
        this.context = context;
        this.data = data;
        this.eventListener = eventListener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_devices_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BluetoothDevicesModel model = data.get(position);

        if (model.getBluetoothDevice().getName() != null){
            holder.blName.setText(model.getBluetoothDevice().getName());
        }else {
            holder.blName.setText(model.getBluetoothDevice().getAddress());
        }


                    if (model.getPaired() == 0) {
                        holder.blStatus.setText("Not paired");
                    } else {
                        holder.blStatus.setText("Paired");
                    }

                    holder.card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            eventListener.onClickBluetoothDevice(model.getBluetoothDevice(), model.getPaired());
                            //Toast.makeText(context, "device connected", Toast.LENGTH_SHORT).show();
                        }
                    });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView blName,blStatus;
        MaterialCardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.card);
            blName = itemView.findViewById(R.id.bl_name);
            blStatus = itemView.findViewById(R.id.bl_status);

        }
    }
}
