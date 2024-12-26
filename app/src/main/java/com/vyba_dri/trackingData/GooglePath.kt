package com.vyba_dri.trackingData


import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.vyba_dri.R
import com.vyba_dri.SaloneDriver
import com.vyba_dri.model.dataclassses.clientConfig.ClientConfigDC
import com.vyba_dri.util.SharedPreferencesManager
import com.vyba_dri.view.ui.home_drawer.ui.home.HomeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import kotlin.math.abs
import kotlin.math.sign


var polyLine: List<LatLng>? = null
var mainPolyLine: List<LatLng>? = null
private var srcMarker: Marker? = null
private var driverMarker: Marker? = null
private var destMarker: Marker? = null
private var polyline: Polyline? = null

data class StepInstruction(
    val startLocation: LatLng? = null,
    val endLocation: LatLng? = null,
    val instruction: String? = null,
    val maneuver: String? = null
)

/**Show path on Map*/
fun Context.showPath(
    srcLat: LatLng,
    desLat: LatLng,
    mMap: GoogleMap?,
    @DrawableRes source: Int = R.drawable.car_icon,
    @DrawableRes destination: Int = R.drawable.new_location_placeholder,
    @DrawableRes via: Int = R.drawable.new_location_placeholder,
    wayPoints: ArrayList<LatLng> = ArrayList(),
    bearing: Double? = 0.0,
    valueIs: (PathModel) -> Unit = { _ -> }
) {
    try {
//        mMap?.clearMap()
        val pathModel = PathModel()
        val latLongB = LatLngBounds.Builder()

        val options = PolylineOptions()
        options.color(ContextCompat.getColor(this, R.color.theme))

        val url = getDirectionsUrl(srcLat, desLat, wayPoints)
        CoroutineScope(Dispatchers.IO).launch {
            val result = URL(url).readText()
            val pathResponse: PathResponse = Gson().fromJson(result, PathResponse::class.java)
            val steps = pathResponse.routes?.getOrNull(0)?.legs?.getOrNull(0)?.steps
            HomeFragment.stepsInstructionArrayList = (steps?.map {
                StepInstruction(
                    startLocation = it?.startLocation?.lat?.let { it1 ->
                        it.startLocation?.lng?.let { it2 ->
                            LatLng(
                                it1,
                                it2
                            )
                        }
                    },
                    endLocation = it?.endLocation?.lat?.let { it1 ->
                        it.endLocation?.lng?.let { it2 ->
                            LatLng(
                                it1,
                                it2
                            )
                        }
                    },
                    instruction = it?.htmlInstructions,
                    maneuver = it?.maneuver
                )
            } ?: emptyList()) as ArrayList<StepInstruction>
            CoroutineScope(Dispatchers.Main).launch {
                mMap?.clear()
                val parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                val routes = json.array<JsonObject>("routes")
                if (routes != null && routes.size > 0) {
                    pathModel.durationText =
                        pathResponse.routes?.get(0)?.legs?.get(0)?.duration?.text ?: ""
                    pathModel.durationMin =
                        (pathResponse.routes?.get(0)?.legs?.get(0)?.duration?.value ?: 0.0) / 60
                    pathModel.srcDesDistance =
                        (pathResponse.routes?.get(0)?.legs?.get(0)?.distance?.value?.toDouble()
                            ?: 0.0) / 1000
                    pathModel.srcDesDistanceInKm =
                        pathResponse.routes?.get(0)?.legs?.get(0)?.distance?.text ?: ""
                    val points = routes["legs"]["steps"][0] as JsonArray<JsonObject>
                    val polyPoints =
                        points.flatMap { decodePoly(it.obj("polyline")?.string("points") ?: "") }
                    latLongB.include(srcLat)
                    for (point in polyPoints) {
                        options.add(point)
                        latLongB.include(point)
                    }
                    mainPolyLine = polyPoints
                    if (mainPolyLine != polyPoints) {
                        mMap?.clear()
                    }
                    val bounds = latLongB.build()
                    if (mMap != null && srcLat.latitude != 0.0 && srcLat.longitude != 0.0) {
                        polyline = mMap.addPolyline(options)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                    if (mMap != null) {

//                        for (data in pathResponse.routes?.get(0)?.legs?.get(0)?.viaWaypoint
//                            ?: ArrayList()) {
//                            data?.location?.let { loc ->
//                                mMap.addMarker(
//                                    MarkerOptions().position(
//                                        LatLng(loc.lat ?: 0.0, loc.lng ?: 0.0)
//                                    ).icon(
//                                        vectorToBitmap(via)
//                                    ).anchor(0.5f, 1f)
//                                )
//                            }
//
//                        }

                        pathModel.srcMarker = mMap.addMarker(
                            MarkerOptions().position(
                                srcLat
                            ).apply {
                                icon(vectorToBitmap(source))
                                bearing?.toFloat()?.let { rotation(it) }
                                anchor(0.5f, 1f)
                            }
                        )

                        pathModel.desMarker = mMap.addMarker(
                            MarkerOptions().apply {
                                position(desLat)
                                icon(vectorToBitmap(destination))
                                anchor(0.5f, 1f)
                            }
                        )

                        srcMarker = pathModel.srcMarker
                        destMarker = pathModel.desMarker
                    }

                    valueIs(pathModel)
                }

            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}


/**Find Eta between two lat longs*/
fun Context.findEta(srcLat: LatLng, desLat: LatLng, valueIs: (PathModel) -> Unit) {

    try {
        val pathModel = PathModel()

        val url = getDirectionsUrl(srcLat, desLat)
        CoroutineScope(Dispatchers.IO).launch {
            val result = URL(url).readText()

            val pathResponse: PathResponse = Gson().fromJson(result, PathResponse::class.java)
            CoroutineScope(Dispatchers.Main).launch {
                if (pathResponse.routes != null && !pathResponse.routes.isNullOrEmpty()) {
                    pathModel.durationText =
                        pathResponse.routes?.get(0)?.legs?.get(0)?.duration?.text ?: ""
                    pathModel.durationMin =
                        (pathResponse.routes?.get(0)?.legs?.get(0)?.duration?.value ?: 0.0) / 60
                    pathModel.srcDesDistance =
                        (pathResponse.routes?.get(0)?.legs?.get(0)?.distance?.value?.toDouble()
                            ?: 0.0) / 1000
                    pathModel.srcDesDistanceInKm =
                        pathResponse.routes?.get(0)?.legs?.get(0)?.distance?.text ?: ""
                    valueIs(pathModel)
                }
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**Get url*/
private fun Context.getDirectionsUrl(
    src: LatLng, des: LatLng, markerPoints: ArrayList<LatLng> = ArrayList()
): String {
    if (SaloneDriver.googleMapKey.isEmpty())
        SaloneDriver.googleMapKey = SharedPreferencesManager.getModel<ClientConfigDC>(
            SharedPreferencesManager.Keys.CLIENT_CONFIG
        )?.googleMapKey ?: ""

    var url = "https://maps.googleapis.com/maps/api/directions/"
    val params = "&mode=driving&key=" + SaloneDriver.googleMapKey
    val output = "json"
    var parameters = ""

    if (markerPoints.isNotEmpty()) {
        var waypoints = ""
        for (i in 0 until markerPoints.size)//loop starts from 2 because 0 and 1 are already printed
        {
            waypoints =
                waypoints + "via:" + markerPoints[i].latitude + "," + markerPoints[i].longitude + "|"
        }
        waypoints = "&waypoints=optimize:true|$waypoints"
        parameters =
            "origin=${src.latitude.toString() + "," + src.longitude}$waypoints" + "&destination=${des.latitude.toString() + "," + des.longitude}"
        url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&$params"

    } else {
        parameters =
            "origin=${src.latitude.toString() + "," + src.longitude}" + "&destination=${des.latitude.toString() + "," + des.longitude}&$params"
        url += "$output?$parameters"

    }
    Log.e("PathUtil", "getDirectionsUrl: $url")
    return url
}

/**Decode poly line*/
private fun decodePoly(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = LatLng(
            lat.toDouble() / 1E5,
            lng.toDouble() / 1E5
        )
        poly.add(p)
    }
    return poly
}

/**VECTOR IMAGE TO BITMAP*/
fun Context.vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor? {
    ResourcesCompat.getDrawable(resources, id, null)?.let { vectorDrawable ->
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    } ?: return null
}

fun filterInstructions(currentPosition: LatLng): ArrayList<StepInstruction> {
    // Create a list to hold the filtered instructions
    val filteredInstructions = ArrayList<StepInstruction>()

    for (step in HomeFragment.stepsInstructionArrayList) {
        val start = step.startLocation?.latitude?.let { LatLng(it, step.startLocation.longitude) }
        val end = step.endLocation?.latitude?.let { LatLng(it, step.endLocation.longitude) }

        // Check if the current position is within the bounds of the start and end location
        val isOnPath = PolyUtil.isLocationOnPath(
            currentPosition,
            listOf(start, end),
            false,
            50.0
        )

        // If user is on the path, add the instruction to the filtered list
        if (isOnPath) {
            filteredInstructions.add(step)
        }
    }

    // Return the filtered instructions
    return filteredInstructions
}

/**Animate Driver*/

var oldDriverLat: Double? = null
var oldDriverLng: Double? = null
fun Context.animateDriver(
    driverLatitude: Double?,
    driverLongitude: Double?,
    bearing: Double?,
    googleMap: GoogleMap?,
    @DrawableRes source: Int = R.drawable.car_icon,
    @DrawableRes destination: Int = R.drawable.new_location_placeholder,
    @DrawableRes via: Int = R.drawable.new_location_placeholder,
    eta: (String) -> Unit = {}
) {
    if (oldDriverLat == driverLatitude && oldDriverLng == driverLongitude) {
        return
    }
    oldDriverLat = driverLatitude
    oldDriverLng = driverLongitude

    if (mainPolyLine != null) {
        val currentPosition = LatLng(driverLatitude ?: 0.0, driverLongitude ?: 0.0)

        val exceededTolerance = !PolyUtil.isLocationOnPath(
            LatLng(driverLatitude ?: 0.0, driverLongitude ?: 0.0),
            mainPolyLine,
            false,
            50.0
        )
        if (exceededTolerance) {
            showPath(
                srcLat = LatLng(driverLatitude ?: 0.0, driverLongitude ?: 0.0),
                desLat = destMarker?.position ?: LatLng(0.0, 0.0),
                mMap = googleMap,
                source = source,
                destination = destination,
                via = via, bearing = bearing
            ) {}
        } else {
            newAnimateCar(
                LatLng(driverLatitude ?: 0.0, driverLongitude ?: 0.0),
                (bearing ?: 0.0).toFloat(),
                googleMap
            ) {
                eta(it)
            }
            /*To filter travelled points from existed polyline*/
            val nearestPointIndex = mainPolyLine!!.indexOfFirst {
                PolyUtil.isLocationOnPath(currentPosition, listOf(it), false, 50.0)
            }
            if (nearestPointIndex != -1) {
                polyline?.remove()
                polyLine = null
                val filteredPoints = mainPolyLine!!.subList(nearestPointIndex, mainPolyLine!!.size)
                HomeFragment.temStepsInstructionArrayList = filterInstructions(currentPosition)
                val polylineOptions = PolylineOptions()
                    .color(ContextCompat.getColor(this, R.color.theme))
                    .addAll(filteredPoints)
                // Add a new polyline to the map
                if (polyline?.equals(filteredPoints) == false) {
                    googleMap?.addPolyline(polylineOptions).also {
                        polyline = it
                        polyLine = it?.points
                    }
                    Log.d(
                        "animateDriver",
                        "after Updated Polyline with ${polyline?.points?.size} points."
                    )
                }
            } else {
                // Logging for debugging
                Log.d("animateDriver", "No points found on the path for current position.")
            }
            eta("")
        }
    }
}

private fun Context.newAnimateCar(
    endPosition: LatLng,
    bearing: Float,
    googleMap: GoogleMap?,
    eta: (String) -> Unit
) {
    try {
        srcMarker?.let { marker ->
            val startPosition = marker.position
            // Set anchor and flat properties
            marker.isFlat = true
            marker.setAnchor(0.5f, 0.5f) // Center of the marker
            // Use ValueAnimator for smooth marker movement
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 1000 // Duration of animation in milliseconds
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                val latLng = LatLng(
                    newLerp(startPosition.latitude, endPosition.latitude, fraction.toDouble()),
                    newLerp(startPosition.longitude, endPosition.longitude, fraction.toDouble())
                )
                marker.position = latLng
                marker.rotation = bearing

                // Smooth camera movement
                if (fraction == 1f) {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng), 1000, null)
                }

            }
            valueAnimator.start()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun newLerp(a: Double, b: Double, t: Double): Double {
    return a + (b - a) * t
}

private interface LatLngInterpolator {
    fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
    class LinearFixed : LatLngInterpolator {
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            val lat = (b.latitude - a.latitude) * fraction + a.latitude
            var lngDelta = b.longitude - a.longitude
            if (abs(lngDelta) > 180) {
                lngDelta -= sign(lngDelta) * 360
            }
            val lng = lngDelta * fraction + a.longitude
            return LatLng(lat, lng)
        }
    }
}

private fun Context.animateCar(
    endPosition: LatLng,
    bearing: Float,
    googleMap: GoogleMap?,
    eta: (String) -> Unit
) {

    try {
        srcMarker?.let {
            val startPosition = it.position
            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(endPosition, 16f)
            )
            val endPosition = endPosition

            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 1000 // Duration of animation in milliseconds
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                val latLng = LatLng(
                    lerp(startPosition.latitude, endPosition.latitude, fraction.toDouble()),
                    lerp(startPosition.longitude, endPosition.longitude, fraction.toDouble())
                )
                it.position = latLng
                it.rotation = bearing
            }
            valueAnimator.start()
        }


    } catch (e: Exception) {
        e.printStackTrace()
    }

}

private fun lerp(start: Double, end: Double, fraction: Double): Double {
    return start + fraction * (end - start)
}

fun findNearestPoint(test: LatLng, target: List<LatLng>): LatLng {
    var distance = -1.0
    var minimumDistancePoint = test
    for (i in target.indices) {
        val point = target[i]
        var segmentPoint = i + 1
        if (segmentPoint >= target.size) {
            segmentPoint = 0
        }
        val currentDistance = PolyUtil.distanceToLine(test, point, target[segmentPoint])
        if (distance == -1.0 || currentDistance < distance) {
            distance = currentDistance
            minimumDistancePoint = findNearestPoint(test, point, target[segmentPoint])
        }
    }
    return minimumDistancePoint
}

private fun Context.bitmapDescriptorFromVector(): BitmapDescriptor? {
    return ContextCompat.getDrawable(this, R.drawable.car_icon)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

fun findNearestPoint(p: LatLng, start: LatLng, end: LatLng): LatLng {
    if (start == end) {
        return start
    }
    val s0lat = Math.toRadians(p.latitude)
    val s0lng = Math.toRadians(p.longitude)
    val s1lat = Math.toRadians(start.latitude)
    val s1lng = Math.toRadians(start.longitude)
    val s2lat = Math.toRadians(end.latitude)
    val s2lng = Math.toRadians(end.longitude)
    val s2s1lat = s2lat - s1lat
    val s2s1lng = s2lng - s1lng
    val u =
        ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng) / (s2s1lat * s2s1lat + s2s1lng * s2s1lng)
    if (u <= 0) {
        return start
    }
    return if (u >= 1) {
        end
    } else LatLng(
        start.latitude + u * (end.latitude - start.latitude),
        start.longitude + u * (end.longitude - start.longitude)
    )
}

/**Navigate to Map*/

/*
fun Context.navigateToMap(
    source: LatLng,
    destination: LatLng
) {
    try {
        when {
            AppController.globalSettings?.navigateOn?.contains("GOOGLE") == true &&
                    AppController.globalSettings?.navigateOn?.contains("WAZE") == true -> {
                showMapDialogWithLatLong(
                    source.latitude,
                    source.longitude,
                    destination.latitude,
                    destination.longitude
                )
            }
            AppController.globalSettings?.navigateOn?.contains("GOOGLE") == true -> {
                navigateToMapWithLatLong(
                    source.latitude,
                    source.longitude,
                    destination.latitude,
                    destination.longitude
                )
            }
            AppController.globalSettings?.navigateOn?.contains("WAZE") == true -> {
                navigateToWazeWithLatLong(
                    source.latitude,
                    source.longitude,
                    destination.latitude,
                    destination.longitude
                )
            }
        }
        */
/* val intent = Intent(
             Intent.ACTION_VIEW,
             Uri.parse("http://maps.google.com/maps?saddr=${source.latitude},${source.longitude}&daddr=${destination.latitude},${destination.longitude}")
         )
         startActivity(intent)*//*

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
*/

/*fun Context.showMapDialogWithLatLong(
    sourceLatitude: Double,
    sourceLongitude: Double,
    destinationLatitude: Double,
    destinationLongitude: Double
) {
    try {
        (this as MapActivity).supportFragmentManager.let {

            CommonBottomSheet(R.layout.map_type) { view, dialog ->
                val layoutView = MapTypeBinding.bind(view)

                layoutView.btnGoogleMap.setOnClickListener {
                    navigateToMapWithLatLong(
                        sourceLatitude,
                        sourceLongitude,
                        destinationLatitude,
                        destinationLongitude
                    )
                    dialog.dismiss()
                }

                layoutView.btnWazeMap.setOnClickListener {
                    navigateToWazeWithLatLong(
                        sourceLatitude,
                        sourceLongitude,
                        destinationLatitude,
                        destinationLongitude
                    )
                    dialog.dismiss()
                }

            }.show(it, "")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}*/

private fun Context.navigateToMapWithLatLong(
    sourceLatitude: Double,
    sourceLongitude: Double,
    destinationLatitude: Double,
    destinationLongitude: Double
) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?saddr=$sourceLatitude,$sourceLongitude&daddr=$destinationLatitude,$destinationLongitude")
        )
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

data class PathModel(
    var srcMarker: Marker? = null,
    var desMarker: Marker? = null,
    var durationText: String? = null,
    var durationMin: Double? = null,
    var srcDesDistance: Double? = null,
    var srcDesDistanceInKm: String? = null,
)

/*private fun getMarkerBitmapFromView(eta: String): BitmapDescriptor? {
    val customMarkerView: View =
        (HomeActivity.context.get()
            ?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.view_custom_marker,
            null
        )

    customMarkerView.findViewById<TextView>(R.id.tvTime).text =
        Html.fromHtml(eta.replace(" ", "<br>"))
    customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    customMarkerView.layout(
        0,
        0,
        customMarkerView.measuredWidth,
        customMarkerView.measuredHeight
    )
    customMarkerView.buildDrawingCache()
    val returnedBitmap = Bitmap.createBitmap(
        customMarkerView.measuredWidth, customMarkerView.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(returnedBitmap)
    canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
    val drawable: Drawable = customMarkerView.background
    drawable.draw(canvas)
    customMarkerView.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(returnedBitmap)
}*/

fun GoogleMap.clearMap() {
    polyLine = null
    polyline = null
    srcMarker?.remove()
    destMarker?.remove()
    srcMarker = null
    destMarker = null
    oldDriverLat = null
    oldDriverLng = null
    this.clear()
}