package com.teramatrix.vos.volvouptime;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.teramatrix.vos.MyTicketActivity;
import com.teramatrix.vos.R;
import com.teramatrix.vos.SplashActivity;
import com.teramatrix.vos.asynctasks.AppVersionChekerAsyn;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.reciver.InternetAvailabilityRecever;
import com.teramatrix.vos.utils.UtilityFunction;
import com.teramatrix.vos.volvouptime.adapter.VehicleAdapter;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeGetData;
import com.teramatrix.vos.volvouptime.asyntask.UpTimeGetReasons;
import com.teramatrix.vos.volvouptime.custom.ConfirmationDialog;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.custom.OnItemClickListener;
import com.teramatrix.vos.volvouptime.models.UpTimeReasonsModel;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by arun.singh on 6/1/2018.
 * This is Home activity of UpTime application.
 * This activity display list of vehicles for a site.
 */
public class UpTimeVehicleListActivity extends UpTimeBaseActivity implements View.OnClickListener,ConfirmationDialog.I_ConfirmationResponse, AppVersionChekerAsyn.I_AppVersionChekerAsyn {

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private List<VehicleModel> vehicleModelList;
    private Dialog confirmjobDialog = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    SearchView searchView;
    ViewPager viewPager;
    TabLayout tablayout;
    UpTImeVehicleListFragment upTImeVehicleListFragment;
    SparseArray<Fragment> registeredFragments = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //define layout with no title with full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        //change Status bar color
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.volvo_blue));
        }

        setContentView(R.layout.activity_uptime_vehicle_list);

        //initialize basic views
        initViews();

    }

    @Override
    protected void onResume() {
        super.onResume();

        registerBReciver(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBReciver();

    }

    @Override
    public void syncCompleteCallback(int status) {
        super.syncCompleteCallback(status);
        //ca;; from here uptime vehicle list fragment on load method
        //loadVehicleList();
        if (fragmentList.get(viewPager.getCurrentItem()) instanceof UpTImeVehicleListFragment) {
            ((UpTImeVehicleListFragment) viewPagerAdapter.getItem(viewPager.getCurrentItem())).loadVehicleList();
        }
    }

        ViewPagerAdapter viewPagerAdapter;

        /**
         * Initialize basic views
         */
        private void initViews () {
            upTImeVehicleListFragment = new UpTImeVehicleListFragment();
            //findViewById(R.id.icon_search).setVisibility(View.VISIBLE);
            //findViewById(R.id.icon_search).setOnClickListener(this);
            findViewById(R.id.icon_refresh).setVisibility(View.VISIBLE);
            findViewById(R.id.icon_refresh).setOnClickListener(this);
            findViewById(R.id.icon_logout).setVisibility(View.VISIBLE);
            findViewById(R.id.icon_logout).setOnClickListener(this);

            viewPager = (ViewPager) findViewById(R.id.viewPager);
            tablayout = (TabLayout) findViewById(R.id.tablayout);
            searchView = (SearchView) findViewById(R.id.searchView);
            searchView.setQueryHint("Search Chasis");
            fragmentList = new ArrayList<>();
            fragmentList.add(new UpTImeVehicleListFragment());
            fragmentList.add(new UptimeEngineReadingFragment());
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentList);
            viewPager.setAdapter(viewPagerAdapter);
            tablayout.setupWithViewPager(viewPager);
            //tablayout.setVisibility(View.GONE);
            new AppVersionChekerAsyn(this, "1001", this).execute();
            searchView.setInputType(InputType.TYPE_CLASS_NUMBER);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query)
                {
                    UpTImeVehicleListFragment upTImeVehicleListFragment =(UpTImeVehicleListFragment)getRegisteredFragment(viewPager.getCurrentItem());
                    upTImeVehicleListFragment.getFilterfromFragment(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {

                    UpTImeVehicleListFragment upTImeVehicleListFragment =(UpTImeVehicleListFragment)getRegisteredFragment(viewPager.getCurrentItem());
                    upTImeVehicleListFragment.getFilterfromFragment(query);
                    return false;
                }
            });




        }


        @Override
        public void onClick (View v){
            switch (v.getId()) {
            /*case R.id.icon_search :
            {

            }break;*/
                case R.id.icon_refresh: {
                    //Re-load Vehicle List from Server
                    if (fragmentList.get(viewPager.getCurrentItem()) instanceof UpTImeVehicleListFragment) {
                        ((UpTImeVehicleListFragment) viewPagerAdapter.getItem(viewPager.getCurrentItem())).loadVehicleList();
                    }else {
                        ((UptimeEngineReadingFragment)viewPagerAdapter.getItem(viewPager.getCurrentItem())).onRefresh();
                    }
                }
                break;
                case R.id.icon_logout: {
                    ConfirmationDialog.showConfirmationDialog(UpTimeVehicleListActivity.this, "Are you sure you want to Log Out?", UpTimeVehicleListActivity.this);
                }
                break;

            }
        }

        @Override
        public void onPositive_ConfirmationDialog () {
            //Clear All Login Credentials by clearing sharedpreferemce
            //new VECVPreferences(UpTimeVehicleListActivity.this).clearSharedPreference();
            new VECVPreferences(UpTimeVehicleListActivity.this).setCheckLogin(false);
            new VECVPreferences(UpTimeVehicleListActivity.this).setCheckconfigure(false);

            //Launch Splash Screen
            startActivity(new Intent(UpTimeVehicleListActivity.this, SplashActivity.class));
            //close this activity
            finish();
        }

        @Override
        public void onNegative_ConfirmationDialog () {

        }


        List<Fragment> fragmentList;

    @Override
    public void I_AppVersionChekerAsyn_onSuccess(String playStoreVersionName) {
        String curretnVersion = UtilityFunction.getAppVersion(UpTimeVehicleListActivity.this);
        if (!curretnVersion.equalsIgnoreCase(playStoreVersionName)) {
            showAppUpgradePopUp(UpTimeVehicleListActivity.this);
        }
    }

    @Override
    public void I_AppVersionChekerAsyn_onFailure(String message) {

    }
    public void showAppUpgradePopUp(Context context) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.app_version_upgrade);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("market://details?id=" + getPackageName())));
            }
        });
        dialog.show();
    }

    /**
         * view pager adapter to set fragments
         */
        public class ViewPagerAdapter extends FragmentPagerAdapter {
            List<Fragment> fragmentList;


            public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
                super(fm);
                this.fragmentList = fragmentList;
            }

            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                String title = null;
                if (position == 0) {
                    title = "Vehicle List";
                } else if (position == 1) {
                    title = "Engine Hours";
                }

                return title;
            }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

}