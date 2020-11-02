package com.teramatrix.vos.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
/**
 * 
 * @author Gaurav.Mangal
 *
 */
/*Extended EditText class for forcing keyboard to have no Enter button 
 * */

public class ActionEditText extends EditText {

	/**
	 * 
	 * @param context
	 */
	public ActionEditText(Context context) {
		super(context);
	}
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public ActionEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ActionEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
/*
 * (non-Javadoc)
 * @see android.widget.TextView#onCreateInputConnection(android.view.inputmethod.EditorInfo)
 */
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		InputConnection conn = super.onCreateInputConnection(outAttrs);
		
		//dismiss using enter with the keyboard IME_FLAG_NO_ENTER_ACTION.
		outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
		return conn;
	}
}
