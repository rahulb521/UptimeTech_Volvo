package com.teramatrix.vos.appurl;

/**
 * @author Gaurav.Mangal
 * <p>
 * This ApiUrls class is used for calling web api for paticular name.
 * also mention with urls internal and external.
 */
public class ApiUrls {


    // Used for internal url @ 605
    /*
     * public static String url_vecv = "http://180.149.46.100:665/";
     * public static String url_telimatic = "http://180.149.46.100:692/";
     */

    // Used for Internal Production @ 667

    /*
     * public static String url_vecv = "http://180.149.46.100:666/";
     *  public static String url_telimatic = "http://180.149.46.100:694/";
     */

    // Used for Demo @ 675
    /*
     * public static String url_vecv = "http://180.149.46.100:668/";
     * public static String url_telimatic ="";
     */

    // Used for play store release version (Go daddy)
    // public static String url_vecv="http://104.238.99.56:8081/";

    // URL for DEVICE_CONFIGURATION_LICENSE
    public static String DEVICE_CONFIGURATION_LICENSE = "Api/ServiceEnginner";

    // URL for DEVICE_CONFIGURAITON_PIN
    public static String CONFIGURATION_PIN = "Api/ServiceEnginner?Pin=ttpl";

    // URL for DEVICE_CONFIGURAITON_PIN_SIGNIN
    public static String CONFIGURATION_SIGN_IN_PIN = "Api/ServiceEnginner?SignId=ttpl&ServiceEnginner=tttpl";

    // URL for DEVICE_CONFIGURAITON_PIN_SIGNIN
    public static String INSERT_TRACKING_LOCATION = "Api/Tracking";


    // URL for TICKET_OPEN_DEVICE_ALIAS
    public static String TICKET_OPEN_DEVICE_ALIAS = "Api/OpenTicket";

    // URL for TICKET_DECLINE
    public static String TICKET_DECLINE = "Api/OpenTicket?TicketAccept=ttt";

    // URL for TICKET_ACCEPT
    public static String TICKET_ACCEPT = "Api/OpenTicket?TicketAccept=ttt";

    // URL for Offline Location data upload on server when netwok available
    public static String BULK_UPLOAD_OFFLINE_LOCATION = "Api/Tracking?tracking=ttpl&tracking1=ttpl&tracking2=ttpl";

    // URL for BULK_UPLOAD_OFFLINE_TICKETS
    public static String BULK_UPLOAD_OFFLINE_TICKETS = "Api/OpenTicketOffline";

    // URL for TICKET_STATUS_LIST
    public static String TICKET_STATUS_LIST = "Api/TicketStatus";

    // URL for TICKET_ACTION_REASONS_LIST
    public static String TICKET_ACTION_REASONS_LIST = "Api/Global?Reason=android";

    // URL for ESTIMATION_COST_LIST
    public static String ESTIMATION_COST_LIST = "Api/Global?EstimateCost=e&EstimateCost1=e1";

    // URL for SYNC_DB
    public static String SYNC_DB = "Api/Sync?Sync=s";

    // URL for UPDATE_TICKET_STATUS
    public static String UPDATE_TICKET_STATUS = "Api/OpenTicket?TicketUpdate=tt&TicketUpdate1=t&TicketUpdate2=ttt";


    //---------------------------------------------------------------------------------------------------------------
    //For Volvo UpTime Application

    // URL for GET_VEHICLE_DATA
    public static String UPTIME_GET_VEHICLE_DATA = "Api/Uptime?ApiType=vehicle_list";
    public static String UPTIME_GET_DATA = "Api/Uptime?ApiType=vehicle_ticket_reasons";

    // URL for GET_REASONS
    public static String UPTIME_GET_REASONS = "Api/Uptime?ApiType=service_reasons_list";

    // URL for UPDATE_TICKET
    public static String UPTIME_UPDATE_TICKET = "Api/UptimeTicket?Action=update&InsertUpdateTicket=1";

    // URL for INSERT_TICKET
    public static String UPTIME_INSERT_TICKET = "Api/UptimeTicket?Action=insert&InsertUpdateTicket=1";

    // URL for GET_TICKET_DETAILS
    public static String UPTIME_GET_TICKET_DETAILS = "Api/UptimeTicket?Ticket=tt&OpenTicket=tt";

    // URL for GET_OPEN_TICKET_REASONS
    public static String UPTIME_GET_OPEN_TICKET_REASONS = "Api/UptimeTicket?ReasonType=open&TicketReason=1&TicketDelayedReason=1";

    // URL for SYNC_OFFLINE
    public static String UPTIME_SYNC_OFFLINE = "Api/OpenTicketOffline?UptimeOfflineData=tt";

    //Get Ticket ID from UUID
    public static String UPTIME_GET_TICKET_ID_FROM_UUID = "Api/OpenTicketOffline?UptimeOfflineData=ticket&InsertTicket=Ticket";

    public static final String UTILIZATIONDETAILS = "Api/Mobile?UtilizationDetails=jj";
    public static final String SAVEUTILIZATION = "Api/Mobile?UtilizationDetails=jj&UtilizationDetails1=hh";
    public static final String REFRESHTOKEN="Api/ServiceEnginner?refreshPushNotification=1&refreshPushNotification1=1&refreshPushNotification2=1";
    public static String APP_VERSION_UPDATE = "Api/Global?AppConfig=fdgdf&AppVersion=dsfds&AppVersion1=1";

}