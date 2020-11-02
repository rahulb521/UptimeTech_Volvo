package com.teramatrix.vos.model;

/*
 * TicketHistory_list_data Class is used as a Model Class for stored tickets history data values
 */
public class TicketHistory_list_data
{
	public String ticket_id;
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

	

	

	public String getTicket_id() {
		return ticket_id;
	}

	public void setTicket_id(String ticket_id) {
		this.ticket_id = ticket_id;
	}

	public TicketHistory_list_data(String ticket_id, String ticket_status,
			String ticket_sla, String ticket_sla_time) {
		this.ticket_id = ticket_id;
		this.ticket_status = ticket_status;
		this.ticket_sla = ticket_sla;
		this.ticket_sla_time = ticket_sla_time;
	}

	

	


	
	
}
