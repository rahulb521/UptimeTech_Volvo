package com.teramatrix.vos.checkinternet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * CheckInternetConnection Class is used to check Internet connection
 * @author Gaurav Mangal
 */
public class CheckInternetConnection 
{
	private Context context;
	
	public CheckInternetConnection(Context mContext){
		context = mContext;
	}
	
	/**
	 * @return
	 * isConnectedToInternet?(), is used to check Internet Connectivity by using Connection Manager.
	 * it will return true if it is connected and false if not connected
	 */
	public boolean isConnectedToInternet()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null){
            	for (int i = 0; i < info.length; i++){
            		if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
            	}
            }
        }
		return false;
	}
}