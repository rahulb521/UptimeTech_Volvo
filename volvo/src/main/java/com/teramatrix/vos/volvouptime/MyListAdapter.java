package com.teramatrix.vos.volvouptime;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teramatrix.vos.R;
import com.teramatrix.vos.firebase.config.Config;
import com.teramatrix.vos.volvouptime.custom.OnItemClickListener;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
    public List<VehicleModel> vehicleModelList,vehicleModelFilterListSecond;
    public Context context;
    private OnItemClickListener listener;
    // RecyclerView recyclerView;

    public MyListAdapter(List<VehicleModel> vehicleModelList,List<VehicleModel> vehicleModelFilterListSecond,Context context,OnItemClickListener listener) {
        this.vehicleModelList = vehicleModelList;
        this.vehicleModelFilterListSecond = vehicleModelFilterListSecond;
        vehicleModelFilterListSecond.addAll(vehicleModelList);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.custom_alert_dialog_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final VehicleModel vehicleModel = vehicleModelFilterListSecond.get(position);
        holder.tv_reg_no_value.setText(vehicleModel.getChassisNumber());
        holder.tv_door_no_value.setText(vehicleModel.getDoorNumber());
        if (vehicleModel.isDown() == 0)
        {
            holder.indicator.getBackground().setColorFilter(context.getResources().getColor(R.color.volvo_red), PorterDuff.Mode.SRC_ATOP);
        }
        else if (vehicleModel.isDown() == 1)
        {
            holder.indicator.getBackground().setColorFilter(context.getResources().getColor(R.color.volvo_green), PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            holder.indicator.getBackground().setColorFilter(context.getResources().getColor(R.color.volvo_orange), PorterDuff.Mode.SRC_ATOP);
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),"click on item: "+vehicleModel.getChassisNumber(),Toast.LENGTH_LONG).show();
            }
        });
        holder.bind(vehicleModel, listener);
    }


    @Override
    public int getItemCount() {
        return vehicleModelFilterListSecond.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView tv_reg_no_value,tv_door_no_value;
        public LinearLayout linearLayout;
        public View indicator;
        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_reg_no_value = (TextView) itemView.findViewById(R.id.tv_reg_no_value);
            this.tv_door_no_value = (TextView) itemView.findViewById(R.id.tv_door_no_value);
            this.indicator = (View) itemView.findViewById(R.id.indicator);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
        }

        public void bind(final VehicleModel item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Date c = Calendar.getInstance().getTime();
                    System.out.println("Current time => " + c);
                    SimpleDateFormat df = new SimpleDateFormat("dd", Locale.getDefault());
                    String formattedDate = df.format(c);
                    if (formattedDate.equals("01") || formattedDate.equals("02"))
                    {
                        listener.onItemClick(item);
                    }
                    else
                    {
                        if (Config.isClickable)
                        {
                            listener.onItemClick(item);
                        }
                    }
                }
            });
        }

    }
}
