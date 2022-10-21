package com.teramatrix.vos.volvouptime.custom;

import android.app.Activity;
import android.content.Context;

import com.teramatrix.vos.utils.TimeFormater;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Arun.Singh on 5/17/2018.
 * Calender Utility to select date and time.
 */

public class TimePickerUtilEndDate implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener{


    private Context context;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private String resultTime;
    private I_TimePickerUtilEndDate i_timePickerUtil;
    private int resId;
    private String timeRange_Min;
    private String timeRange_Max;


    public interface I_TimePickerUtilEndDate
    {
        void onTimePickerUtilResultEndDate(String resultTime, int resId);
    }

    public class RequestModel
    {
        public Context context;
        public String timeRange_Min;
        public String timeRange_Max;
        public String default_Time;
        public String tag;
        public I_TimePickerUtilEndDate i_timePickerUtil;
        public int resId;
    }




    /**
     *
     * @param context
     * @param timeRange_Min - minimum time in timePicker , [dd MMM yyyy HH:mm]
     * @param timeRange_Max - maximum time in timePicker , [dd MMM yyyy HH:mm]
     * @param default_Time - default time to be shown selected on timepicker , [dd MMM yyyy HH:mm]
     */
    public void initTimePicker(Context context,String timeRange_Min,String timeRange_Max,String default_Time,I_TimePickerUtilEndDate i_timePickerUtil,int resId)
    {
        this.context = context;
        this.i_timePickerUtil = i_timePickerUtil;
        this.resId = resId;
        this.timeRange_Min = timeRange_Min;
        this.timeRange_Max = timeRange_Max;

        int day  = TimeFormater.getPartOfDateString(default_Time,"dd MMM yyyy HH:mm","day");
        int month  = TimeFormater.getPartOfDateString(default_Time,"dd MMM yyyy HH:mm","month");
        int year  = TimeFormater.getPartOfDateString(default_Time,"dd MMM yyyy HH:mm","year");
        int hour  = TimeFormater.getPartOfDateString(default_Time,"dd MMM yyyy HH:mm","hour");
        int minute  = TimeFormater.getPartOfDateString(default_Time,"dd MMM yyyy HH:mm","minute");

        //Date Selecter
        datePickerDialog = DatePickerDialog.newInstance(
                this,
                year,
                month,
                day
        );
        Calendar now = Calendar.getInstance();

        //set minimum date in date picker
        int timeRange_Min_day  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","day");
        int timeRange_Min_month  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","month");
        int timeRange_Min_year  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","year");
        now.set(timeRange_Min_year,timeRange_Min_month,timeRange_Min_day);
        datePickerDialog.setMinDate(now);

        //set maximum date in date picker
//        int timeRange_Max_day  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","day");
//        int timeRange_Max_month  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","month");
//        int timeRange_Max_year  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","year");
//        now.set(timeRange_Max_year,timeRange_Max_month,timeRange_Max_day);
//        datePickerDialog.setMaxDate(now);


        //Time selector
        timePickerDialog = TimePickerDialog.newInstance(
                this,
                hour,
                minute,
                true

        );
        //set min time(hour / min) in time picker
       /* int timeRange_Min_hour  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","hour");
        int timeRange_Min_minute  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","minute");
        timePickerDialog.setMinTime(timeRange_Min_hour,timeRange_Min_minute,0);*/


        //set max time(hour / min) in time picker
        /*int timeRange_Max_hour  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","hour");
        int timeRange_Max_minute  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","minute");
        timePickerDialog.setMaxTime(timeRange_Max_hour,timeRange_Max_minute,0);*/

        //Show Date selector Dialog
        datePickerDialog.show(((Activity)context).getFragmentManager(),null);

    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {


        int timeRange_Min_day  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","day");
        int timeRange_Min_hour  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","hour");
        int timeRange_Min_minute  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","minute");
        int timeRange_Min_month  = TimeFormater.getPartOfDateString(timeRange_Min,"dd MMM yyyy HH:mm","month");


        int timeRange_Max_day  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","day");
        int timeRange_Max_hour  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","hour");
        int timeRange_Max_minute  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","minute");
        int timeRange_Max_month  = TimeFormater.getPartOfDateString(timeRange_Max,"dd MMM yyyy HH:mm","month");

        if(timeRange_Min_day==timeRange_Max_day && timeRange_Min_month==timeRange_Max_month)
        {
            //if Start day and end day are same
            timePickerDialog.setMinTime(timeRange_Min_hour,timeRange_Min_minute,0);
            timePickerDialog.setMaxTime(timeRange_Max_hour,timeRange_Max_minute,0);
            //Set Minimum selectable time in timePicker


        }else
        {

            //if Start day and end day are not same
            if(dayOfMonth == timeRange_Min_day)
            {
                timePickerDialog.setMinTime(timeRange_Min_hour,timeRange_Min_minute,0);
                timePickerDialog.setMaxTime(23,59,0);
            }else if(dayOfMonth == timeRange_Max_day)
            {
                timePickerDialog.setMinTime(0,0,0);
                timePickerDialog.setMaxTime(timeRange_Max_hour,timeRange_Max_minute,0);
            }


        }



        resultTime = dayOfMonth+" "+(monthOfYear+1)+" "+year;
        String curretnFormat = "dd MM yyyy";
        String newFormat = "dd MMM yyyy";
        resultTime = TimeFormater.convertDateFormate(resultTime,curretnFormat,newFormat);


        timePickerDialog.show(((Activity)context).getFragmentManager(),null);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

        String hr="";
        String min="";

        if(hourOfDay<10)
            hr = 0+""+hourOfDay;
        else
            hr = hourOfDay+"";

        if(minute<10)
            min = 0+""+minute;
        else
            min = minute+"";

        resultTime = resultTime+" "+hr+":"+min;

        i_timePickerUtil.onTimePickerUtilResultEndDate(resultTime,resId);
    }

    /**
     *
     * @param context
     * @param format - retutn time format
     * @param offset - 0-> today, less than 0 -> past days , more than 0 -> future days
     * @param flag -> 0 -> time included  00:00 hours , 1-> time included 23:59 hours
     * @return formated tiem string
     */
    public static String getTimeOffset(Context context,String format,int offset,int flag)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, offset);

        if(flag==0)
        {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
        }else if(flag ==1)
        {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
        }

        Date date = cal.getTime();

        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }
    public static String getTimeOffsetNew(Context context,String format,int flag)
    {
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.DATE,100);
        //cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE));

        
        if(flag==0)
        {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
        }else if(flag ==1)
        {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
        }

        Date date = cal.getTime();

        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

}
