package com.teramatrix.vos.jsonparser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.teramatrix.vos.model.DeclineReasonModel;
import com.teramatrix.vos.model.EstimationCostModel;
import com.teramatrix.vos.model.Ticket;
import com.teramatrix.vos.model.TicketStatus;

/**
 * 
 * This TicketJsonParser class is used for parsing json with ticketing data.
 */
public class TicketJsonParser {

	// this method is used for get tickets from array json to string json.
	public static ArrayList<Ticket> getTicketFromArrayJsonString(
			String ticketJsonString) throws Exception {

		Ticket ticket = null;
		if (ticketJsonString != null) {
			try {
				JSONArray jsonarray = new JSONArray(ticketJsonString);
				if (jsonarray.length() != 0) {

					ArrayList<Ticket> tickets = new ArrayList<Ticket>();
					for (int i = 0; i < jsonarray.length(); i++) {
						JSONObject jsonobject = jsonarray.getJSONObject(i);
						ticket = new Ticket();
						if (jsonobject != null) {

							if (jsonobject.has("Id")) {
								ticket.Id = jsonobject.getString("Id");
							}
							if (jsonobject.has("BreakDownLocation")) {
								ticket.BreakDownLocation = jsonobject
										.getString("BreakDownLocation");
							}
							if (jsonobject.has("BreackDownLatitude")) {
								ticket.BreackDownLatitude = jsonobject
										.getString("BreackDownLatitude");
							}
							if (jsonobject.has("BreackDownLongitude")) {
								ticket.BreackDownLongitude = jsonobject
										.getString("BreackDownLongitude");
							}
							if (jsonobject.has("VehicleType")) {
								ticket.vehicleType = jsonobject
										.getString("VehicleType");
							}
							if (jsonobject.has("Description")) {
								ticket.Description = jsonobject
										.getString("Description");
							}
							if (jsonobject.has("CreationTime")) {
								ticket.CreationTime = jsonobject
										.getString("CreationTime");
							}
							if (jsonobject.has("TotalTicketLifeCycleTimeSlab")) {
								ticket.SlaTimeAchevied = jsonobject
										.getString("TotalTicketLifeCycleTimeSlab");
							}
							if (jsonobject.has("DefaultSlaTime")) {
								ticket.TotalTicketLifeCycleTimeSlab = jsonobject
										.getString("DefaultSlaTime");
							}
							if (jsonobject.has("CustomeContact_no")) {
								ticket.CustomeContact_no = jsonobject
										.getString("CustomeContact_no");
							}
							if (jsonobject.has("EstimatedDistance")) {
								ticket.EstimatedDistance = jsonobject
										.getString("EstimatedDistance");
							}
							if (jsonobject.has("OwnerContact_no")) {
								ticket.OwnerContact_no = jsonobject
										.getString("OwnerContact_no");
							}
							if (jsonobject.has("TicketIdAlias")) {
								ticket.Id  = jsonobject
										.getString("TicketIdAlias") +"/"+ticket.Id;
							}
							if(jsonobject.has("ticketid_alias"))
							{
								ticket.Id  = jsonobject
										.getString("ticketid_alias") +"/"+ticket.Id;
							}

							
							
							tickets.add(ticket);
						}
					}
					return tickets;

				} else {
				}
			} catch (Exception e) {
				
				throw e;
			}
		}

		return null;
	}

	// this method is used for get ticket for passing json ticket
	public static Ticket getTicket(String ticketJsonString) throws Exception {

		Ticket ticket = null;
		if (ticketJsonString != null) {
			try {
				if (ticketJsonString.length() != 0) {

					JSONArray jsonArray = new JSONArray(ticketJsonString);
					if (jsonArray != null) {

						if (jsonArray.length() > 0) {
							JSONObject jsonobject = (JSONObject) jsonArray
									.get(0);

							if (jsonobject.has("Status")) {
								String status = jsonobject.getString("Status");
								if (status != null
										&& status.equalsIgnoreCase("0"))
									return null;
							}

							ticket = new Ticket();
							if (jsonobject.has("TicketId")) {
								ticket.Id = jsonobject.getString("TicketId");
							}
							if (jsonobject.has("TicketStatus")) {
								ticket.TicketStatus = jsonobject
										.getString("TicketStatus");
							}
							if (jsonobject.has("BreakdownLocation")) {
								ticket.BreakDownLocation = jsonobject
										.getString("BreakdownLocation");
							}
							if (jsonobject.has("BreakdownLattitude")) {
								ticket.BreackDownLatitude = jsonobject
										.getString("BreakdownLattitude");
							}
							if (jsonobject.has("BreakdownLongitude")) {
								ticket.BreackDownLongitude = jsonobject
										.getString("BreakdownLongitude");
							}
							if (jsonobject.has("VehicleType")) {
								ticket.vehicleType = jsonobject
										.getString("VehicleRegistrationNo");
							}
							if (jsonobject.has("VehicleRegisterNumber")) {
								ticket.VehicleRegistrationNo = jsonobject
										.getString("VehicleRegisterNumber");
							}
							if (jsonobject.has("Description")) {
								ticket.Description = jsonobject
										.getString("Description");
							}
							if (jsonobject.has("CreationTime")) {
								ticket.CreationTime = jsonobject
										.getString("CreationTime");
							}
							if (jsonobject.has("TotalTicketLifecycleTimeSla")) {
								ticket.TotalTicketLifeCycleTimeSlab = jsonobject
										.getString("TotalTicketLifecycleTimeSla");
							}
							if (jsonobject.has("LastModifiedTime")) {
								ticket.LastModifiedTime = jsonobject
										.getString("LastModifiedTime");
							}
							if (jsonobject.has("Priority")) {
								ticket.Priority = jsonobject
										.getString("Priority");
							}
						}

					}

				} else {
				}
			} catch (Exception e) {

				throw e;

			}
		}

		return ticket;
	}

	// This method is used for getting ticket status with passing json response
	public static ArrayList<TicketStatus> getTicketStatus(String responseJson)
			throws Exception {
		ArrayList<TicketStatus> ticketStatus = new ArrayList<TicketStatus>();
		if (responseJson != null && responseJson.length() > 0) {

			try {
				JSONArray array = new JSONArray(responseJson);
				if (array != null && array.length() > 0) {
					for (int i = 0; i < array.length(); i++) {
						TicketStatus status = new TicketStatus();
						JSONObject jsonObject = (JSONObject) array.get(i);
						if (jsonObject.has("Id")) {
							status.id = jsonObject.getString("Id");
						}
						if (jsonObject.has("SequenceNo")) {
							status.sequenceNumber = jsonObject
									.getString("SequenceNo");
						}
						if (jsonObject.has("StatusName")) {
							status.statusName = jsonObject
									.getString("StatusName");
						}
						if (jsonObject.has("Alias")) {
							status.alias = jsonObject.getString("Alias");
						}
						ticketStatus.add(status);
					}
					return ticketStatus;
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block

				throw e;
			}
		}
		return null;
	}

	// This method is used for all getting getTicketActionReasonsList.
	public static ArrayList<DeclineReasonModel> getTicketActionReasonsList(
			String responseJson) throws Exception {
		ArrayList<DeclineReasonModel> reasonsList = new ArrayList<DeclineReasonModel>();
		if (responseJson != null && responseJson.length() > 0) {

			try {
				JSONArray array = new JSONArray(responseJson);
				if (array != null && array.length() > 0) {
					for (int i = 0; i < array.length(); i++) {
						DeclineReasonModel reason = new DeclineReasonModel();
						JSONObject jsonObject = (JSONObject) array.get(i);
						if (jsonObject.has("Id")) {
							reason.setId(jsonObject.getString("Id"));
						}
						if (jsonObject.has("ReasonName")) {
							reason.setReason_name(jsonObject
									.getString("ReasonName"));
						}
						if (jsonObject.has("TypeId")) {
							reason.reason_type = jsonObject.getString("TypeId");
						}
						reasonsList.add(reason);
					}
					return reasonsList;
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block

				throw e;
			}
		}
		return null;
	}

	// This method is used for all getEstimationCostList.
	public static ArrayList<EstimationCostModel> getEstimationCostList(
			String responseJson) throws Exception {
		ArrayList<EstimationCostModel> estiamtionCostList = new ArrayList<EstimationCostModel>();
		if (responseJson != null && responseJson.length() > 0) {

			try {
				JSONArray array = new JSONArray(responseJson);
				if (array != null && array.length() > 0) {
					for (int i = 0; i < array.length(); i++) {
						EstimationCostModel es_cost = new EstimationCostModel();
						JSONObject jsonObject = (JSONObject) array.get(i);
						if (jsonObject.has("Id")) {
							es_cost.id = jsonObject.getString("Id");
						}
						if (jsonObject.has("CostRange")) {
							es_cost.cost_range = jsonObject
									.getString("CostRange");
						}
						estiamtionCostList.add(es_cost);
					}
					return estiamtionCostList;
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block

				throw e;
			}
		}
		return null;
	}

	// This method is used for getExistingOpenAndClosedTicket.
	public static ArrayList<ArrayList<Ticket>> getExistingOpenAndClosedTicket(
			String jsonResponse) throws Exception {
		ArrayList<ArrayList<Ticket>> tickets = new ArrayList<ArrayList<Ticket>>();

		ArrayList<Ticket> openTicketsList = new ArrayList<Ticket>();
		ArrayList<Ticket> closedTicketsList = new ArrayList<Ticket>();
		if (jsonResponse != null && jsonResponse.length() > 0) {

			try {

				JSONArray array = new JSONArray(jsonResponse);
				// JSONObject object = new JSONObject(jsonResponse);

				if (array != null && array.length() > 0) {
					JSONObject jsonObject = (JSONObject) array.get(0);
					if (jsonObject != null) {
						if (jsonObject.has("TicketOpenList")) {
							JSONArray openTicketArray = jsonObject
									.getJSONArray("TicketOpenList");

							if (openTicketArray != null
									&& openTicketArray.length() > 0) {
								for (int i = 0; i < openTicketArray.length(); i++) {
									Ticket ticket = new Ticket();
									JSONObject ticketObj = (JSONObject) openTicketArray
											.get(i);
									if (ticketObj.has("TicketId")) {
										ticket.Id = ticketObj
												.getString("TicketId");
									}
									if (ticketObj.has("Description")) {
										ticket.Description = ticketObj
												.getString("Description");
									}
									if (ticketObj.has("TicketStatus")) {
										ticket.TicketStatus = ticketObj
												.getString("TicketStatus");
									}
									if (ticketObj.has("Priority")) {
										ticket.Priority = ticketObj
												.getString("Priority");
									}
									if (ticketObj.has("CreationTime")) {
										ticket.CreationTime = ticketObj
												.getString("CreationTime");
									}
									if (ticketObj.has("LastModifiedBy")) {
										ticket.LastModifiedBy = ticketObj
												.getString("LastModifiedBy");
									}
									if (ticketObj.has("LastModifiedTime")) {
										ticket.LastModifiedTime = ticketObj
												.getString("LastModifiedTime");
									}
									if (ticketObj.has("BreakdownLocation")) {
										ticket.BreakDownLocation = ticketObj
												.getString("BreakdownLocation");
									}
									if (ticketObj.has("BreakdownLongitude")) {
										ticket.BreackDownLongitude = ticketObj
												.getString("BreakdownLongitude");
									}
									if (ticketObj.has("BreakdownLattitude")) {
										ticket.BreackDownLatitude = ticketObj
												.getString("BreakdownLattitude");
									}
									if (ticketObj.has("Isdeclined")) {
										ticket.IsDeclined = ticketObj
												.getString("Isdeclined");
									}
									if (ticketObj
											.has("EstimatedTimeForJobCompletion")) {
										ticket.EstimatedTimeForJobComplition = ticketObj
												.getString("EstimatedTimeForJobCompletion");
									}
									if (ticketObj
											.has("TotalTicketLifecycleTimeSla")) {
										ticket.SlaTimeAchevied = ticketObj
												.getString("TotalTicketLifecycleTimeSla");
									}
									if (ticketObj
											.has("EstimatedTimeForJobCompletionSubmitTime")) {
										ticket.EstimatedTimeForJobComplitionSubmitTime = ticketObj
												.getString("EstimatedTimeForJobCompletionSubmitTime");
									}
									if (ticketObj.has("VehicleRegisterNumber")) {
										ticket.VehicleRegistrationNo = ticketObj
												.getString("VehicleRegisterNumber");
									}
									if (ticketObj.has("RepairCost")) {
										ticket.EstimatedCostForJobComplition = ticketObj
												.getString("RepairCost");
									}
									if (ticketObj.has("DefaultSlaTime")) {
										ticket.TotalTicketLifeCycleTimeSlab = ticketObj
												.getString("DefaultSlaTime");
									}
									if (ticketObj
											.has("JobCompleteResponseTime")) {
										ticket.AcheviedEstimatedTimeForJobComplition = ticketObj
												.getString("JobCompleteResponseTime");
									}
									if (ticketObj.has("SuggestionComment")) {
										ticket.SuggestionComment = ticketObj
												.getString("SuggestionComment");
									}

									if (ticketObj.has("RouteId")) {
									}
									if (ticketObj.has("CustomerContactNo")) {
										ticket.CustomeContact_no = ticketObj
												.getString("CustomerContactNo");
									}
									if (ticketObj.has("EstimatedDistance")) {
										ticket.EstimatedDistance = ticketObj
												.getString("EstimatedDistance");
									}
									if (ticketObj.has("Owner_Contact_no")) {
										ticket.OwnerContact_no = ticketObj
												.getString("Owner_Contact_no");
									}
									if (ticketObj.has("TicketIdAlias")) {
										ticket.Id = ticketObj
												.getString("TicketIdAlias")+"/"+ticket.Id;
									}
									if (ticketObj.has("IsTripEnd")) {
										ticket.IsTripEnd = ticketObj
												.getString("IsTripEnd");
									}
									
									
									
									openTicketsList.add(ticket);
								}
							}
							tickets.add(openTicketsList);
							tickets.add(closedTicketsList);
						}
						if (jsonObject.has("TicketCloseList")) {
							JSONArray closedTicketArray = jsonObject
									.getJSONArray("TicketCloseList");

							if (closedTicketArray != null
									&& closedTicketArray.length() > 0) {
								for (int i = 0; i < closedTicketArray.length(); i++) {
									Ticket ticket = new Ticket();
									JSONObject ticketObj = (JSONObject) closedTicketArray
											.get(i);
									if (ticketObj.has("TicketId")) {
										ticket.Id = ticketObj
												.getString("TicketId");
									}
									if (ticketObj.has("Description")) {
										ticket.Description = ticketObj
												.getString("Description");
									}
									if (ticketObj.has("TicketStatus")) {
										ticket.TicketStatus = ticketObj
												.getString("TicketStatus");
									}
									if (ticketObj.has("Priority")) {
										ticket.Priority = ticketObj
												.getString("Priority");
									}
									if (ticketObj.has("CreationTime")) {
										ticket.CreationTime = ticketObj
												.getString("CreationTime");
									}
									if (ticketObj.has("LastModifiedBy")) {
										ticket.LastModifiedBy = ticketObj
												.getString("LastModifiedBy");
									}
									if (ticketObj.has("LastModifiedTime")) {
										ticket.LastModifiedTime = ticketObj
												.getString("LastModifiedTime");
									}
									if (ticketObj.has("BreakdownLocation")) {
										ticket.BreakDownLocation = ticketObj
												.getString("BreakdownLocation");
									}
									if (ticketObj.has("BreakdownLongitude")) {
										ticket.BreackDownLongitude = ticketObj
												.getString("BreakdownLongitude");
									}
									if (ticketObj.has("BreakdownLattitude ")) {
										ticket.BreackDownLatitude = ticketObj
												.getString("BreakdownLattitude");
									}
									if (ticketObj.has("Isdeclined")) {
										ticket.IsDeclined = ticketObj
												.getString("Isdeclined");
									}
									if (ticketObj
											.has("EstimatedTimeForJobCompletion")) {
										ticket.EstimatedTimeForJobComplition = ticketObj
												.getString("EstimatedTimeForJobCompletion");
									}
									if (ticketObj
											.has("TotalTicketLifecycleTimeSla")) {
										ticket.SlaTimeAchevied = ticketObj
												.getString("TotalTicketLifecycleTimeSla");
									}
									if (ticketObj
											.has("EstimatedTimeForJobCompletionSubmitTime")) {
										ticket.EstimatedTimeForJobComplitionSubmitTime = ticketObj
												.getString("EstimatedTimeForJobCompletionSubmitTime");
									}
									if (ticketObj.has("VehicleRegisterNumber")) {
										ticket.VehicleRegistrationNo = ticketObj
												.getString("VehicleRegisterNumber");
									}
									if (ticketObj.has("DefaultSlaTime")) {
										ticket.TotalTicketLifeCycleTimeSlab = ticketObj
												.getString("DefaultSlaTime");
									}
									if (ticketObj
											.has("JobCompleteResponseTime")) {
										ticket.AcheviedEstimatedTimeForJobComplition = ticketObj
												.getString("JobCompleteResponseTime");
									}
									if (ticketObj.has("SuggestionComment")) {
										ticket.SuggestionComment = ticketObj
												.getString("SuggestionComment");
									}
									if (ticketObj.has("RouteId")) {
									}
									if (ticketObj.has("CustomerContactNo")) {
										ticket.CustomeContact_no = ticketObj
												.getString("CustomerContactNo");
									}
									if (ticketObj.has("RepairCost")) {
										ticket.EstimatedCostForJobComplition = ticketObj
												.getString("RepairCost");
									}
									if (ticketObj.has("EstimatedDistance")) {
										ticket.EstimatedDistance = ticketObj
												.getString("EstimatedDistance");
									}
									if (ticketObj.has("Owner_Contact_no")) {
										ticket.OwnerContact_no = ticketObj
												.getString("Owner_Contact_no");
									}
									if (ticketObj.has("TicketIdAlias")) {
										ticket.Id = ticketObj
												.getString("TicketIdAlias")+"/"+ticket.Id;
									}
									if (ticketObj.has("IsTripEnd")) {
										ticket.IsTripEnd = ticketObj
												.getString("IsTripEnd");
									}
									
									closedTicketsList.add(ticket);
								}
								tickets.add(closedTicketsList);
							}

						}
						if (jsonObject.has("DbSynLastTime")) {
							String DbSynLastTime = jsonObject
									.getString("DbSynLastTime");

						}

					}

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block

				throw e;
			}

		}

		return tickets;
	}

}
