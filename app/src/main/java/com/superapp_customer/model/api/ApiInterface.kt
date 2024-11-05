package com.superapp_customer.model.api

import com.google.gson.JsonElement
import com.salonedriver.model.dataclassses.notificationDC.NotificationDC
import com.superapp_customer.BuildConfig
import com.superapp_customer.model.dataClass.AboutAppDC
import com.superapp_customer.model.dataClass.CardData
import com.superapp_customer.model.dataClass.CouponAndPromos
import com.superapp_customer.model.dataClass.CouponResponse
import com.superapp_customer.model.dataClass.CreateProfileResponse
import com.superapp_customer.model.dataClass.ScheduleList
import com.superapp_customer.model.dataClass.SetUpIntentResponse
import com.superapp_customer.model.dataClass.ShowMessage
import com.superapp_customer.model.dataClass.UploadPackageResponse
import com.superapp_customer.model.dataClass.WalletTransaction
import com.superapp_customer.model.dataClass.addedAddresses.AddedAddressData
import com.superapp_customer.model.dataClass.base.BaseResponse
import com.superapp_customer.model.dataClass.base.ClientConfig
import com.superapp_customer.model.dataClass.fareEstimate.FareEstimateDC
import com.superapp_customer.model.dataClass.fetchOngoingTrip.FetchOngoingTripDC
import com.superapp_customer.model.dataClass.findDriver.FindDriverDC
import com.superapp_customer.model.dataClass.findNearDriver.FindNearDriverDC
import com.superapp_customer.model.dataClass.requestTrip.RequestTripDC
import com.superapp_customer.model.dataClass.tripsDC.RideSummaryDC
import com.superapp_customer.model.dataClass.tripsDC.TripListDC
import com.superapp_customer.model.dataClass.userData.UserDataDC
import com.superapp_customer.util.constants.APIEndPointsConstants
import com.superapp_customer.util.constants.APIEndPointsConstants.FIND_NEAR_DRIVER
import com.superapp_customer.util.constants.APIEndPointsConstants.GET_NOTIFICATIONS
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
    @Multipart
    @POST(APIEndPointsConstants.UPLOAD_PACKAGE_IMAGE)
    suspend fun uploadDocument(
        @Part multipartBody: MultipartBody.Part? = null
    ): Response<BaseResponse<UploadPackageResponse>>

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
    ): Response<BaseResponse<CreateProfileResponse>>


    @POST(APIEndPointsConstants.LOGIN_VIA_ACCESS_TOKEN)
    suspend fun loginViaAccessToken(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<UserDataDC>>

    @POST(APIEndPointsConstants.DELETE_ACCOUNT)
    suspend fun deleteAccount(): Response<BaseResponse<Any>>

    @DELETE(APIEndPointsConstants.LOGOUT)
    suspend fun logout(): Response<BaseResponse<Any>>


    @GET(APIEndPointsConstants.INFORMATION_URL)
    suspend fun aboutApp(
        @Query("operatorId") operatorId: String,
        @Query("cityId") cityId: String,
        @Query("pageType") type: Int
    ): Response<BaseResponse<AboutAppDC>>

    @POST(APIEndPointsConstants.GET_TRANSACTIONS)
    suspend fun getTransactions(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<WalletTransaction>>

    @POST(APIEndPointsConstants.ADD_MONEY)
    suspend fun addMoney(
        @Body requestBody: RequestBody
    ): Response<BaseResponse<Any>>


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
    ): Response<BaseResponse<CouponResponse>>

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
        @Field("start_from") startFrom: String = "0",
        @Field("request_ride_type") serviceType: Int? = null
    ): Response<BaseResponse<List<TripListDC>>>

    @FormUrlEncoded
    @POST(APIEndPointsConstants.ADD_CARD)
    suspend fun addCard(
        @Field("client_secret") secret: String = ""
    ): Response<BaseResponse<SetUpIntentResponse>>

    @FormUrlEncoded
    @POST(APIEndPointsConstants.CONFIRM_CARD)
    suspend fun confirmCard(
        @Field("client_secret") secret: String = "",
        @Field("setup_intent_id") id: String = ""
    ): Response<BaseResponse<Any>>

    @FormUrlEncoded
    @POST(APIEndPointsConstants.DELETE_CARD)
    suspend fun deleteCard(
        @Field("card_id") id: String = ""
    ): Response<BaseResponse<Any>>

    @FormUrlEncoded
    @POST(APIEndPointsConstants.GET_ALL_SCHEDULE_RIDES)
    suspend fun getAllScheduleRides(
        @Field("request_ride_type") serviceType: Int? = null
    ): Response<BaseResponse<List<ScheduleList>>>

    @GET(APIEndPointsConstants.GET_TRIP_SUMMARY)
    suspend fun getTripSummary(
        @Query("tripId") tripId: String,
        @Query("driverId") driverId: String
    ): Response<BaseResponse<RideSummaryDC>>

    @GET(APIEndPointsConstants.GET_CARDS)
    suspend fun getCards(
        @Query("payment_method_type") type: Int
    ): Response<BaseResponse<List<CardData>>>


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