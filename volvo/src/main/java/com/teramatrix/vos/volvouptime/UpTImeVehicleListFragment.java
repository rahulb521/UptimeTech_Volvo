package com.teramatrix.vos.volvouptime;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
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

import com.teramatrix.vos.R;
import com.teramatrix.vos.volvouptime.adapter.VehicleAdapter;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeGetData;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeGetReasons;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.custom.OnItemClickListener;
import com.teramatrix.vos.volvouptime.models.UpTimeReasonsModel;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * @author neeraj on 26/12/18.
 */
public class UpTImeVehicleListFragment extends android.support.v4.app.Fragment implements OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, UpTimeGetData.I_UpTimeGetVehicles, UpTimeGetReasons.I_UpTimeGetReasons {
    View view;

    private RecyclerView recyclerView;
    public  VehicleAdapter vehicleAdapter;
    public  List<VehicleModel> vehicleModelList;
    public  List<String> vehicleChasisNo;
    private Dialog confirmjobDialog = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view==null){
            view=inflater.inflate(R.layout.fragment_uptime_vehicle_list,container,false);
        }
        initViews();
        mSwipeRefreshLayout.setRefreshing(true);
        loadVehicleList();
        //Get All delayed reasons and save them in local DB
        if (getActivity().getIntent().getBooleanExtra("isFromLoginPage", false)) {
            new UpTimeGetReasons(getActivity(), "teramatrix", this).execute();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initViews(){
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        vehicleModelList = new ArrayList<>();
        vehicleChasisNo = new ArrayList<>();
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
        for (int i=0; i<vehicleModels.size();i++)
        {
            vehicleChasisNo.add(vehicleModels.get(i).getChassisNumber());
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
    }

    @Override
    public void reasonsList(List<UpTimeReasonsModel> upTimeReasonsModels) {

    }
}
