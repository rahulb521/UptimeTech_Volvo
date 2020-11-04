package com.teramatrix.vos.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.logs.CompleteLogging;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.volvouptime.models.EngineHourReadingModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neeraj on 27/12/18.
 */
public class SaveEngineReading extends AsyncTask<Void, Void, Void> {
    // Define Context for this class
    private Context mContext;
    // Define ProgressDialog for this class
    public static ProgressDialog mProgressDialog;

    // Define RestIntraction for this class
    private RestIntraction restIntraction;
    String response;

    String  current_data,Reg_no,requiredTime,prevReadings;

    List<EngineHourReadingModel> engineHourReadingModels = new ArrayList<>();
    public EngineReadingSaveListener engineReadingSaveListener;


    public EngineReadingSaveListener getEngineReadingSaveListener() {
        return engineReadingSaveListener;
    }

    public void setEngineReadingSaveListener(EngineReadingSaveListener engineReadingSaveListener) {
        this.engineReadingSaveListener = engineReadingSaveListener;
    }

    public SaveEngineReading(Context mContext, String current_data, String Reg_no,String requiredTime,String prevValues) {
        this.mContext = mContext;
        this.Reg_no = Reg_no;
        this.current_data = current_data;
        this.requiredTime=requiredTime;
        this.prevReadings=prevValues;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.please_wait),
                false);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            final VECVPreferences vecvPreferences = new VECVPreferences(mContext);
            restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + "" + ApiUrls.SAVEUTILIZATION);
            restIntraction.AddParam("Token", "teramatrix");
            restIntraction.AddParam("IncurrentMonthUtilizationData", current_data);
            restIntraction.AddParam("RegistrationNo",Reg_no);
            restIntraction.AddParam("PreviousMonthUtilization",prevReadings);
            restIntraction.AddParam("UtilizationTime",requiredTime);
            restIntraction.toString();
            restIntraction.Execute(1);

            // get response from the service
            response = restIntraction.getResponse();
            if (ApplicationConstant.IS_LOG_SHOWN) {
                String log_message = "Engine Utilization Save Api Response:"
                        + response;
                CompleteLogging.logAcceptTicket(mContext, log_message);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            if (response != null) {
                Log.v("SaveEngineHoursResponse", response);
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("Status") &&
                        jsonObject.getString("Status").equalsIgnoreCase("1")) {
                    //api response is correct get the list here and save into db
                   //data saved successfully
                    if (engineReadingSaveListener!=null){
                        engineReadingSaveListener.engineReadingSaved();
                    }
                    }
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public interface EngineReadingSaveListener {
        void engineReadingSaved();
    }


}
