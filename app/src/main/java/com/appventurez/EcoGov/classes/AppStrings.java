package com.appventurez.EcoGov.classes;


import com.appventurez.EcoGov.BuildConfig;
import com.appventurez.EcoGov.R;

public class AppStrings {
    public final static String site_url = BuildConfig.SITE_URL;
    public final static String base_url = BuildConfig.BASE_URL;

    public final static String cbwtf_login = base_url+"App_Cbwtf/operator_login";
    public final static String hcf_login = base_url+"Cbwtf_api/hcf_login";
    public final static String hospital_data = base_url+"Cbwtf_api/get_hospital";
    public final static String cbwtf_data = base_url+"Cbwtf_api/get_cbwtf";
    public final static String cbwtf_scan_submit = base_url+"App_Cbwtf/scanning_cbwtf_side";
    public final static String hcf_scan_submit = base_url+"Cbwtf_api/scanning_hcf_side_api";
    public final static String hcf_scan_submit_hcf = base_url+"Cbwtf_api/scanning_hcf_side_api_hcf";
    public final static String get_today_data = base_url+"Cbwtf_api/get_today_qrdata";
    public final static String get_today_data_hcf = base_url+"Cbwtf_api/get_today_qrdata_hcf";
    public final static String get_report = base_url+"Cbwtf_api/get_report";
    public final static String get_report_cbwtf = base_url+"Cbwtf_api_new/get_report";
    public final static String get_report_hcf = base_url+"Cbwtf_api/get_report_hcf";
    public final static String get_all_report = base_url+"Cbwtf_api/get_all_report";
    public final static String delete_qr = base_url+"Cbwtf_api/delete_qr_data";
    public final static String generate_otp = base_url+"Cbwtf_api/generate_otp";

    public final static String send_mail = base_url+"Cbwtf_api/mail";
    public final static String get_otp = base_url+"Cbwtf_api/get_otp";
    public final static String check_attendance = base_url+"Cbwtf_api/check_cbwtf_attendance";
    public final static String mark_attendance = base_url+"Cbwtf_api/cbwtf_attendance";
    public final static String get_app_version = base_url+"Cbwtf_api/get_app_version";
    public final static String get_app_notice = base_url+"Cbwtf_api/get_app_notice";
    public final static String set_hospital_location = base_url+"Cbwtf_api/set_hospital_location";
    public final static String send_query = base_url+"Cbwtf_api/send_query";
    public final static String getGet_report_PDF = base_url+"App_Cbwtf/get_hcf";

    public final static String userName = "userName";
    public final static String userMobile = "userMobile";
    public final static String userPassword = "userPassword";
    public final static String userAddress = "userAddress";
    public final static String userID = "userID";
    public final static String userCbwtfID = "userCbwtfID";
    public final static String loginAs = "loginAs";
    public final static String hcfCode = "hcfCode";
    public final static String attendance_compulsory = "attendance_compulsory";

    public final static String currentHcfCode = "currentHcfCode";
    public final static String currentHcfName = "currentHcfName";
    public final static String bluetoothConnection = "bluetoothConnection";

}
