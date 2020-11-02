package com.teramatrix.vos.interfaces;

import com.teramatrix.vos.model.Ticket;

//Container Activity must implement this interface 
public interface INewTicket{

	//define method signature using setNewTicket passing ticket object.
	void setNewTicket(Ticket ticket) ;
	
	//define method signature using setAcceptedTicket passing boolean isAccepted and ticket id.
	void setAcceptedTicket(boolean isAccepted, String ticketId);
	
	//define method signature using api_VanReachedResponse
	void api_VanReachedResponse();
	
	//define method signature using api_JobCompletedREsponse
	void api_JobCompletedREsponse();
	
	//define method signature using api_TicketUpdateResponse
	void api_TicketUpdateResponse(String ticket_id);
}
