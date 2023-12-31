package com.teramatrix.vos.restapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.gson.Gson;
import com.teramatrix.vos.model.ErrorResponseModel;

import javax.net.ssl.HostnameVerifier;

public class RestIntraction {

	String TAG = this.getClass().getSimpleName();
	// Defining arraylist to get parameters and headers
	private ArrayList<NameValuePair> params;
	private ArrayList<NameValuePair> headers;
	private JSONObject jsonObject;

	private  static HttpClient mHhttpclient = new DefaultHttpClient();

	// Defining string to get url
	private String url;

	// Defining string to get message, response and responseCode
	private int responseCode;
	private String message;
	private String response;

	/**
	 * To get Response from server
	 * 
	 * @return response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * To get Error Message
	 * 
	 * @return error message
	 */
	public String getErrorMessage() {
		return message;
	}

	/**
	 * To get Response code from server
	 * 
	 * @return responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * Overloaded Constructor to get rest api url
	 * 
	 * @param url
	 *            of the server
	 */
	public RestIntraction(String url) {
		this.url = url;
		params = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
		jsonObject = new JSONObject();
	}

	/**
	 * To add parameters to REST Request
	 * 
	 * @param name
	 *            of the parameter
	 * @param value
	 *            of the parameter
	 */
	public void AddParam(String name, String value) {
		params.add(new BasicNameValuePair(name, value));
	}

	public void AddJson(String name, JSONObject value) throws JSONException {
		jsonObject = value;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return params.toString();

	}


	public void AddParamArray(String name, ArrayList<String> arrayOfValues) {
		for (String value : arrayOfValues) {
			params.add(new BasicNameValuePair(name, value));
		}
	}

	/**
	 * To add headers to the REST Request
	 * 
	 * @param name
	 * @param value
	 */
	public void AddHeader(String name, String value) {
		headers.add(new BasicNameValuePair(name, value));
	}

	/**
	 * To execute the REST Request
	 * 
	 * @param method
	 *            (GET or POST or PUT)
	 * @throws Exception
	 */
	public void Execute(int method) throws Exception {
		// int method 0 for get, 1 for POST, 2 for put
		switch (method) {
		case 0: {
			// add parameters
			String combinedParams = "";
			if (!params.isEmpty()) {
				combinedParams += "?";
				for (NameValuePair p : params) {
					String paramString = p.getName() + "="
							+ URLEncoder.encode(p.getValue(), "UTF-8");
					if (combinedParams.length() > 1) {
						combinedParams += "&" + paramString;
					} else {
						combinedParams += paramString;
					}
				}
			}

			HttpGet request = new HttpGet(url + combinedParams);
			// add headers
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}
			executeRequest(request, url);
			break;

		}
		case 1: {
			System.out.println("Case 1");
			HttpPost request = new HttpPost(url);

			// add headers
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
				Log.e("APIHEADER",h.getName()+":"+h.getValue());

			}
			for (NameValuePair p:params){
				Log.e("APIPARAM",p.getName()+":"+p.getValue());

			}
			if (!params.isEmpty()) {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			executeRequest(request, url);
			break;
		}
		case 2: {
			// add parameters
			HttpPut request = new HttpPut(url);

			// add headers
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}

			if (!params.isEmpty()) {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

			}

			executeRequest(request, url);
			break;
		}
			case 3:
			{
				// add obj
				HttpPost request = new HttpPost(url);
				request.setHeader("Content-type", "application/json");
				request.setHeader("Accept", "application/json");
				//request.setHeader("Authorization", "Bearer " + accesstoken);
				String utf=convertStringToUTF8(jsonObject.toString().trim());
				StringEntity se = new StringEntity(utf);
				request.setEntity(se);
				executeRequest(request, url);
				break;
			}

		}
	}
	// convert internal Java String format to UTF-8
	public static String convertStringToUTF8(String s) {
		String out = null;
		try {
			out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
		} catch (Exception e) {
			return null;
		}
		return out;
	}
	/**
	 * To execute the REST request
	 * 
	 * @param request
	 * @param url
	 */
	private void executeRequest(HttpUriRequest request, String url)throws Exception {
		Log.e("RESTUrl", url);


		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = 60000*2;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 60000*2;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		// Initializing HttpClient to execute request



		//===============================new code for certificate issue


		HttpClient client = new DefaultHttpClient(httpParameters);

		Log.e(TAG, "resturl111: " );

		// Defining HttpResponse to get response of request
		HttpResponse httpResponse;

		try {
			httpResponse = client.execute(request);
			responseCode = httpResponse.getStatusLine().getStatusCode();
			message = httpResponse.getStatusLine().getReasonPhrase();

			HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {

				InputStream instream = entity.getContent();
				response = convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				Log.e(TAG, "executeRequest:response "+response);

				instream.close();
			}
		} catch (ConnectTimeoutException connectTimeoutException) {
			client.getConnectionManager().shutdown();
			ErrorResponseModel errorResponseModel=new ErrorResponseModel();
			errorResponseModel.setMessage("Timeout Slow Connection");
			errorResponseModel.setStatus("0");
			response = new Gson().toJson(errorResponseModel);

			Log.e(TAG, "executeRequest:333 "+connectTimeoutException.getMessage() );

			throw connectTimeoutException;

		} catch (ClientProtocolException e) {
			client.getConnectionManager().shutdown();
			ErrorResponseModel errorResponseModel=new ErrorResponseModel();
			errorResponseModel.setMessage("Protocol Error");
			errorResponseModel.setStatus("0");
			response = new Gson().toJson(errorResponseModel);
			Log.e(TAG, "executeRequest222: "+e.getMessage() );

			throw e;
		} catch (IOException e) {
			client.getConnectionManager().shutdown();
			ErrorResponseModel errorResponseModel=new ErrorResponseModel();
			errorResponseModel.setMessage("Connection Error");
			errorResponseModel.setStatus("0");
			response = new Gson().toJson(errorResponseModel);
			Log.e(TAG,e.fillInStackTrace()+" executeRequest111: "+e.getMessage() );

			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "executeRequest: "+e.getMessage() );
		}
	}

	/**
	 * To convert input stream to String
	 * 
	 * @param is
	 *            Input Stream
	 * @return string
	 */
	private static String convertStreamToString(InputStream is)throws Exception  {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			throw e;
			
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				
				throw e;
			}
		}
		return sb.toString();
	}



}
