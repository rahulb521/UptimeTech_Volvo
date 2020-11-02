package com.teramatrix.vos.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.teramatrix.vos.R;
import com.teramatrix.vos.appurl.ApiUrls;
import com.teramatrix.vos.logs.CompleteLogging;
import com.teramatrix.vos.preferences.VECVPreferences;
import com.teramatrix.vos.restapi.RestIntraction;
import com.teramatrix.vos.utils.ApplicationConstant;
import com.teramatrix.vos.volvouptime.custom.DAO;
import com.teramatrix.vos.volvouptime.models.EngineHourReadingModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neeraj on 27/12/18.
 */
public class EngineReading extends AsyncTask<Void, Void, Void> {
    // Define Context for this class
    private Context mContext;
    // Define ProgressDialog for this class
    public static ProgressDialog mProgressDialog;

    // Define RestIntraction for this class
    private RestIntraction restIntraction;
    String response;
    boolean isCurrentMonth;
    String ms;
    List<EngineHourReadingModel> engineHourReadingModels = new ArrayList<>();
    public EngineReadingListListener engineReadingListListener;

    private boolean whenRefrshing;

    public void setEngineReadingListListener(EngineReadingListListener engineReadingListListener) {
        this.engineReadingListListener = engineReadingListListener;
    }

    public EngineReading(Context mContext, boolean isCurrentMonth, String ms, boolean whenRefrshing) {
        this.mContext = mContext;
        this.isCurrentMonth = isCurrentMonth;
        this.ms = ms;
        this.whenRefrshing=whenRefrshing;
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
            restIntraction = new RestIntraction(new VECVPreferences(mContext).getAPIEndPoint_EOS() + "" + ApiUrls.UTILIZATIONDETAILS);
            restIntraction.AddParam("Token", "teramatrix");
            restIntraction.AddParam("InapplicationLicenseKey", vecvPreferences.getLicenseKey());
            restIntraction.AddParam("RequiredDate", ms);
            restIntraction.toString();restIntraction.Execute(1);

            // get response from the service
            response = restIntraction.getResponse();
            if (ApplicationConstant.IS_LOG_SHOWN) {
                String log_message = "Engine Utilization Api Response:"
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
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("Status") &&
                        jsonObject.getString("Status").equalsIgnoreCase("1")) {
                    //api response is correct get the list here and save into db
                    JSONArray jsonArray = jsonObject.getJSONArray("list_UtilizationDetails");
                    if (jsonArray != null) {
                        if (isCurrentMonth) {
                            //clear database
                            DAO.deleteAllRecords(EngineHourReadingModel.class);
                        }
                        for (int i = 0; i < jsonArray.length(); i++
                                ) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            if (jsonObject1 != null) {
                                EngineHourReadingModel engineHourReadingModel = new EngineHourReadingModel();
                                engineHourReadingModel.setChassisNumber(jsonObject1.getString("ChassisNumber"));
                                engineHourReadingModel.setDoorNumber(jsonObject1.getString("DoorNumber"));
                                engineHourReadingModel.setCurrentMonthUtilizationData(jsonObject1.getString("CurrentMonthUtilizationData"));
                                engineHourReadingModel.setPreviousMonthUtilizationData(jsonObject1.getString("PreviousMonthUtiizationData"));
                                engineHourReadingModel.setRegistrationNumber(jsonObject1.getString("RegistrationNumber"));
                                engineHourReadingModel.setModified(false);
                                if (isCurrentMonth) {
                                    engineHourReadingModel.save();
                                }
                                engineHourReadingModels.add(engineHourReadingModel);
                            }

                        }
                        //call list listener here
                        if (engineReadingListListener != null) {
                            engineReadingListListener.engineReadingList(engineHourReadingModels,whenRefrshing);
                        }
                    }
                } else {

                }
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public interface EngineReadingListListener {
        void engineReadingList(List<EngineHourReadingModel> engineHourReadingModels,boolean whenRefreshing);
    }


}
