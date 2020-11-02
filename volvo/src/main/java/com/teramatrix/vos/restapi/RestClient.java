package com.teramatrix.vos.restapi;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * RestClient class is used to perform data from json data addFormPart and getResponse from server. 
 * @author Gaurav.Mangal
 *
 */
public class RestClient {

	private String url;

	private String delimiter = "--";
	private String boundary =  "SwA"+Long.toString(System.currentTimeMillis())+"SwA";
	private HttpURLConnection con;
	private OutputStream os;

	/**
	 * This method handle connection for multi-part such as images, text, etc. 
	 * @throws Exception
	 */
	public void connectForMultipart() throws Exception {
		con = (HttpURLConnection) ( new URL(url)).openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		con.connect();
		os = con.getOutputStream();
	}

	/**
	 * This method create (key, value) pair to the data pass through it so that it can be handle at the time of post request by rest api
	 * @param paramName
	 * @param value
	 * @throws Exception
	 */
	
	public void addFormPart(String paramName, String value) throws Exception {
		writeParamData(paramName, value);
	}

	/**
	 * This method handle putting data in rest api 
	 * @param paramName 
	 * @param value
	 * @throws Exception
	 */
	private void writeParamData(String paramName, String value) throws Exception {
		os.write( (delimiter + boundary + "\r\n").getBytes());
		os.write( "Content-Type: text/plain\r\n".getBytes());
		os.write( ("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());;
		os.write( ("\r\n" + value + "\r\n").getBytes());
	}

	/**
	 * RestClient constructor to initialize a URL to post the object created by this class
	 * @param url
	 */
	public RestClient(String url)
	{
		this.url = url;
	}

	/**
	 * This method will hold image path for the rest api
	 * @param paramName
	 * @param fileName
	 * @param data
	 * @throws Exception
	 */
	public void addFilePart(String paramName, String fileName, byte[] data) throws Exception {
	    os.write( (delimiter + boundary + "\r\n").getBytes());
	    os.write( ("Content-Disposition: form-data; name=\"" + paramName +  "\"; filename=\"" + fileName + "\"\r\n"  ).getBytes());
	    os.write( ("Content-Type: application/octet-stream\r\n"  ).getBytes());
	    os.write( ("Content-Transfer-Encoding: binary\r\n"  ).getBytes());
	    os.write("\r\n".getBytes());
	    os.write(data);
	    os.write("\r\n".getBytes());
	}
	
	/**
	 * This method will finish the multi part connection
	 * @throws Exception
	 */
	public void finishMultipart() throws Exception {
		os.write( (delimiter + boundary+ delimiter + "\r\n").getBytes());
	}
	
	/**
	 * This method will get response from the rest api
	 * @return
	 * @throws Exception
	 */
	public String getResponse() throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		try {
			InputStream is = con.getInputStream();
			byte[] b1 = new byte[1024];
			

			while ( is.read(b1) != -1)
				buffer.append(new String(b1));

			con.disconnect();

			return buffer.toString();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println(e);
			
			throw e;
//			return e.toString();
			
			
		}	
		
		//return bu
	}
}