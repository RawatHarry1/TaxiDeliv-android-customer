package com.marsapp_driver.model.api

const val GET_CLIENT_CONFIG = "getClientConfig"

const val GENERATE_LOGIN_OTP = "driver/generateLoginOtp"

const val VERIFY_LOGIN_OTP = "driver/login"

const val UPDATE_DRIVER_INFO = "updateDriverProfile"

const val FETCH_REQUIRED_DOCUMENT = "fetchRequiredDocs"

const val UPLOAD_DOCUMENT = "uploadDocument"

const val GET_CITY_VEHICLES = "getCityVehicles"

const val UPDATE_DRIVER_VEHICLE = "driver/updateVehicle"

const val ADD_DRIVER_VEHICLE = "driver/add_new_vehicle"

const val ADD_BANK_DETAIL = "payouts/addAccount"

const val GET_PROFILE = "driver/profile"

const val LOGOUT = "logout_driver"

const val DELETE_ACCOUNT = "removeAccount"

const val BOOKING_HISTORY = "driver/bookingHistory"

const val RIDE_SUMMARY = "driver/getTripSummary"

const val LOGIN_VIA_ACCESS_TOKEN = "loginViaAccessToken"

const val EARNING_DETAIL = "getDriverAllEarningsV2"

const val CHANGE_AVAILABILITY = "changeAvailability"

const val GET_NOTIFICATIONS = "/driver/notifications"

const val Wallet_BALANCE = "driver/walletBalance"

const val REJECT_RIDE = "driver/rejectTripRequest"

const val ACCEPT_RIDE = "driver/acceptTripRequest"

const val MARK_ARRIVED = "driver/markArrived"

const val START_TRIP = "driver/startTrip"

const val END_TRIP = "driver/endTrip"

const val ONGOING_TRIP = "driver/fetchOngoingTrip"

const val ABOUT_US = "getInformationUrls"

const val VEHICLE_DATA = "driver/fetch_driver_vehicles"

const val TRANSACTION_HISTORY = "get_transaction_history"

const val CANCEL_TRIP = "cancelTheTrip"
const val UPDATE_DRIVER_LOCATION = "update_driver_location"

const val RATE_THE_CUSTOMER = "rate_the_customer"
const val GENERATE_SUPPORT_TICKET = "generate_driver_support_ticket"
const val ADD_CARD = "stripe/add_card_3d"
const val CONFIRM_CARD = "stripe/confirm_card_3d"
const val GET_CARDS = "fetch/cardDetails"
const val DELETE_CARD = "removeCard"
const val ADD_MONEY = "add_money_via_stripe"
const val UPLOAD_PACKAGE_IMAGE = "upload_file_driver"
const val UPDATE_PACKAGE_STATUS = "update_delivery_package_status"
const val GENERATE_TICKET = "generate_ticket"
const val LIST_TICKETS = "list_support_tickets"
const val UPLOAD_TICKET_FILE = "upload_file_support_ticket"