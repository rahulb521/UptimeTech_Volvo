package com.teramatrix.vos.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teramatrix.vos.JobStatusDetailActivity;
import com.teramatrix.vos.MapViewTimerActivitySeekBar;
import com.teramatrix.vos.R;
import com.teramatrix.vos.model.Ticket;

/**
 * 
 * @author Gaurav.Mangal
 * 
 *         This MyTicketListAdapter adapter is used for showing ticket in the
 *         list and also reflected with ticket status bar in the particular row
 */
public class MyTicketListAdapter extends BaseAdapter {

	// Defining List for this class
	private List<Ticket> items;

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
	int timeZoneOffset;

	/**
	 * 
	 * @param context
	 * @param resource
	 * @param items
	 */
	public MyTicketListAdapter(Context context, int resource, List<Ticket> items) {
		this.items = items;
		this.context = context;
		row = resource;

		timeZoneOffset = TimeZone.getDefault().getRawOffset();
	}

	/**
	 * 
	 * @param items
	 */
	public void setNewData(List<Ticket> items) {
		this.items = items;
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
	 * 
	 * Method is responsible to estimation cost list during scrolling.
	 */

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View v = convertView;
		if (v == null) {

			// Create an object to access Holder class

			holder = new ViewHolder();
			v = LayoutInflater.from(context).inflate(row, null);

			View row = v.findViewById(R.id.ticket_row);
			// initialize TypeFace Font view component that will be used in this
			// adapter
			centuryGothic = Typeface.createFromAsset(context.getAssets(),
					"gothic_0.TTF");
			verdana = Typeface.createFromAsset(context.getAssets(),
					"Verdana.ttf");

			holder.ticket_row = row;


			holder.ticket_number = (TextView) v
					.findViewById(R.id.tv_ticket_number);
			holder.ticket_time_status = (TextView) v
					.findViewById(R.id.tv_id_details);
			holder.ticket_detail = (TextView) v
					.findViewById(R.id.tv_ticket_details);

			// Initialize all classes and views those we are going to use in
			// this font value
			//holder.ticket_time_status.setTypeface(centuryGothic);
			//holder.ticket_detail.setTypeface(verdana);

			holder.colr_bar = (RelativeLayout) v
					.findViewById(R.id.row_color_line);

			holder.img_eye = (ImageView) v.findViewById(R.id.img_eye);

			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}

		final Ticket ticket = items.get(position);

		/*
		 * if ticket status 2 shows as blue when the ticket is accepted.
		 */
		if (ticket.TicketStatus.equalsIgnoreCase("2")) {

			// set the background color in color bar top left row in the list
			holder.colr_bar.setBackgroundColor(context.getResources().getColor(
					R.color.volvo_blue));
		} else {

			// check the sla time miss or not
			boolean isSlaMissed = isSlaMissed(ticket);
			if (isSlaMissed) {
				// set the background color in color bar top left row in the
				// list
				holder.colr_bar.setBackgroundColor(context.getResources()
						.getColor(R.color.volvo_red));
			} else {
				// set the background color in color bar top left row in the
				// list
				holder.colr_bar.setBackgroundColor(context.getResources()
						.getColor(R.color.volvo_green));
			}
		}

		// Show special indicator icon on ticket(Open Trip) to distinguish it
		// from other ticket.
		if (ticket.TicketStatus.equalsIgnoreCase("4") || (ticket.TicketStatus.equalsIgnoreCase("5") && ticket.IsTripEnd.equalsIgnoreCase("false"))) {
			holder.img_eye.setVisibility(View.VISIBLE);
		} else {
			holder.img_eye.setVisibility(View.GONE);
		}

		String ticket_id = items.get(position).Id;

		String TotalTicketLifeCycleTimeSlab = null;
		if (ticket.LastModifiedTimeInCurrentTimeZone == null
				|| ticket.LastModifiedTimeInCurrentTimeZone.length() == 0) {
			TotalTicketLifeCycleTimeSlab = getTimeInCurrentTimeZone(items
					.get(position).LastModifiedTimeInMilliSec);
			ticket.LastModifiedTimeInCurrentTimeZone = TotalTicketLifeCycleTimeSlab;
		} else {
			TotalTicketLifeCycleTimeSlab = items.get(position).LastModifiedTimeInCurrentTimeZone;
		}

		String ticket_description = items.get(position).Description;
		String ticket_sla_meet = ticket.TicketStatusText;

		String[] ticketid = ticket_id.split("/");
		String str_ticket_details = " * "
				+ ticket_sla_meet + " * " + TotalTicketLifeCycleTimeSlab;


		holder.ticket_number.setText("# " + ticketid[0]);
		holder.ticket_time_status.setText(str_ticket_details);
		holder.ticket_detail.setText(ticket_description);

		// when click on particular row check ticket status and moving next
		// activity
		holder.ticket_row.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				String ticketstatus = ticket.TicketStatus;
				String estimatedCostForJobComplition = ticket.EstimatedCostForJobComplition;
				if (ticketstatus != null) {
					Intent in = null;

					// if ticket status 2 i.e ticket is accepted move
					// MapViewTimerActivitySeekBar activity .
					if (ticketstatus.equalsIgnoreCase("2")) {
						// in = new Intent(context, MapViewTimerActivity.class);
						in = new Intent(context,
								MapViewTimerActivitySeekBar.class);
					}else if (ticketstatus.equalsIgnoreCase("9"))
					{
						// This case will occur when user have Started Trip to breakdown location
						// Redirect User to Map screen
						in = new Intent(context,
								MapViewTimerActivitySeekBar.class);
					} else if (ticketstatus.equalsIgnoreCase("3")
							&& (estimatedCostForJobComplition == null || (estimatedCostForJobComplition != null && (estimatedCostForJobComplition
									.equalsIgnoreCase("null") || estimatedCostForJobComplition
									.equalsIgnoreCase(""))))) {
						// This case will occur when user have reached on place
						// (Van reached) but Form(Problem description) is not
						// submitted.
						// Redirect User to Map screen
						in = new Intent(context,
								MapViewTimerActivitySeekBar.class);
					} else if (ticketstatus.equalsIgnoreCase("3")) {
						// This case will occur when user have reached on place
						// (Van reached) and Form(Problem description) is
						// submitted.
						// Redirect User to JobStatusDetailActivity
						in = new Intent(context, JobStatusDetailActivity.class);
					} else if (ticketstatus.equalsIgnoreCase("4")) {
						in = new Intent(context, JobStatusDetailActivity.class);
						in.putExtra("jobcompleted", "true");
					} else if (ticketstatus.equalsIgnoreCase("5") && ticket.IsTripEnd.equalsIgnoreCase("true")) {
						in = new Intent(context, JobStatusDetailActivity.class);
						in.putExtra("jobcompleted", "closed");
					} else if (ticketstatus.equalsIgnoreCase("7")) {
						in = new Intent(context, JobStatusDetailActivity.class);
						in.putExtra("jobcompleted", "closed");
					}else if (ticketstatus.equalsIgnoreCase("8")) {
						in = new Intent(context, JobStatusDetailActivity.class);
						in.putExtra("jobcompleted", "true");
					}else if (ticketstatus.equalsIgnoreCase("5") && ticket.IsTripEnd.equalsIgnoreCase("false")) {
						in = new Intent(context, JobStatusDetailActivity.class);
						in.putExtra("jobcompleted", "true");
					} 

					if (in != null) {
						in.putExtra("ticket", ticket);
						context.startActivity(in);
					}

				}

			}
		});
		return v;
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

	public void doSet() {

	}

	class ViewHolder {
		TextView ticket_number;
		TextView ticket_time_status;
		TextView ticket_detail;
		View ticket_row;
		View colr_bar;
		ImageView img_eye;

	}

	private String getTimeInCurrentTimeZone(String utcTime) {

		if (utcTime == null)
			return "null";

		long currentzone_time = Long.parseLong(utcTime) + timeZoneOffset;
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		return sdf.format(new Date(currentzone_time));
	}

	private boolean isSlaMissed(Ticket ticket) {
		if (ticket.SlaTimeAchevied != null
				&& !ticket.SlaTimeAchevied.equalsIgnoreCase("null")
				&& ticket.TotalTicketLifeCycleTimeSlab != null
				&& !ticket.TotalTicketLifeCycleTimeSlab
						.equalsIgnoreCase("null")) {
			int sla_achevied_time = Integer.parseInt(ticket.SlaTimeAchevied);
			int sla_time = Integer
					.parseInt(ticket.TotalTicketLifeCycleTimeSlab);
			if (sla_achevied_time > sla_time)
				return true;
			else
				return false;
		}
		return false;

	}
}
