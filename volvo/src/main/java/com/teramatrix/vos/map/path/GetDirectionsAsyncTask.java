package com.teramatrix.vos.map.path;

import java.util.ArrayList;
import java.util.Map;

import org.w3c.dom.Document;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.teramatrix.vos.EosApplication;
import com.teramatrix.vos.MapViewTimerActivitySeekBar;
import com.teramatrix.vos.R;
import com.teramatrix.vos.utils.UtilityFunction;
/**
 * This class performs background operation for GetDirectionsAsyncTask for fetch the direction on map
 * @author Gaurav Mangal
 *
 */
public class GetDirectionsAsyncTask extends AsyncTask<Map<String, String>, Object, ArrayList<LatLng>>
{
    public static final String USER_CURRENT_LAT = "user_current_lat";
    public static final String USER_CURRENT_LONG = "user_current_long";
    public static final String DESTINATION_LAT = "destination_lat";
    public static final String DESTINATION_LONG = "destination_long";
    public static final String DIRECTIONS_MODE = "directions_mode";
    private MapViewTimerActivitySeekBar seek_activity;
    private Exception exception;
 // Defining ProgressDialog for this class
    private ProgressDialog progressDialog;
    /**
	 * @param context
	 * 
	 * Pass all the @params in constructor, as it it required for GetDirectionsAsyncTask show dialog
	 */

    public GetDirectionsAsyncTask(MapViewTimerActivitySeekBar seek_activity)
    {
        super();
        this.seek_activity = seek_activity;
    }
    
    /* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute(), Method is used to perform UI operation
	 * before starting background Service
	 */
    public void onPreExecute()
    {
        progressDialog = new ProgressDialog(seek_activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(seek_activity.getResources().getString(R.string.getting_direction));
        progressDialog.show();
    }
    /* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object), Method is used to perform
	 * UI operation after background task will be finishing.
	 */
    @Override
    public void onPostExecute(ArrayList result)
    {
    	try
    	{
    		    progressDialog.dismiss();
    	        if (exception == null)
    	        {
    	        	seek_activity.handleGetDirectionsResult(result);
    	        }
    	        else
    	        {
    	            processException();
    	        }
    	}
    	catch(Exception e)
    	{
    		//Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);	
    		UtilityFunction.saveErrorLog(seek_activity, e);
    	}
    }
    /* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[]), Method is used to perform
	 * Background Task.
	 */
    @Override
    protected ArrayList<LatLng> doInBackground(Map<String, String>... params)
    {
        Map<String, String> paramMap = params[0];
        try
        {
            LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)) , Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
            LatLng toPosition = new LatLng(Double.valueOf(paramMap.get(DESTINATION_LAT)) , Double.valueOf(paramMap.get(DESTINATION_LONG)));
            GMapV2Direction md = new GMapV2Direction(seek_activity);
            Document doc = md.getDocument(fromPosition, toPosition, paramMap.get(DIRECTIONS_MODE));
            ArrayList<LatLng> directionPoints = md.getDirection(doc);
            return directionPoints;
        }
        catch (Exception e)
        {
            exception = e;
         
          //Google Analytic -Tracking Exception 
			EosApplication.getInstance().trackException(e);
            UtilityFunction.saveErrorLog(seek_activity, e);
            return null;
        }
    }
    /* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object), Method is used to perform
	 *process exception handling with show toast.
	 */
    private void processException()
    {
        Toast.makeText(seek_activity, seek_activity.getResources().getString(R.string.retriving_data), 3000).show();
    }
}