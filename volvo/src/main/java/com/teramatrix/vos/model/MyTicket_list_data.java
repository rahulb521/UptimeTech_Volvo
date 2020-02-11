package com.teramatrix.vos.model;

/*
 *  MyTicket_list_data Class is used as a Model Class for stored tickets data values
 */
public class MyTicket_list_data
{
	public String ticket_id;
	public String ticket_owner_name;
	public double ticket_dest_lat;
	public double ticket_dest_long;
	public String ticket_status;
	public String ticket_sla;
	public String ticket_sla_time;
	

	public String getTicket_status() {
		return ticket_status;
	}

	public void setTicket_status(String ticket_status) {
		this.ticket_status = ticket_status;
	}

	public String getTicket_sla() {
		return ticket_sla;
	}

	public void setTicket_sla(String ticket_sla) {
		this.ticket_sla = ticket_sla;
	}

	public String getTicket_sla_time() {
		return ticket_sla_time;
	}

	public void setTicket_sla_time(String ticket_sla_time) {
		this.ticket_sla_time = ticket_sla_time;
	}

	public String getTicket_owner_name() {
		return ticket_owner_name;
	}

	public void setTicket_owner_name(String ticket_owner_name) {
		this.ticket_owner_name = ticket_owner_name;
	}

	

	public String getTicket_id() {
		return ticket_id;
	}

	public void setTicket_id(String ticket_id) {
		this.ticket_id = ticket_id;
	}
	public MyTicket_list_data(String ticket_id, String ticket_owner_name,
			double ticket_dest_lat, double ticket_dest_long,
			String ticket_status, String ticket_sla, String ticket_sla_time) {
		this.ticket_id = ticket_id;
		this.ticket_owner_name = ticket_owner_name;
		this.ticket_dest_lat = ticket_dest_lat;
		this.ticket_dest_long = ticket_dest_long;
		this.ticket_status = ticket_status;
		this.ticket_sla = ticket_sla;
		this.ticket_sla_time = ticket_sla_time;
	}

	public double getTicket_dest_lat() {
		return ticket_dest_lat;
	}

	public void setTicket_dest_lat(double ticket_dest_lat) {
		this.ticket_dest_lat = ticket_dest_lat;
	}

	public double getTicket_dest_long() {
		return ticket_dest_long;
	}

	public void setTicket_dest_long(double ticket_dest_long) {
		this.ticket_dest_long = ticket_dest_long;
	}
	
	
	
}
