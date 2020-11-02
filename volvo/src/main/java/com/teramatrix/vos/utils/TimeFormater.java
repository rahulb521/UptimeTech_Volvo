package com.teramatrix.vos.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/*TimeFormater class for formating  one form of time to another form*/
public class TimeFormater {

	/* convert seconds to HH:mm:ss formate */
	/**
	 * 
	 * @param seconds
	 * @return
	 */
	public static String convertSecondsToHMmSs(long seconds) {
		//convert second
		long s = seconds % 60;
		//convert minute
		long m = (seconds / 60) % 60;
		//convert hour
		long h = (seconds / (60 * 60)) % 24;

		//define hours string variable
		String hours;
		//define minutes string variable
		String minutes;
		//define second string variable
		String second;

		//check hours value
		if (h < 10)
			hours = "0" + h;
		else
			hours = "" + h;
		//check minute value
		if (m < 10)
			minutes = "0" + m;
		else
			minutes = "" + m;
		//check second value
		if (s < 10)
			second = "0" + s;
		else
			second = "" + s;

		return hours + ":" + minutes + ":" + second;

	}

	/* convert minutes to HH:mm:ss formate */
	/**
	 * 
	 * @param min
	 * @return
	 */
	public static String convertMinutesToHMmSs(long min) {
		//convert second
		long seconds = min * 60;
		long s = seconds % 60;
		//convert minute
		long m = (seconds / 60) % 60;
		//convert hour
		long h = (seconds / (60 * 60)) % 24;
		//define hours string variable
		String hours;
		//define minutes string variable
		String minutes;
		//define second string variable
		String second;
		//check hours value
		if (h < 10)
			hours = "0" + h;
		else
			hours = "" + h;

		//check minutes value
		if (m < 10)
			minutes = "0" + m;
		else
			minutes = "" + m;

		//check second value
		if (s < 10)
			second = "0" + s;
		else
			second = "" + s;

		return hours + ":" + minutes + ":" + second;

	}

	/* convert utc Time to dd MMM yyyy HH:mm:ss formate */
	/**
	 * 
	 * @param utcTime
	 * @return
	 */
	public static String getTimeInCurrentTimeZone(String utcTime) {

		if (utcTime == null)
			return "null";

		long currentzone_time = Long.parseLong(utcTime)
				+ TimeZone.getDefault().getRawOffset();
		// Create an instance of the SimpleDateFormat
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		// return date in the specified format
		return sdf.format(new Date(currentzone_time));
	}


	public static String getTimeInIST(String timeString,String curretnTimeFormat,String requiredTimeFormat)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(curretnTimeFormat);
		try {
			//parse current date String
			Date date = sdf.parse(timeString);

			//add 5:30 hours
			long currentzone_time = date.getTime()
					+ TimeZone.getDefault().getRawOffset();

			//parse new time with new time format
			sdf = new SimpleDateFormat(requiredTimeFormat);
			return sdf.format(new Date(currentzone_time));

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}


	public static String convertMillisecondsToDateFormat(long current_time_milliseconds) {

		// Create an instance of the SimpleDateFormat
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		// return date in the specified format
		return sdf.format(new Date(current_time_milliseconds));
	}

	public static String convertMillisecondsToDateFormat(long current_time_milliseconds,String newformat) {

		// Create an instance of the SimpleDateFormat
		SimpleDateFormat sdf = new SimpleDateFormat(newformat);
		// return date in the specified format
		return sdf.format(new Date(current_time_milliseconds));
	}

	

	/* convert dd MMM yyyy HH:mm:ss formated time to milliseconds */
	/**
	 * 
	 * @param lastModifiedTime
	 * @return
	 * @throws Exception
	 */
	public static long getModifiedTimeInMillisecond(String lastModifiedTime)
			throws Exception {

		// 02-JAN-2015 02:15:25
		String definedFormate = "dd MMM yyyy HH:mm:ss";
		// Create an instance of the SimpleDateFormat
		SimpleDateFormat sdf = new SimpleDateFormat(definedFormate);
		try {
			Date formatedDate = sdf.parse(lastModifiedTime);
			// return date in the specified format
			return formatedDate.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	/**
	 * 
	 * @param datetimeString
	 * @return
	 * @throws Exception
	 * 
	 * This function for convert time string in current time zone
	 */
	public static String getTimeStringInCurrentTimeZone(String datetimeString)
			throws Exception {
		String timeInMilli = getModifiedTimeInMillisecond(datetimeString) + "";

		String dateTimeStringInCurrentTimeZone = getTimeInCurrentTimeZone(timeInMilli);
		// return date in the specified format
		return dateTimeStringInCurrentTimeZone;
	}

	/**
	 * Get Today Date in dd-MMM-yyyy formate
	 * @return String
	 */
	public static String getTodayDateInCurrentTimeZone() {
		// Create an instance of the SimpleDateFormat
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		// return date in the specified format
		return sdf.format(new Date());
	}


	/**
	 * Convert a date string format
	 * @param dateString - date in string
	 * @param currentFormat - format of date string
	 * @param newFormat - new format of date string
	 * @return String
	 */
	public static String convertDateFormate(String dateString,String currentFormat,String newFormat)
	{
		if(dateString==null || dateString.length()==0)
			return "N/A";

		SimpleDateFormat originalFormat = new SimpleDateFormat(currentFormat);
		originalFormat.setLenient(false) ;
		SimpleDateFormat targetFormat = new SimpleDateFormat(newFormat);

		Date date = null;
		try {
			date = originalFormat.parse(dateString);
			return targetFormat.format(date);  // 20120821
		} catch (ParseException e) {


			try
			{
				date =  targetFormat.parse(dateString);
				return targetFormat.format(date);

			}catch (ParseException ex)
			{
				ex.printStackTrace();
			}
			//e.printStackTrace();
		}
		return null;
	}


   /**
    * 
    * @param minutes
    * @return
    * 
    * This method convert minutes in the formated estimation time shown.
    */
	public static String getFormatedEstimatedTime(int minutes) {

		//convert hours
		int hours = minutes / 60;
		//convert min
		int min = minutes % 60;

		//define string variable
		String timeString = "";
		//check hours value
		if (hours <= 1) {
			//check minute value
			if (min == 0) {
				if (hours < 1) {
					//concat time hours value in the timeString
					// OO:OO hr
					timeString = "00" + ":" + "00" + " hr";
				} else if (hours == 1) {
					// 1:00 hr
					timeString = hours + ":" + "00" + " hr";
				}
			} else {
				if (hours < 1) {
					// 00:30 hr
					timeString = "00" + ":" + min + " hr";
				} else if (hours == 1) {
					// 1:30 hrs
					timeString = hours + ":" + min + " hrs";
				}
			}

		} else if (hours > 1) {
			if (min == 0) {
				// 2:00 hrs
				timeString = hours + ":" + "00" + " hrs";
			} else {
				// 2:30 hrs
				timeString = hours + ":" + min + " hrs";
			}

		}

		return timeString;
	}

	/**
	 * Parse and return numeric value of a part from a date string.
	 * @param dateString - date in string
	 * @param currentFormat - current format of date string
	 * @param partRequired - part of date string which need to be parsed i.e 'year' or 'month' or 'day' etc
	 * @return int value of part
	 */

	public static int getPartOfDateString(String dateString,String currentFormat,String partRequired)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(currentFormat);
		try {

			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(dateString));


			if(partRequired.equalsIgnoreCase("day"))
			return  c.get(Calendar.DAY_OF_MONTH);
			if(partRequired.equalsIgnoreCase("month"))
				return  c.get(Calendar.MONTH);
			if(partRequired.equalsIgnoreCase("year"))
				return  c.get(Calendar.YEAR);
			if(partRequired.equalsIgnoreCase("hour"))
				return  c.get(Calendar.HOUR_OF_DAY);
			if(partRequired.equalsIgnoreCase("minute"))
				return  c.get(Calendar.MINUTE);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/*
	*  Compare two date string. find if  dateString1 if older than dateString2
	*  dateString1 - first date in string
	*  dateString2 - second date in string
	*  dateFormat - current format of dates
	* */
	public static int compareDateString(String dateString1,String dateString2, String dateFormat)
	{
		//return 0 -> if dateString1 == dateString2
		//return -1 -> if dateString1 < dateString2
		//return 1 -> if dateString1 > dateString2

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		try {

			Date  date1 = sdf.parse(dateString1);
			Date  date2 = sdf.parse(dateString2);
			int result = date1.compareTo(date2);
			return result;

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static Date getDateFromString(String dateString,String currentFormat)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(currentFormat);
		try {
			Date  date1 = sdf.parse(dateString);
			return date1;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
