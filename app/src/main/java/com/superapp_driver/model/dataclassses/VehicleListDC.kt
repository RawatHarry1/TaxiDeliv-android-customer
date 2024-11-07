package com.superapp_driver.model.dataclassses


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class VehicleListDC(
    @SerializedName("docStatus")
    val docStatus: String? = null,
    @SerializedName("vehicle_array")
    val vehicleArray: List<VehicleArray>? = null
) : Parcelable {
    @Keep
    @Parcelize
    data class VehicleArray(
        @SerializedName("brand")
        val brand: String? = null,
        @SerializedName("city_id")
        val cityId: String? = null,
        @SerializedName("color")
        val color: String? = null,
        @SerializedName("color_id")
        val colorId: String? = null,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("door_id")
        val doorId: String? = null,
        @SerializedName("driver_id")
        val driverId: String? = null,
        @SerializedName("driver_vehicle_mapping_id")
        val driverVehicleMappingId: String? = null,
        @SerializedName("driver_vehicle_mapping_status")
        val driverVehicleMappingStatus: String? = null,
        @SerializedName("id")
        val id: String? = null,
        @SerializedName("model_id")
        val modelId: String? = null,
        @SerializedName("model_name")
        val modelName: String? = null,
        @SerializedName("no_of_doors")
        val noOfDoors: String? = null,
        @SerializedName("no_of_seat_belts")
        val noOfSeatBelts: String? = null,
        @SerializedName("no_of_seats")
        val noOfSeats: String? = null,
        @SerializedName("reason")
        val reason: String? = null,
        @SerializedName("seat_belt_id")
        val seatBeltId: String? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null,
        @SerializedName("vehicle_id")
        val vehicleId: String? = null,
        @SerializedName("vehicle_image")
        val vehicleImage: String? = null,
        @SerializedName("vehicle_make_id")
        val vehicleMakeId: String? = null,
        @SerializedName("vehicle_no")
        val vehicleNo: String? = null,
        @SerializedName("vehicle_online")
        val vehicleOnline: String? = null,
        @SerializedName("vehicle_status")
        val vehicleStatus: String? = null,
        @SerializedName("vehicle_type")
        val vehicleType: String? = null,
        @SerializedName("vehicle_year")
        val vehicleYear: String? = null,
        @SerializedName("make_image")
        val makeImage: String? = null,
        @SerializedName("vehicle_type_name")
        val vehicleTypeName: String? = null
    ) : Parcelable
}