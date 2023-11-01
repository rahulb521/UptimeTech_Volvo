package com.teramatrix.vos.volvouptime.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by Arun.Singh on 3/19/2018.
 * This is adapter class to generate vehicle List on UpTime Home Screen(UpTimeVehicleLsitActivity)
 */

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.MyViewHolder> {

    String TAG = this.getClass().getSimpleName();
    static List<VehicleModel> vehicleModelList;
    static List<VehicleModel> vehicleModelFilterList;
    static List<String> vehicleChasisList;
    private Context context;
    private OnItemClickListener listener;
    Toast toast;

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
                public void onClick(View v)
                {
                    //listener.onItemClick(item);
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
                            Log.e(TAG, "onClick: config clickable "+Config.isClickable );
                        //if (Config.isClickable)
                        {
                            listener.onItemClick(item);
                        }
                    }
                }
            });
        }

    }


    public VehicleAdapter(Context context, List<VehicleModel> vehicleModelList, List<String> vehicleChasisList,OnItemClickListener listener) {
        this.context = context;
        this.vehicleModelList = vehicleModelList;
        this.vehicleModelFilterList = vehicleModelList;
        this.vehicleChasisList = vehicleChasisList;
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


    public void filter(String text)
    {
        List<VehicleModel> temp = new ArrayList();
        vehicleModelList = vehicleModelFilterList;
        for(VehicleModel d: vehicleModelList)
        {
            if(d.getChassisNumber().contains(text) || d.getDoorNumber().contains(text))
            {
                temp.add(d);
            }
        }
        if (temp.size()==0)
        {
           showAToast("Data not found");
        }
        Log.v("ListSize:",temp.toString());
        updateList(temp);
    }

    public void updateList(List<VehicleModel> list){
        vehicleModelList = list;
        notifyDataSetChanged();
    }

    public void showAToast (String message){
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }


}
