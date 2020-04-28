package com.teramatrix.vos.volvouptime;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.teramatrix.vos.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author neeraj on 17/12/18.
 */
public class EngineHourReadingDialog extends Dialog {
    private Context context;
    TextView btn_ok, msg;

    public EngineHourReadingDialog(@NonNull Context context,String msg) {
        super(context);
        this.context = context;
        initDialog(msg);
    }

    private void initDialog(String message) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dialog_reading_enginehours, null);
        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.gravity = Gravity.CENTER;
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        msg = findViewById(R.id.msg);
        btn_ok = findViewById(R.id.btn_ok);
        msg.setText(message);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private int getTotalHours() {
        Calendar calendar = Calendar.getInstance();
        Calendar mycal = new GregorianCalendar(calendar.YEAR, calendar.MONTH, calendar.DAY_OF_MONTH);
        // Get the number of days in that month
        int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH); // 28
        return daysInMonth;
    }
}
