package com.teramatrix.vos.volvouptime;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teramatrix.vos.R;
import com.teramatrix.vos.firebase.config.Config;
import com.teramatrix.vos.utils.CustomTextViewArial;
import com.teramatrix.vos.volvouptime.custom.OnItemClickListener;
import com.teramatrix.vos.volvouptime.models.VehicleModel;

import java.util.ArrayList;

public class CustomDialogActivity extends AppCompatActivity  implements OnItemClickListener {

    TextView btnSkip,rl_title_bar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_custom_dialog);
        this.setFinishOnTouchOutside(false);
        btnSkip =(TextView)findViewById(R.id.btnSkip);
        rl_title_bar_title =(TextView)findViewById(R.id.rl_title_bar_title);
        rl_title_bar_title.setText(getResources().getString(R.string.update_delay_reason));
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        showCustomList();
    }

    private void showCustomList()
    {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        MyListAdapter adapter = new MyListAdapter(Config.vehicleModelList,Config.vehicleModelFilterListSecond,this,this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(VehicleModel item) {
        Intent mainIntent = null;
        if (item.isDown() == 0) {
            mainIntent = new Intent(this, UpTimeTicketDetailsActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainIntent.putExtra("registration_no", item.getReg_no_value());
            mainIntent.putExtra("door_no", item.getDoorNumber());
            mainIntent.putExtra("chasis_number", item.getChassisNumber());
            startActivityForResult(mainIntent, 1001);
            finish();
        } else if (item.isDown() == 1) {
            mainIntent = new Intent(this, UpTimeRegisterActivity.class);
            mainIntent.putExtra("type", UpTimeRegisterActivity.TYPE_JOB);
            mainIntent.putExtra("registration_no", item.getReg_no_value());
            mainIntent.putExtra("door_no", item.getDoorNumber());
            mainIntent.putExtra("chasis_number", item.getChassisNumber());
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(mainIntent, 1001);
            finish();
        }
    }
}