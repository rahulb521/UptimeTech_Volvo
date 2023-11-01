package com.teramatrix.vos.volvouptime;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teramatrix.vos.R;
import com.teramatrix.vos.firebase.config.Config;
import com.teramatrix.vos.volvouptime.adapter.VehicleAdapter;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeGetData;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeGetReasons;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.custom.OnItemClickListener;
import com.teramatrix.vos.volvouptime.models.UpTimeAddedReasonsModel;
import com.teramatrix.vos.volvouptime.models.UpTimeReasonsModel;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * @author neeraj on 26/12/18.
 */
public class UpTImeVehicleListFragment extends Fragment implements OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, UpTimeGetData.I_UpTimeGetVehicles, UpTimeGetReasons.I_UpTimeGetReasons {
    View view;

    String TAG = this.getClass().getSimpleName();
    private RecyclerView recyclerView;
    public  VehicleAdapter vehicleAdapter;
    public  List<VehicleModel> vehicleModelList;
    public  List<VehicleModel> vehicleModelFilterList;
    public  List<String> vehicleChasisNo;
    public  List<VehicleModel> vehicleModelFilterListSecond;
    private Dialog confirmjobDialog = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view==null){
            view=inflater.inflate(R.layout.fragment_uptime_vehicle_list,container,false);
        }

        Log.e(TAG, "onCreateView: " );
        initViews();
        mSwipeRefreshLayout.setRefreshing(true);
        loadVehicleList();
        //Get All delayed reasons and save them in local DB
        if (getActivity().getIntent().getBooleanExtra("isFromLoginPage", false)) {
            Log.e(TAG, "onCreateView: getdelay reason " );
            new UpTimeGetReasons(getActivity(), "teramatrix", this).execute();
        }
        return view;
    }

  /*  public void scheduleAlarm()
    {
        int i = 20;
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getActivity(), 234324243, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (i * 1000), pendingIntent);
    }
*/
    private void showCustomList()
    {
        Intent intent = new Intent(getActivity(),CustomDialogActivity.class);
        Config.vehicleModelList = vehicleModelFilterList;
        Config.vehicleModelFilterListSecond = vehicleModelFilterListSecond;
        startActivity(intent);
    }



    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: " );
        //loadVehicleList();
    }

    private void initViews(){
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        vehicleModelList = new ArrayList<>();
        vehicleModelFilterList = new ArrayList<>();
        vehicleChasisNo = new ArrayList<>();
        vehicleModelFilterListSecond = new ArrayList<>();
        vehicleAdapter = new VehicleAdapter(getActivity(),vehicleModelList,vehicleChasisNo , this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(vehicleAdapter);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

    }

    @Override
    public void onItemClick(VehicleModel item) {
        Log.e(TAG, item.getPreEnginehours()+" onItemClick: item "+item.isDown );
        Intent mainIntent = null;
        if (item.isDown() == 0) {
            mainIntent = new Intent(getActivity(),
                    UpTimeTicketDetailsActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainIntent.putExtra("registration_no", item.getReg_no_value());
            mainIntent.putExtra("door_no", item.getDoorNumber());
            mainIntent.putExtra("chasis_number", item.getChassisNumber());

            startActivityForResult(mainIntent, 1001);
        } else if (item.isDown() == 1) {
            mainIntent = new Intent(getActivity(),
                    UpTimeRegisterActivity.class);
            mainIntent.putExtra("type", UpTimeRegisterActivity.TYPE_JOB);
            mainIntent.putExtra("registration_no", item.getReg_no_value());
            mainIntent.putExtra("door_no", item.getDoorNumber());
            mainIntent.putExtra("chasis_number", item.getChassisNumber());

            mainIntent.putExtra("preenginehoursintent",item.getPreEnginehours());

            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(mainIntent, 1001);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                //Re-load Vehicle List
                mSwipeRefreshLayout.setRefreshing(true);
                loadVehicleList();
            }
        }
    }

    public void getFilterfromFragment(String text)
    {
        vehicleAdapter.filter(text);
        if (text.equals(""))
        {
            vehicleAdapter = new VehicleAdapter(getActivity(),vehicleModelList,vehicleChasisNo , this);
            recyclerView.setAdapter(vehicleAdapter);
        }
    }

    public void resetList()
    {
        vehicleAdapter = new VehicleAdapter(getActivity(),vehicleModelList,vehicleChasisNo , this);
        recyclerView.setAdapter(vehicleAdapter);
    }

    @Override
    public void onRefresh() {
        loadVehicleList();
    }
    /**
     * Load vehicles data
     */
    public void loadVehicleList() {
        DAO.getAllVehicles(getActivity(), this);
    }

    @Override
    public void vehicleList(List<VehicleModel> vehicleModels) {
        vehicleModelList.clear();
        vehicleChasisNo.clear();
        vehicleModelFilterList.clear();
        vehicleModelFilterListSecond.clear();
        if (Config.vehicleModelFilterListSecond!=null)
        {
            Config.vehicleModelFilterListSecond.clear();
        }
        if(Config.vehicleModelList!=null)
        {
            Config.vehicleModelList.clear();
        }
        for (int i=0; i<vehicleModels.size();i++)
        {
            vehicleChasisNo.add(vehicleModels.get(i).getChassisNumber());
        }
        for (int i=0; i<vehicleModels.size();i++)
        {
            if (vehicleModels.get(i).isDown()==0)
            {
                if(vehicleModels.get(i).getJobStartDate()!=null)
                {
                    if (getJobDateDifference(vehicleModels.get(i).getJobStartDate()) == 1)
                    {
                        List<UpTimeAddedReasonsModel> upTimeAddedReasonsModels = DAO.getAddedReasons(vehicleModels.get(i).getTicketId());
                        Log.v("Difference:-", " jobStartDate: " + upTimeAddedReasonsModels);
                        if (upTimeAddedReasonsModels.size() == 0) {
                            vehicleModelFilterList.add(vehicleModels.get(i));
                        }
                    } else if (getJobDateDifference(vehicleModels.get(i).getJobStartDate()) > 1)
                    {
                        List<UpTimeAddedReasonsModel> upTimeAddedReasonsModels = DAO.getAddedReasons(vehicleModels.get(i).getTicketId());
                        Log.v("Difference:-", " jobStartDate: 1111 " + upTimeAddedReasonsModels);
                        if (upTimeAddedReasonsModels.size() == 0) {
                            vehicleModelFilterListSecond.add(vehicleModels.get(i));
                        }
                    }
                }
            }
        }
        if (vehicleModels.size() > 0) {
            view.findViewById(R.id.txt_empty_view).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            vehicleModelList.addAll(vehicleModels);
            vehicleAdapter.notifyDataSetChanged();
        } else {
            view.findViewById(R.id.txt_empty_view).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        // Stopping swipe refresh
        mSwipeRefreshLayout.setRefreshing(false);

        if (vehicleModelFilterList.size()>0 || vehicleModelFilterListSecond.size()>0 )
        {
            showCustomList();
        }
    }

    private long getJobDateDifference(String jobStartDate)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long days = 0;
        try {
            Date oldDate = dateFormat.parse(jobStartDate);
            System.out.println(oldDate);
            Date currentDate = new Date();
            long diff = currentDate.getTime() - oldDate.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            days = hours / 24;
            if (oldDate.before(currentDate))
            {
                Log.v("Difference:-", " jobStartDate: " + jobStartDate+" days: " + days);
                if (days>=1)
                {
                    return days;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    @Override
    public void reasonsList(List<UpTimeReasonsModel> upTimeReasonsModels) {

    }
}
