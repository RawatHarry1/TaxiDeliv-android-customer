package com.venus_customer.model.api

import com.google.gson.JsonElement
import com.salonedriver.model.dataclassses.notificationDC.NotificationDC
import com.venus_customer.BuildConfig
import com.venus_customer.model.dataClass.AboutAppDC
import com.venus_customer.model.dataClass.CouponAndPromos
import com.venus_customer.model.dataClass.ScheduleList
import com.venus_customer.model.dataClass.ShowMessage
import com.venus_customer.model.dataClass.WalletTransaction
import com.venus_customer.model.dataClass.addedAddresses.AddedAddressData
import com.venus_customer.model.dataClass.base.BaseResponse
import com.venus_customer.model.dataClass.base.ClientConfig
import com.venus_customer.model.dataClass.fareEstimate.FareEstimateDC
import com.venus_customer.model.dataClass.fetchOngoingTrip.FetchOngoingTripDC
import com.venus_customer.model.dataClass.findDriver.FindDriverDC
import com.venus_customer.model.dataClass.findNearDriver.FindNearDriverDC
import com.venus_customer.model.dataClass.requestTrip.RequestTripDC
import com.venus_customer.model.dataClass.tripsDC.RideSummaryDC
import com.venus_customer.model.dataClass.tripsDC.TripListDC
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.util.constants.APIEndPointsConstants
import com.venus_customer.util.constants.APIEndPointsConstants.FIND_NEAR_DRIVER
import com.venus_customer.util.constants.APIEndPointsConstants.GET_NOTIFICATIONS
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query
import retrofit2.http.Url


interface ApiInterface {

    @GET
    suspend fun searchPlaces(
        @Url url: String = "https://maps.googleapis.com/maps/api/place/textsearch/json",
        @Query("query") inputText: String,
        @Query("radius") radius: String = "50",
        @Query("key") key: String
    ): Response<JsonElement>

    @GET
    suspend fun getDistanceFromGoogle(
        @Url url: String = "https://maps.googleapis.com/maps/api/distancematrix/json",
        @Query("destinations") destination: String,
        @Query("origins") origin: String,
        @Query("key") key: String,
        @Query("units") units: String = "km",
        @Query("mode") mode: String = "driving",
    ): Response<JsonElement>

    @GET(APIEndPointsConstants.FETCH_OPERATOR_TOKEN)
    suspend fun fetchUserToken(
        @Query("packageName") packageName: String = BuildConfig.APPLICATION_ID
    ): Response<BaseResponse<ClientConfig>>


    @POST(APIEndPointsConstants.SEND_LOGIN_OTP)
    suspend fun sendLoginOtp(
        @Body body: RequestBody
    ): Response<BaseResponse<Any>>

    @POST(APIEndPointsConstants.VERIFY_OTP)
    suspend fun verifyOtp(
        @Body body: RequestBody
    ): Response<BaseResponse<UserDataDC>>


    @Multipart
    @PUT(APIEndPointsConstants.UPDATE_PROFILE)
    suspend fun updateProfile(
        @PartMap partMap: HashMap<String, RequestBody?>,
        @Part multipartBody: MultipartBody.Part? = null
    ): Response<BaseResponse<Any>>


    @POST(APIEndPointsConstants.LOGIN_VIA_ACCESS_TOKEN)
    suspend fun loginViaAccessToken(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<UserDataDC>>

    @DELETE(APIEndPointsConstants.LOGOUT)
    suspend fun logout(): Response<BaseResponse<Any>>


    @GET(APIEndPointsConstants.INFORMATION_URL)
    suspend fun aboutApp(
        @Query("operatorId") operatorId: String,
        @Query("cityId") cityId: String
    ): Response<BaseResponse<AboutAppDC>>

    @POST(APIEndPointsConstants.GET_TRANSACTIONS)
    suspend fun getTransactions(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<WalletTransaction>>


    @POST(APIEndPointsConstants.FIND_DRIVER)
    suspend fun findDriver(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<FindDriverDC>>


    @POST(APIEndPointsConstants.REQUEST_TRIP)
    suspend fun requestTrip(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<RequestTripDC>>

    @POST(APIEndPointsConstants.REQUEST_SCHEDULE)
    suspend fun requestSchedule(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<RequestTripDC>>

    @HTTP(method = "DELETE", path = APIEndPointsConstants.CANCEL_TRIP, hasBody = true)
    suspend fun cancelTrip(
        @Query("sessionId") sessionId: String,
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


    @POST(APIEndPointsConstants.FARE_ESTIMATE)
    suspend fun fareEstimate(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<FareEstimateDC>>

    @POST(APIEndPointsConstants.ENTER_PROMO_CODE)
    suspend fun enterPromoCode(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>

    @POST(APIEndPointsConstants.SOS)
    suspend fun hitSOS(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


    @POST(APIEndPointsConstants.RATE_DRIVER)
    suspend fun rateDriver(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


    @GET(APIEndPointsConstants.FETCH_ONGOING_TRIP)
    suspend fun fetchOngoingTrip(): Response<BaseResponse<FetchOngoingTripDC>>

    @FormUrlEncoded
    @POST(APIEndPointsConstants.GET_ALL_RIDES)
    suspend fun getAllRides(
        @Field("start_from") startFrom: String = "0"
    ): Response<BaseResponse<List<TripListDC>>>


    @POST(APIEndPointsConstants.GET_ALL_SCHEDULE_RIDES)
    suspend fun getAllScheduleRides(): Response<BaseResponse<List<ScheduleList>>>

    @GET(APIEndPointsConstants.GET_TRIP_SUMMARY)
    suspend fun getTripSummary(
        @Query("tripId") tripId: String,
        @Query("driverId") driverId: String
    ): Response<BaseResponse<RideSummaryDC>>


    @GET(GET_NOTIFICATIONS)
    suspend fun getNotifications(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int
    ): Response<BaseResponse<List<NotificationDC>>>

    @POST(FIND_NEAR_DRIVER)
    suspend fun findNearDriver(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<FindNearDriverDC>>

    @POST(APIEndPointsConstants.ADD_ADDRESS)
    suspend fun addAddress(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>

    @POST(APIEndPointsConstants.REMOVE_SCHEDULE)
    suspend fun removeSchedule(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<ShowMessage>>

    @POST(APIEndPointsConstants.FETCH_USER_ADDRESS)
    suspend fun fetchAddresses(): Response<BaseResponse<AddedAddressData>>

    @POST(APIEndPointsConstants.FETCH_COUPON_PROMO)
    suspend fun getCouponAndPromo(@Body requestBody: RequestBody): Response<BaseResponse<CouponAndPromos>>


}