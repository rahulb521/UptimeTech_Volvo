package com.teramatrix.vos.volvouptime.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.teramatrix.vos.R;
import com.teramatrix.vos.volvouptime.custom.OnItemClickListener;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import java.util.List;

/**
 * Created by Arun.Singh on 3/19/2018.
 * This is adapter class to generate vehicle List on UpTime Home Screen(UpTimeVehicleLsitActivity)
 */

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.MyViewHolder> {

    private List<VehicleModel> vehicleModelList;
    private Context context;
    private OnItemClickListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_reg_no_value, tv_door_no_value;
        public View indicator;

        public MyViewHolder(View view) {
            super(view);

            tv_reg_no_value = (TextView) view.findViewById(R.id.tv_reg_no_value);
            tv_door_no_value = (TextView) view.findViewById(R.id.tv_door_no_value);
            indicator = (View) view.findViewById(R.id.indicator);
        }

        public void bind(final VehicleModel item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }

    }


    public VehicleAdapter(Context context, List<VehicleModel> vehicleModelList, OnItemClickListener listener) {
        this.context = context;
        this.vehicleModelList = vehicleModelList;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vehicle_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        VehicleModel vehicleModel = vehicleModelList.get(position);
        holder.tv_reg_no_value.setText(vehicleModel.getChassisNumber());
        holder.tv_door_no_value.setText(vehicleModel.getDoorNumber());

        if (vehicleModel.isDown() == 0)
            holder.indicator.getBackground().setColorFilter(context.getResources().getColor(R.color.volvo_red), PorterDuff.Mode.SRC_ATOP);
        else if (vehicleModel.isDown() == 1)
            holder.indicator.getBackground().setColorFilter(context.getResources().getColor(R.color.volvo_green), PorterDuff.Mode.SRC_ATOP);
        else
            holder.indicator.getBackground().setColorFilter(context.getResources().getColor(R.color.volvo_orange), PorterDuff.Mode.SRC_ATOP);

        //Add Listener to item
        holder.bind(vehicleModel, listener);
    }

    @Override
    public int getItemCount() {
        return vehicleModelList.size();
    }
}
