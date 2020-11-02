package com.teramatrix.vos.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.teramatrix.vos.R;
import com.teramatrix.vos.model.DeclineReasonModel;

/**
 * 
 * @author Gaurav.Mangal
 * 
 *         This DeclineReasonAdapter is used for Decline a ticket reason in the
 *         list with check box selection.
 * 
 */
public class DeclineReasonAdapter extends BaseAdapter {
	// Defining List for this class
	private List<DeclineReasonModel> items;

	// Defining Context for this class
	private Context context;

	// Defining int variable for this class
	private int row;

	// Defining TypeFace font component that will be used in this adapter
	Typeface centuryGothic;
	Typeface verdana;

	// Defining TextView component that will be used in this adapter
	TextView tv_id_details, tv_ticket_details;

	// Defining int variable for this class
	private boolean[] checkedBox;

	// Defining String variable for this class
	private String previous_selected_reasons_positions;
	private String[] previous_selected_reasons_positions_array;

	private ListView listView;

	EditText other_reason;
	String other_reason_text;

	/**
	 * 
	 * @param context
	 * @param resource
	 * @param items
	 * @param previous_selected_reasons_positions
	 * 
	 *            Pass all the @params in constructor, as it it required to show
	 *            data in a list.
	 */
	public DeclineReasonAdapter(Context context, int resource,
			List<DeclineReasonModel> items,
			String previous_selected_reasons_positions) {
		this.items = items;
		this.context = context;
		row = resource;
		checkedBox = new boolean[items.size()];
		this.previous_selected_reasons_positions = previous_selected_reasons_positions;

		if (previous_selected_reasons_positions != null
				&& previous_selected_reasons_positions.length() > 0)
			previous_selected_reasons_positions_array = previous_selected_reasons_positions
					.split(",");

		if (previous_selected_reasons_positions_array != null
				&& previous_selected_reasons_positions_array.length > 0) {

			for (int p = 0; p < previous_selected_reasons_positions_array.length; p++) {
				int pos = Integer
						.parseInt(previous_selected_reasons_positions_array[p]);
				checkedBox[pos] = true;
			}
		}
	}

	public DeclineReasonAdapter(Context context, int resource,
			List<DeclineReasonModel> items,
			String previous_selected_reasons_positions, ListView listView) {
		this.items = items;
		this.context = context;
		row = resource;
		checkedBox = new boolean[items.size()];
		this.previous_selected_reasons_positions = previous_selected_reasons_positions;
		this.listView = listView;

		if (previous_selected_reasons_positions != null
				&& previous_selected_reasons_positions.length() > 0)
			previous_selected_reasons_positions_array = previous_selected_reasons_positions
					.split(",");

		if (previous_selected_reasons_positions_array != null
				&& previous_selected_reasons_positions_array.length > 0) {

			for (int p = 0; p < previous_selected_reasons_positions_array.length; p++) {
				int pos = Integer
						.parseInt(previous_selected_reasons_positions_array[p]);
				checkedBox[pos] = true;
			}
		}
	}

	public DeclineReasonAdapter(Context context, int resource,
			List<DeclineReasonModel> items,
			String previous_selected_reasons_positions, ListView listView,
			String other_reason_text) {
		this.items = items;
		this.context = context;
		row = resource;
		checkedBox = new boolean[items.size()];
		this.previous_selected_reasons_positions = previous_selected_reasons_positions;
		this.listView = listView;
		this.other_reason_text = other_reason_text;

		if (previous_selected_reasons_positions != null
				&& previous_selected_reasons_positions.length() > 0)
			previous_selected_reasons_positions_array = previous_selected_reasons_positions
					.split(",");

		if (previous_selected_reasons_positions_array != null
				&& previous_selected_reasons_positions_array.length > 0) {

			for (int p = 0; p < previous_selected_reasons_positions_array.length; p++) {
				int pos = Integer
						.parseInt(previous_selected_reasons_positions_array[p]);
				checkedBox[pos] = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(row, null);
			// Create an object to access Holder class
			holder = new ViewHolder();
			holder.row = convertView.findViewById(R.id.rl_row_reason);
			holder.checkButton = (CheckBox) convertView
					.findViewById(R.id.value);
			holder.checkButton.setClickable(false);
			holder.reasonValue = (TextView) convertView.findViewById(R.id.text);
			holder.otherReasonView = convertView
					.findViewById(R.id.reason_other);
			holder.ed_other_reason = (EditText) convertView
					.findViewById(R.id.ed_reason_other_value);
			//holder.ed_other_reason.setText("Yaaa "+position);
			holder.ed_other_reason.setTag("Yaaa "+position);
			holder.position = position;

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		DeclineReasonModel estimationCostModel = items.get(position);

		if(estimationCostModel.getReason_name().length()>0)
			holder.reasonValue.setText(estimationCostModel.getReason_name());

		holder.checkButton.setChecked(checkedBox[position]);

		Log.i("Debug","ViewHolder Index:"+holder.position);


		if (position == items.size() - 1 && checkedBox[position]) {

			holder.otherReasonView.setVisibility(View.VISIBLE);
			//other_reason = holder.ed_other_reason;

			if (other_reason_text != null && other_reason_text.length() > 0) {
				holder.ed_other_reason.setText(other_reason_text);
				other_reason = holder.ed_other_reason;
			}


			String viewHolderTag = holder.ed_other_reason.getTag().toString();
			Log.i("Debug","ViewHolder Tag:"+viewHolderTag);

		} else {
			holder.otherReasonView.setVisibility(View.GONE);
		}

		holder.row.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				holder.checkButton.setChecked(!checkedBox[position]);
				checkedBox[position] = !checkedBox[position];

				if (position == items.size() - 1) {

					if (checkedBox[position]) {
						holder.otherReasonView.setVisibility(View.VISIBLE);
						listView.setSelection(items.size() - 1);
						other_reason = holder.ed_other_reason;

						String local_reason = other_reason.getText().toString();
						holder.ed_other_reason.setText(local_reason);

					} else
						holder.otherReasonView.setVisibility(View.GONE);
				}

			}
		});

		return convertView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/**
	 * onclick selection with title using getSelectedReasonsTitle methods.
	 * 
	 * @return
	 */
	public String getSelectedReasonsTitle() {

		String ids = "";
		for (int i = 0; i < checkedBox.length; i++) {
			if (checkedBox[i] && (i != (items.size() - 1))) {
				if (ids.length() > 0)
					ids = ids + "," + items.get(i).getReason_name();
				else
					ids = items.get(i).getReason_name();

			}
		}
		// other_reason
		if (checkedBox[items.size() - 1] && other_reason != null) {

			if (ids.length() > 0)
				ids = ids + "," + other_reason.getText().toString();
			else
				ids = other_reason.getText().toString();

		}

		if (ids.length() > 0)
			return ids;
		else
			return null;
	}

	// method to check if Other reason option is checked and value is filled in
	// other reason edit box
	public boolean isOtherReasonOptionValid() {

		if (items.size() == 0)
			return false;

		if (checkedBox[items.size() - 1]) {

			String ids = "";
			if (other_reason != null) {
				ids = other_reason.getText().toString();
				String local_ids = other_reason.getText().toString();
				Log.i("DeclineReasonAdapter 2","Reason:"+local_ids);
				if (ids.length() > 0)
					return true;
				else
					return false;
			} else
				return true;
		} else
			return true;

	}

	public String getOtherReasonText() {
		String ids = "";
		if (checkedBox[items.size() - 1] && other_reason != null) {

			ids = other_reason.getText().toString();

		}

		if (ids.length() > 0)
			return ids;
		else
			return null;
	}

	/**
	 * on click selection with title with ids using getSelectedReasonsIds
	 * 
	 * @return
	 */
	public String getSelectedReasonsIds() {

		String ids = "";
		for (int i = 0; i < checkedBox.length; i++) {
			if (checkedBox[i]) {
				if (ids.length() > 0)
					ids = ids + "," + items.get(i).getId();
				else
					ids = items.get(i).getId();

			}
		}
		if (ids.length() > 0)
			return ids;
		else
			return null;
	}

	/**
	 * get the position of the check box selection using
	 * getSelectedReasonsPositions
	 * 
	 * @return
	 */
	public String getSelectedReasonsPositions() {

		String positions = "";
		for (int i = 0; i < checkedBox.length; i++) {
			if (checkedBox[i]) {
				if (positions.length() > 0)
					positions = positions + "," + i;
				else
					positions = "" + i;

			}
		}
		if (positions.length() > 0)
			return positions;
		else
			return null;
	}

	// ViewHolder Class for list row data, Here we have defined View those, we
	// are going to use for List row
	class ViewHolder {
		View row;
		CheckBox checkButton;
		TextView reasonValue;
		View otherReasonView;
		EditText ed_other_reason;
		int position;
	}

}
