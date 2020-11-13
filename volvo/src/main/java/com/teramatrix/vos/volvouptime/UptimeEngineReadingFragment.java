package com.teramatrix.vos.volvouptime;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.teramatrix.vos.R;
import com.teramatrix.vos.asynctasks.EngineReading;
import com.teramatrix.vos.asynctasks.SaveEngineReading;
import com.teramatrix.vos.checkinternet.CheckInternetConnection;
import com.teramatrix.vos.firebase.config.Config;
import com.teramatrix.vos.restapi.RestClient;
import com.teramatrix.vos.volvouptime.adapter.EngineHourAdapter;
import com.teramatrix.vos.volvouptime.adapter.VehicleAdapter;
import com.teramatrix.vos.volvouptime.models.EngineHourReadingModel;
import com.teramatrix.vos.volvouptime.models.EngineHourUtlilizationReadingDialog;
import com.teramatrix.vos.volvouptime.models.VehicleModel;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author neeraj on 26/12/18.
 */
public class UptimeEngineReadingFragment extends android.support.v4.app.Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, EngineReading.EngineReadingListListener, SaveEngineReading.EngineReadingSaveListener {
    View view;
    TextView month_select;
    Long requiredTime;
    EngineHourAdapter engineHourAdapter;
    private RecyclerView recyclerView;
    private Dialog confirmjobDialog = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static List<EngineHourReadingModel> engineHourReadingModels = new ArrayList<>();
    LinearLayout lin_action;
    TextView save_btn;
    TextView current_month, prev_month;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_uptime_engine_reading, container, false);

        }
        initViews();

        return view;
    }

    private void initViews() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        lin_action = view.findViewById(R.id.lin_action);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        month_select = view.findViewById(R.id.month_select);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        month_select.setOnClickListener(this);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM yyyy");
        String month_name = month_date.format(calendar.getTime());
        month_select.setText(month_name);
        requiredTime = calendar.getTimeInMillis();
        //call api with current date by diffault
        engineHourAdapter = new EngineHourAdapter(getActivity(), requiredTime, engineHourReadingModels);
        recyclerView.setAdapter(engineHourAdapter);
        engineHourAdapter.setLayoutManager(recyclerView.getLayoutManager());
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container);
        //mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setEnabled(false);
        callEngineReadingApi(false);
        save_btn = view.findViewById(R.id.save_btn);
        save_btn.setOnClickListener(this);
        prev_month = view.findViewById(R.id.prev_month);
        current_month = view.findViewById(R.id.current_month);
        prev_month.setText(getPreviousMonthName(requiredTime, 1));
        current_month.setText(getPreviousMonthName(requiredTime, 0));
    }

    private String getPreviousMonthName(Long ms, int prev) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ms);
        calendar.add(Calendar.MONTH, -prev);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM yyyy");
        String month_name = month_date.format(calendar.getTime());
        return month_name;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.month_select:
                voidShowMonthDialog();
                break;
            case R.id.save_btn:
                //call save api here
                //update data in db if no internet connection set modified true also
                callSaveApi();
                break;
        }
    }

    private void voidShowMonthDialog() {
        final Calendar today = Calendar.getInstance();
        final MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(), new MonthPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int i, int i1) {
                String month = new DateFormatSymbols().getMonths()[i];
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, i1);
                cal.set(Calendar.MONTH, i);
                long tempTme = cal.getTimeInMillis();

                if (tempTme > getCurrentMonthTime()) {
                    EngineHourReadingDialog engineHourReadingDialog = new EngineHourReadingDialog(getActivity(), "Month should be less then current Month.");
                    engineHourReadingDialog.show();
                } else {
                    if (new CheckInternetConnection(getActivity()).isConnectedToInternet()) {
                        requiredTime = cal.getTimeInMillis();
                        month_select.setText(month + "-" + i1);
                        prev_month.setText(getPreviousMonthName(requiredTime, 1));
                        current_month.setText(getPreviousMonthName(requiredTime, 0));
                        engineHourAdapter.setMs(requiredTime);
                    }else {
                        if (getTimeMonth(tempTme).equalsIgnoreCase(getCurrentMonth())) {

                        }else {
                            Toast.makeText(getActivity(), "Please connect to internet for view history", Toast.LENGTH_LONG).show();

                        }
                    }
                }
                // engineHourAdapter.notifyDataSetChanged();
                callEngineReadingApi(false);
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        builder.setActivatedMonth(today.get(Calendar.MONTH))
                .setMinYear(getMinimumYear())
                .setActivatedYear(today.get(Calendar.YEAR))
                .setMaxYear(today.get(Calendar.YEAR))
                .build().show();

    }

    private int getMinimumYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        return calendar.get(Calendar.YEAR);
    }

    private int getMinimumMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return calendar.get(Calendar.MONTH);
    }

    @Override
    public void onRefresh() {
        callEngineReadingApi(false);
        //mSwipeRefreshLayout.setRefreshing(false);
    }

    private void callEngineReadingApi(boolean whenRefrshing) {
        boolean isCurrentMonth = false;
        if (getTimeMonth(requiredTime).equalsIgnoreCase(getCurrentMonth())) {
            isCurrentMonth = true;
            lin_action.setVisibility(View.VISIBLE);
        } else {
            lin_action.setVisibility(View.GONE);
        }
        if (new CheckInternetConnection(getActivity()).isConnectedToInternet()) {
            if (requiredTime > getCurrentMonthTime()) {
                //show error dialog here
                EngineHourReadingDialog engineHourReadingDialog = new EngineHourReadingDialog(getActivity(), "Month should be less then current Month.");
                engineHourReadingDialog.show();
            } else {
                EngineReading engineReading = new EngineReading(getActivity(), isCurrentMonth, getTimeString(requiredTime), whenRefrshing);
                engineReading.setEngineReadingListListener(this);
                engineReading.execute();
            }

        } else {
            //try to get data from local db
            if (isCurrentMonth) {
                List<EngineHourReadingModel> engineHourReadingModels = new Select().from(EngineHourReadingModel.class).orderBy("CurrentMonthUtilizationData ASC").orderBy("DoorNumber ASC").execute();
                this.engineHourReadingModels.clear();
                this.engineHourReadingModels.addAll(engineHourReadingModels);
                engineHourAdapter.notifyDataSetChanged();
                if (!whenRefrshing) {
                    checkAnyEntryPending();
                }
            } else {
                Toast.makeText(getActivity(), "Please connect to internet for view history", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String getTimeString(long ms) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ms);
        SimpleDateFormat month_date = new SimpleDateFormat("yyyy-MM-dd");
        return month_date.format(calendar.getTime());
    }

    private String getTimeMonth(long ms) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ms);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM yyyy");
        return month_date.format(calendar.getTime());
    }

    private String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM yyyy");
        return month_date.format(calendar.getTime());
    }

    private long getCurrentMonthTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTimeInMillis();
    }

    @Override
    public void engineReadingList(List<EngineHourReadingModel> engineHourReadingModels, boolean whenRefreshing) {
        this.engineHourReadingModels.clear();
        this.engineHourReadingModels.addAll(engineHourReadingModels);
        for (int i=0; i<engineHourReadingModels.size();i++)
        {
            if (engineHourReadingModels.get(i).getGetUtilizationDatafirst().equals("true"))
            {
                Config.isClickable=false;
                break;
            }
            else
            {
                Config.isClickable = true;
            }
        }
        engineHourAdapter.notifyDataSetChanged();
        if (!whenRefreshing) {
            checkAnyEntryPending();
        }

    }

    private void callSaveApi() {

        String reg_number = "", current_data = "",save_time="",prev_util="";
        boolean anyData = false;
        for (EngineHourReadingModel engineHourReadingModel :
                engineHourReadingModels) {
            if (engineHourReadingModel.isModified() && !engineHourReadingModel.getCurrentMonthUtilizationData().equalsIgnoreCase("0")) {
                new Update(EngineHourReadingModel.class)
                        .set("isModified = 0, " + "CurrentMonthUtilizationData=" + engineHourReadingModel.getCurrentMonthUtilizationData())
                        .where("RegistrationNumber = ?", engineHourReadingModel.getRegistrationNumber())
                        .execute();
                anyData = true;
                reg_number = reg_number + engineHourReadingModel.getRegistrationNumber() + ",";
                current_data = current_data + engineHourReadingModel.getCurrentMonthUtilizationData() + ",";
                save_time=save_time+engineHourReadingModel.getInRequiredTime()+",";
                prev_util=prev_util+engineHourReadingModel.getPreviousMonthUtilizationData()+",";
               // Config.isClickable=true;
            }
        }
        if (anyData) {
            reg_number = reg_number.substring(0, reg_number.length() - 1);
            current_data = current_data.substring(0, current_data.length() - 1);
            save_time=save_time.substring(0,save_time.length()-1);
            prev_util=prev_util.substring(0,prev_util.length()-1);
            if (new CheckInternetConnection(getActivity()).isConnectedToInternet()) {
                SaveEngineReading saveEngineReading = new SaveEngineReading(getActivity(), current_data, reg_number,save_time,prev_util);
                saveEngineReading.setEngineReadingSaveListener(this);
                saveEngineReading.execute();
            } else {
                for (EngineHourReadingModel engineHourReadingModel :
                        engineHourReadingModels) {
                    if (engineHourReadingModel.isModified() && !engineHourReadingModel.getCurrentMonthUtilizationData().equalsIgnoreCase("0")) {
                        new Update(EngineHourReadingModel.class)
                                .set("isModified = ?, " + "CurrentMonthUtilizationData = ?" +
                                ", RequiredTime = ?",new String[]{"1",engineHourReadingModel.getCurrentMonthUtilizationData(),engineHourReadingModel.getInRequiredTime()})
                                .where("RegistrationNumber = ?", engineHourReadingModel.getRegistrationNumber())
                                .execute();
                    }
                }
                callEngineReadingApi(true);
                EngineHourReadingDialog engineHourReadingDialog = new EngineHourReadingDialog(getActivity(), "Data saved successfully");
                engineHourReadingDialog.show();
            }
        }

    }

    @Override
    public void engineReadingSaved() {
        callEngineReadingApi(true);
        EngineHourReadingDialog engineHourReadingDialog = new EngineHourReadingDialog(getActivity(), "Data Saved Successfully");
        engineHourReadingDialog.show();
        hideKeyboard(getActivity());
    }

    private void checkAnyEntryPending() {
        for (EngineHourReadingModel engineHourReadingModel : engineHourReadingModels
                ) {
            if (engineHourReadingModel.getCurrentMonthUtilizationData().equalsIgnoreCase("0")) {
                //setdata pending true
                EngineHourUtlilizationReadingDialog engineHourReadingDialog = new EngineHourUtlilizationReadingDialog(getActivity(), "Please enter engine hours utilization for last month");
                engineHourReadingDialog.show();
                break;
            }
        }
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void getFilterfromFragment(String text)
    {
        engineHourAdapter.filter(text);
        if (text.equals(""))
        {
            engineHourAdapter = new EngineHourAdapter(getActivity(), requiredTime, engineHourReadingModels);
            recyclerView.setAdapter(engineHourAdapter);
        }
    }

    public void resetList()
    {
        engineHourAdapter = new EngineHourAdapter(getActivity(), requiredTime, engineHourReadingModels);
        recyclerView.setAdapter(engineHourAdapter);
    }

}
