package com.teramatrix.vos.volvouptime.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Update;
import com.teramatrix.vos.R;
import com.teramatrix.vos.volvouptime.UptimeEngineReadingFragment;
import com.teramatrix.vos.volvouptime.models.EngineHourReadingModel;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * @author neeraj on 26/12/18.
 */
public class EngineHourAdapter extends RecyclerView.Adapter<EngineHourAdapter.ViewHolder> {
    private Context context;
    Long ms;
    List<EngineHourReadingModel> engineHourReadingModels;
    List<EngineHourReadingModel> engineHourReadingFilterModels;
    ViewHolder viewHolder;
    RecyclerView.LayoutManager layoutManager;
    Toast toast;
    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public EngineHourAdapter(Context context, Long ms, List<EngineHourReadingModel> engineHourReadingModels) {
        this.context = context;
        this.ms = ms;
        this.engineHourReadingModels = engineHourReadingModels;
        this.engineHourReadingFilterModels = engineHourReadingModels;
    }

    public Long getMs() {
        return ms;
    }

    public void setMs(Long ms) {
        this.ms = ms;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_vehicle_engine_reading, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(position);

    }

    @Override
    public int getItemCount() {
        if (engineHourReadingModels != null) {
            return engineHourReadingModels.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView current_month, prev_month, data_current, tv_chasis, tv_door;
        EditText edit_current;
        ImageView img_edit;
        MyTextWatcher myTextWatcher;
        public ViewHolder(final View itemView) {
            super(itemView);
            prev_month = itemView.findViewById(R.id.prev_month);
            edit_current = itemView.findViewById(R.id.edit_current);
            tv_door = itemView.findViewById(R.id.tv_door);
            tv_chasis = itemView.findViewById(R.id.tv_chasis);
            data_current = itemView.findViewById(R.id.data_current);
            myTextWatcher = new MyTextWatcher(edit_current);
            img_edit = itemView.findViewById(R.id.img_edit);

            img_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) img_edit.getTag();
//                    ((EditText) layoutManager.findViewByPosition(pos).findViewById(R.id.edit_current)).setHint(
//                            "");
//                    ((EditText) layoutManager.findViewByPosition(pos).findViewById(R.id.edit_current)).setText(
//                            engineHourReadingModels.get(pos).getCurrentMonthUtilizationData());
                    layoutManager.findViewByPosition(pos).findViewById(R.id.edit_current).setEnabled(true);
                    ((EditText) layoutManager.findViewByPosition(pos).findViewById(R.id.edit_current)).requestFocus();
                    ((EditText) layoutManager.findViewByPosition(pos).findViewById(R.id.edit_current)).setSelection(
                            engineHourReadingModels.get(pos).getCurrentMonthUtilizationData().length());
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(((EditText) layoutManager.findViewByPosition(pos).findViewById(R.id.edit_current)), InputMethodManager.SHOW_FORCED);

                }
            });
        }

        public void setData(int position) {
            EngineHourReadingModel engineHourReadingModel = engineHourReadingModels.get(position);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(ms);
            edit_current.setTag(position);
            SimpleDateFormat month_date = new SimpleDateFormat("MMM yyyy");
            String month_name = month_date.format(calendar.getTime());
            img_edit.setTag(position);
            data_current.setText(engineHourReadingModel.getPreviousMonthUtilizationData());
            tv_chasis.setText(engineHourReadingModel.getChassisNumber());
            tv_door.setText(engineHourReadingModel.getDoorNumber());
            edit_current.removeTextChangedListener(myTextWatcher);
            edit_current.setText(engineHourReadingModel.getCurrentMonthUtilizationData());
            edit_current.addTextChangedListener(myTextWatcher);
            if (getCurrentMonth().equalsIgnoreCase(month_name)) {
                //make it editable

                if (engineHourReadingModel.getCurrentMonthUtilizationData().equalsIgnoreCase("0")) {
                    edit_current.setEnabled(true);
                    img_edit.setVisibility(View.INVISIBLE);
                } else {
                    edit_current.setEnabled(false);
                    img_edit.setVisibility(View.VISIBLE);
                }
            } else {
                // non editable
                img_edit.setVisibility(View.GONE);
                edit_current.setEnabled(false);
            }
        }
    }


    private String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-1);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM yyyy");
        String month_name = month_date.format(calendar.getTime());
        return month_name;
    }

    public class MyTextWatcher implements TextWatcher {
        private EditText editText;

        public MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Do whatever you want with position
        }

        @Override
        public void afterTextChanged(Editable s) {

            int position = (int) editText.getTag();
            if (s.toString() != null) {
                try {
                    if (varifyHours(Integer.parseInt(s.toString()), editText,position)) {
                        engineHourReadingModels.get(position).setModified(true);
                        engineHourReadingModels.get(position).setCurrentMonthUtilizationData(s.toString());
                        engineHourReadingModels.get(position).setInRequiredTime(UptimeEngineReadingFragment.getTimeString(System.currentTimeMillis()));
//                        new Update(EngineHourReadingModel.class)
//                                .set("isModified = 1, "+"CurrentMonthUtilizationData="+s.toString())
//                                .where("RegistrationNumber = ?", engineHourReadingModels.get(position).getRegistrationNumber())
//                                .execute();
                    } else {
                        engineHourReadingModels.get(position).setModified(false);
                        engineHourReadingModels.get(position).setCurrentMonthUtilizationData("0");
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean varifyHours(int hours, EditText editText,int pos) {
        int monthhours = getNumberOfDaysInMonth() * 24;
        int totalHours=monthhours+Integer.parseInt(engineHourReadingModels.get(pos).getPreviousMonthUtilizationData());
        if (hours > totalHours) {
            editText.setError("Hours should be less then " + totalHours + " hours");
            return false;
        }else if (hours<Integer.parseInt(engineHourReadingModels.get(pos).getPreviousMonthUtilizationData())){
            editText.setError("Hours can not be less then previous month hours");
            return false;
        }
        return true;
    }

    private int getNumberOfDaysInMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        int iYear = calendar.get(Calendar.YEAR);
        int iMonth = calendar.get(Calendar.MONTH); // 1 (months begin with 0)
        int iDay = 1;

// Create a calendar object and set year and month
        Calendar mycal = new GregorianCalendar(iYear, iMonth, iDay);

// Get the number of days in that month
        return mycal.getActualMaximum(Calendar.DAY_OF_MONTH); // 28
    }


    public void filter(String text)
    {
        List<EngineHourReadingModel> temp = new ArrayList();
        engineHourReadingModels = engineHourReadingFilterModels;
        for(EngineHourReadingModel d: engineHourReadingModels)
        {
            if(d.getChassisNumber().contains(text))
            {
                temp.add(d);
            }
        }
        if (temp.size()==0)
        {
            showAToast("Data not found");

        }
        updateList(temp);
    }

    public void updateList(List<EngineHourReadingModel> list){
        engineHourReadingModels = list;
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
