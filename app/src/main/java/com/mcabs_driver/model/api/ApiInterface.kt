package com.mcabs_driver.model.api

import com.mcabs_driver.BuildConfig
import com.mcabs_driver.firebaseSetup.NewRideNotificationDC
import com.mcabs_driver.model.dataclassses.AboutUsDC
import com.mcabs_driver.model.dataclassses.CardData
import com.mcabs_driver.model.dataclassses.PackageStatus
import com.mcabs_driver.model.dataclassses.SetUpIntentResponse
import com.mcabs_driver.model.dataclassses.Ticket
import com.mcabs_driver.model.dataclassses.UploadPackageResponse
import com.mcabs_driver.model.dataclassses.VehicleListDC
import com.mcabs_driver.model.dataclassses.base.BaseResponse
import com.mcabs_driver.model.dataclassses.bookingHistory.BookingHistoryDC
import com.mcabs_driver.model.dataclassses.bookingHistory.RideSummaryDC
import com.mcabs_driver.model.dataclassses.changeStatus.ChangeStatusDC
import com.mcabs_driver.model.dataclassses.cityVehicle.CityVehicleDC
import com.mcabs_driver.model.dataclassses.clientConfig.ClientConfigDC
import com.mcabs_driver.model.dataclassses.earningDC.EarningDC
import com.mcabs_driver.model.dataclassses.fetchRequiredDocument.FetchRequiredDocumentDC
import com.mcabs_driver.model.dataclassses.notificationDC.NotificationDC
import com.mcabs_driver.model.dataclassses.rideModels.AcceptRideDC
import com.mcabs_driver.model.dataclassses.rideModels.OngoingRideDC
import com.mcabs_driver.model.dataclassses.transactionHistory.TransactionHistoryDC
import com.mcabs_driver.model.dataclassses.updateDriverInfo.UpdateDriverInfo
import com.mcabs_driver.model.dataclassses.userData.Login
import com.mcabs_driver.model.dataclassses.userData.UserDataDC
import com.mcabs_driver.model.dataclassses.walletBalance.WalletBalanceDC
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

interface ApiInterface {

    @GET(GET_CLIENT_CONFIG)
    suspend fun getClientConfig(
        @Query("packageName") packageName: String = BuildConfig.APPLICATION_ID
    ): Response<BaseResponse<ClientConfigDC>>


    @POST(GENERATE_LOGIN_OTP)
    suspend fun generateLoginOtp(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>

    @POST(UPDATE_DRIVER_LOCATION)
    suspend fun updateDriverLocation(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


    @POST(VERIFY_LOGIN_OTP)
    suspend fun verifyLoginOtp(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<UserDataDC>>


    @Multipart
    @POST(UPDATE_DRIVER_INFO)
    suspend fun updateDriverInfo(
        @PartMap partMap: HashMap<String, RequestBody?>,
        @Part multipartBody: MultipartBody.Part? = null
    ): Response<BaseResponse<UpdateDriverInfo>>


    @GET(FETCH_REQUIRED_DOCUMENT)
    suspend fun fetchRequiredDocument(): Response<BaseResponse<List<FetchRequiredDocumentDC>>>


    @Multipart
    @POST(UPLOAD_DOCUMENT)
    suspend fun uploadDocument(
        @PartMap partMap: HashMap<String, RequestBody?>,
        @Part multipartBody: MultipartBody.Part? = null
    ): Response<BaseResponse<Any>>


    @GET(GET_CITY_VEHICLES)
    suspend fun getCityVehicles(
        @Query("city_id") cityId: String,
        @Query("request_ride_type") rideType: Int
    ): Response<BaseResponse<CityVehicleDC>>


    @POST(ADD_DRIVER_VEHICLE)
    suspend fun updateDriverVehicle(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


    @POST(ADD_BANK_DETAIL)
    suspend fun addBankDetail(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>

    @POST(DELETE_ACCOUNT)
    suspend fun deleteAccount(): Response<BaseResponse<Any>>


    @GET(GET_PROFILE)
    suspend fun getProfile(): Response<BaseResponse<Login>>


    @DELETE(LOGOUT)
    suspend fun logout(): Response<BaseResponse<Any>>


    @GET(BOOKING_HISTORY)
    suspend fun bookingHistory(): Response<BaseResponse<List<BookingHistoryDC>>>


    @GET(RIDE_SUMMARY)
    suspend fun rideSummary(
        @Query("tripId") tripId: String
    ): Response<BaseResponse<RideSummaryDC>>


    @POST(LOGIN_VIA_ACCESS_TOKEN)
    suspend fun loginViaAccessToken(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<UserDataDC>>


    @POST(EARNING_DETAIL)
    suspend fun getEarnings(
        @Body requestBody: RequestBody = JSONObject().apply {
            put("filter", 0)
        }.getJsonRequestBody()
    ): Response<BaseResponse<EarningDC>>


    @POST(CHANGE_AVAILABILITY)
    suspend fun changeAvailability(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<ChangeStatusDC>>


    @GET(GET_NOTIFICATIONS)
    suspend fun getNotifications(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int
    ): Response<BaseResponse<List<NotificationDC>>>


    @GET(Wallet_BALANCE)
    suspend fun getWalletBalance(): Response<BaseResponse<WalletBalanceDC>>

    @POST(REJECT_RIDE)
    suspend fun rejectRide(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


    @POST(ACCEPT_RIDE)
    suspend fun acceptRide(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<AcceptRideDC>>


    @POST(MARK_ARRIVED)
    suspend fun markArrived(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


    @POST(START_TRIP)
    suspend fun startTrip(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


    @POST(END_TRIP)
    suspend fun endTrip(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<NewRideNotificationDC>>


    @POST(ONGOING_TRIP)
    suspend fun ongoingTrip(): Response<BaseResponse<OngoingRideDC>>


    @GET(ABOUT_US)
    suspend fun aboutUs(
        @Query("operatorId") operatorId: String,
        @Query("cityId") cityId: String,
        @Query("pageType") type: Int
    ): Response<BaseResponse<AboutUsDC>>


    @GET(VEHICLE_DATA)
    suspend fun getVehicleData(
        @Query("city_id") cityId: String
    ): Response<BaseResponse<VehicleListDC>>

    @POST(TRANSACTION_HISTORY)
    suspend fun getTransactionHistory(): Response<BaseResponse<TransactionHistoryDC>>

    @POST(ADD_MONEY)
    suspend fun addMoney(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>

    @FormUrlEncoded
    @POST(ADD_CARD)
    suspend fun addCard(
        @Field("client_secret") secret: String = ""
    ): Response<BaseResponse<SetUpIntentResponse>>

    @FormUrlEncoded
    @POST(CONFIRM_CARD)
    suspend fun confirmCard(
        @Field("client_secret") secret: String = "",
        @Field("setup_intent_id") id: String = ""
    ): Response<BaseResponse<Any>>

    @FormUrlEncoded
    @POST(DELETE_CARD)
    suspend fun deleteCard(
        @Field("card_id") id: String = ""
    ): Response<BaseResponse<Any>>

    @GET(GET_CARDS)
    suspend fun getCards(
        @Query("payment_method_type") type: Int
    ): Response<BaseResponse<List<CardData>>>

    @POST(CANCEL_TRIP)
    suspend fun cancelTrip(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>

    @POST(RATE_THE_CUSTOMER)
    suspend fun rateCustomer(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>

    @POST(GENERATE_SUPPORT_TICKET)
    suspend fun generateSupportTicket(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>

    @Multipart
    @POST(UPLOAD_PACKAGE_IMAGE)
    suspend fun uploadPackageImage(
        @Part multipartBody: MultipartBody.Part? = null,
        @PartMap partMap: HashMap<String, RequestBody?>
    ): Response<BaseResponse<UploadPackageResponse>>

    @POST(UPDATE_PACKAGE_STATUS)
    suspend fun updatePackageStatus(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<PackageStatus>>

    @Multipart
    @POST(UPLOAD_TICKET_FILE)
    suspend fun uploadTicketFile(
        @Part multipartBody: MultipartBody.Part? = null,
        @PartMap partMap: HashMap<String, RequestBody?>,
    ): Response<BaseResponse<UploadPackageResponse>>

    @GET(LIST_TICKETS)
    suspend fun getRaisedListing(): Response<BaseResponse<List<Ticket>>>

    @POST(GENERATE_TICKET)
    suspend fun generateTicket(@Body requestBody: RequestBody): Response<BaseResponse<Any>>
}