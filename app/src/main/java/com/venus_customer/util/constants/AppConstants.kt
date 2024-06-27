package com.venus_customer.util.constants

object AppConstants {


    const val USER_TOKEN: String = ""
    const val USER_ACCESS_PASSWORD: String = ""
    const val USER_ACCESS_NAME: String = ""
    const val OPERATOR_TOKEN: String = "operator_token"
    const val CLIENT_ID: String = "client_id"

    const val DRIVER_PICKUP: String = "pick_up"
    const val DRIVER_ARRIVED: String = "arrived"
    const val DRIVER_INITIATED: String = "initiated"
    const val IS_TERMS: String = "isTerms"

    const val SALON_PACKAGE_NAME = "com.saloneCustomer"
    const val VENUS_PACKAGE_NAME = "com.venus_customer"

}

object APIEndPointsConstants {
    const val FETCH_OPERATOR_TOKEN = "getClientConfig"
    const val SEND_LOGIN_OTP = "customer/sendLoginOtp"
    const val VERIFY_OTP = "customer/verifyOtp"
    const val UPDATE_PROFILE = "customer/profile"
    const val LOGIN_VIA_ACCESS_TOKEN = "customer/login_using_access_token"
    const val LOGOUT = "customer/logout"
    const val INFORMATION_URL = "getInformationUrls"
    const val FIND_DRIVER = "customer/findDriver"
    const val GET_TRANSACTIONS = "get_transaction_history"
    const val REQUEST_TRIP = "customer/requestTrip"
    const val REQUEST_SCHEDULE = "insert_pickup_schedule"
    const val REMOVE_SCHEDULE = "remove_pickup_schedule"
    const val CANCEL_TRIP = "customer/cancelTrip"
    const val FARE_ESTIMATE = "customer/fareEstimate"
    const val SOS = "emergency/alert"
    const val RATE_DRIVER = "rateTheDriver"
    const val FETCH_ONGOING_TRIP = "customer/fetchOngoingTrip"
    const val GET_ALL_RIDES = "get_recent_rides"
    const val GET_ALL_SCHEDULE_RIDES = "show_pickup_schedules"
    const val GET_TRIP_SUMMARY = "getTripSummary"
    const val GET_NOTIFICATIONS = "customer/notifications"
    const val FIND_NEAR_DRIVER = "customer/findDriver"
    const val ADD_ADDRESS = "add_home_and_work_address"
    const val FETCH_USER_ADDRESS = "customer/fetch_user_address"
    const val FETCH_COUPON_PROMO = "getCouponsAndPromotions"
}

object APIParams {
    const val DEVICE_TYPE = "device_type"
    const val DEVICE_NAME = "device_name"
    const val PACKAGE_NAME = "package_name"
    const val LOGIN_TYPE = "login_type"
    const val APP_VERSION = "app_version"
    const val DEVICE_TOKEN = "device_token"
    const val LOCALE = "locale"
    const val OPERATOR_TOKEN = "operator_token"
}

object ApiKeys {
    const val API_SUCCESS = 143

}

object UserProfileConstants {
    const val USER_ID = "USER_ID"
    const val COUNTRY_CODE = "COUNTRY_CODE"
    const val VENUS_WALLET_BALANCE = "VENUS_WALLET_BALANCE"
    const val PUBLIC_ACCESS_TOKEN = "PUBLIC_ACCESS_TOKEN"
    const val EMAIL = "EMAIL"
    const val IMAGE = "IMAGE"
    const val NAME = "NAME"
    const val PHONE_NUMBER = "PHONE_NUMBER"
    const val USER_NAME = "USER_NAME"

}