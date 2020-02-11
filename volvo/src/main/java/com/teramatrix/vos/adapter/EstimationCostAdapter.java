package com.teramatrix.vos.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.teramatrix.vos.R;
import com.teramatrix.vos.model.EstimationCostModel;

/**
 * 
 * @author Gaurav.Mangal
 * 
 *         This EstimationCostAdapter class adapter used for showing cost in the
 *         list view with radio button.
 * 
 */

public class EstimationCostAdapter extends BaseAdapter {
	// Defining List for this class
	private List<EstimationCostModel> items;

	// Defining Context for this class
	private Context context;

	// Defining int variable for this class
	private int row;

	// Defining TypeFace font component that will be used in this activity
	Typeface centuryGothic;
	Typeface verdana;

	// Defining TextView component that will be used in this adapter
	TextView tv_id_details, tv_ticket_details;

	// Defining int variable for this class
	private boolean[] checkedRow;

	// Defining RadioButton Component for this class
	private RadioButton previousSelectedRadio;

	// Defining int variable for this class
	private int previousCheckedPosition = -1;

	// Defining String variable for this class
	private String selected_estimation_cost_id;

	ListView list_estimationcost;
	EditText editcostView;

	/**
	 * 
	 * @param context
	 * @param resource
	 * @param items
	 * @param selected_estimation_cost_id
	 * 
	 *            Pass all the @params in constructor, as it it required to show
	 *            data in a list.
	 * 
	 */
	public EstimationCostAdapter(Context context, int resource,
			List<EstimationCostModel> items,
			String selected_estimation_cost_id, ListView list_estimationcost) {
		this.items = items;
		this.context = context;
		row = resource;
		// last one index for other option
		checkedRow = new boolean[items.size() + 1];
		this.selected_estimation_cost_id = selected_estimation_cost_id;

		for (int i = 0; i < items.size(); i++) {
			String id = items.get(i).id;
			if (id.equalsIgnoreCase(selected_estimation_cost_id)) {
				checkedRow[i] = true;
				previousCheckedPosition = i;
			}
		}

		this.list_estimationcost = list_estimationcost;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// one more count for Other Option
		return items.size() + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 * 
	 * Method is responsible to estimation cost list during scrolling.
	 */

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			// Here list row layout will initialize.
			convertView = LayoutInflater.from(context).inflate(row, null);

			// Create an object to access Holder class

			holder = new ViewHolder();
			holder.row = convertView.findViewById(R.id.rel_row);
			holder.radioButton = (RadioButton) convertView
					.findViewById(R.id.radio_cost);
			holder.radioButton.setClickable(false);
			holder.costValue = (TextView) convertView
					.findViewById(R.id.txt_cost_value);
			holder.ed_estimated_cost = (EditText) convertView
					.findViewById(R.id.ed_estimated_cost);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// assign a position id to particular view

		if (position == items.size()) {
			holder.costValue.setText("Other Cost");
		} else {
			EstimationCostModel estimationCostModel = items.get(position);
			holder.costValue.setText(estimationCostModel.getCost_range());
		}

		if (checkedRow[position])
			// set a check box for the visible items in the list.
			holder.radioButton.setChecked(true);
		else
			// set a check box value in list false
			holder.radioButton.setChecked(false);

		if (previousCheckedPosition == position)
			previousSelectedRadio = holder.radioButton;

		if (position == checkedRow.length - 1 && checkedRow[position]) {
			holder.ed_estimated_cost.setVisibility(View.VISIBLE);
			editcostView = holder.ed_estimated_cost;
		} else {
			holder.ed_estimated_cost.setVisibility(View.GONE);
			//editcostView = null;
		}
		// click on particular row in the list.

		holder.row.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (previousSelectedRadio != null)
					previousSelectedRadio.setChecked(false);

				if (previousSelectedRadio != null)
					previousSelectedRadio.setChecked(false);

				holder.radioButton.setChecked(true);
				previousSelectedRadio = holder.radioButton;

				if (previousCheckedPosition > -1)
					checkedRow[previousCheckedPosition] = false;

				previousCheckedPosition = position;
				checkedRow[position] = true;

				// make edittext for inputing cost visible
				if (position == checkedRow.length - 1 && checkedRow[position]) {
					holder.ed_estimated_cost.setVisibility(View.VISIBLE);
					list_estimationcost.setSelection(position);
					editcostView = holder.ed_estimated_cost;
				} else {
					holder.ed_estimated_cost.setVisibility(View.GONE);
					if (editcostView != null)
						editcostView.setVisibility(View.GONE);
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
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getSelectedPosition() {
		return previousCheckedPosition;
	}
	public String getOthersValue() {
		if(editcostView != null)
			return editcostView.getText().toString();
		return "";
	}

	// ViewHolder Class for list row data, Here we have defined View those, we
	// are going to use for List row

	class ViewHolder {
		View row;
		RadioButton radioButton;
		TextView costValue;
		EditText ed_estimated_cost;
	}
}
