package com.teramatrix.vos.accept_reject_ticket;

import com.teramatrix.vos.accept_reject_ticket.responsebodyticketdetail.TicketResponseBody;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIInterface {

     @FormUrlEncoded
     @POST("/Api/Sync?SyncStatus=true")
     Call<List<TicketResponseBody>> getDetailTicket(@Field("Token") String Token,
                                                    @Field("DeviceAlias") String DeviceAlias,
                                                    @Field("DbSynLastTime") String dbSynLastTime,
                                                    @Field("Ieminumber") String Ieminumber
     );

    //Reject 10
    //Accept -12
    @FormUrlEncoded
     @POST("/Api/OpenTicket?TicketUpdate=tt&TicketUpdate1=t&TicketUpdate2=ttt")
     Call<ResponseBody> acceptRejectTicket(@Field("Token") String Token,
                                        @Field("TicketId") String TicketId,
                                        @Field("LastModifiedTime") String LastModifiedTime,
                                        @Field("LastModifiedBy") String LastModifiedBy,
                                        @Field("TicketStatus") String TicketStatus,
                                        @Field("SuggestionComment") String SuggestionComment
     );


 }
