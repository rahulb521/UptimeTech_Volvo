package com.teramatrix.vos.utils;

import java.io.File;
import java.util.ArrayList;


/**
 * 
 * @author Gaurav.Mangal
 * 
 * This class is used for fetch all the file name inside the folder(LocationLogs,ErrorLogs,AppLogs) 
 * showing in list view data.
 *
 */
public class LocationLogFunction 
{
	/**
	 *Getting the name of the file inside folder
	 * @param DirectoryPath
	 * @return
	 */
	public static ArrayList<String> GetFiles(String DirectoryPath) 
    {
		//create object ArrayList class MyFiles
		ArrayList<String> MyFiles = new ArrayList<String>();
		//create file object
        File f = new File(DirectoryPath);
        //f.mkdirs(); 
        
        //check file object exists or not
        if(f.exists())
        {
        	
        	//fetch file using list Files method in the file object
        File[] files = f.listFiles();
        // get the length of the file count
        if (files!= null && files.length == 0)
            return null; 
        else { 
        	//get one or more files inside folder 
            for (int i=0; i<files.length; i++)  {
            	//fetch name of file ending with .txt
                if (files[i].getName().endsWith(".txt"))
                {
                	//get name of file using subString
                    String fileName = files[i].getName().substring(0, files[i].getName().lastIndexOf(".")); 
                    //add file name with MyFiles object
                    MyFiles.add(fileName);
                } 
            } 
        } 
 
        }
        return MyFiles;
    }

}
