package com.vyba_dri.view.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vyba_dri.R
import com.vyba_dri.customClasses.singleClick.setOnSingleClickListener
import com.vyba_dri.databinding.ActivityVehicleInfoBinding
import com.vyba_dri.databinding.ItemAppTypesBinding
import com.vyba_dri.model.api.observeData
import com.vyba_dri.model.dataclassses.clientConfig.ClientConfigDC
import com.vyba_dri.model.dataclassses.clientConfig.OperatorAvailablity
import com.vyba_dri.model.dataclassses.userData.UserDataDC
import com.vyba_dri.util.SharedPreferencesManager
import com.vyba_dri.util.arrayAdapter
import com.vyba_dri.util.fetchYearsList
import com.vyba_dri.util.getValue
import com.vyba_dri.util.vehicleModelAdapter
import com.vyba_dri.view.base.BaseActivity
import com.vyba_dri.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class VehicleInfo : BaseActivity<ActivityVehicleInfoBinding>() {
    lateinit var binding: ActivityVehicleInfoBinding
    private val viewModel by viewModels<OnBoardingVM>()
    private val appTypeArrayList = ArrayList<OperatorAvailablity>()
    private lateinit var appTypeAdapter: AppTypeAdapter
    override fun getLayoutId(): Int {
        return R.layout.activity_vehicle_info
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        clickHandler()
        observeCityVehicles()
        observeVehicleUpdate()
        appTypeAdapter = AppTypeAdapter()
        SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
            ?.let {
                if(it.enabledService == 3) {
                    appTypeArrayList.clear()
                    appTypeArrayList.addAll(it.operatorAvailablity.orEmpty())
                    if (appTypeArrayList.isNotEmpty()) {
                        SharedPreferencesManager.put(
                            SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                            appTypeArrayList[0].id ?: 1
                        )
                    }
                    binding.rvDriverType.adapter = appTypeAdapter
                }
                else
                {
                    binding.rvDriverType.isVisible = false
                    binding.tvSelectDriverType.isVisible = false
                    SharedPreferencesManager.put(
                        SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                        it.enabledService ?: 1
                    )
                }
            }

        if (SharedPreferencesManager.getModel<UserDataDC>(
                SharedPreferencesManager.Keys.USER_DATA
            )?.login?.city.isNullOrEmpty()
        ) {
            showSnackBar("Your Are Not In Operational Area")
        } else {
            viewModel.getCityVehicles(
                SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.city.orEmpty(), rideType = SharedPreferencesManager.getInt(
                    SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID
                ) ?: 0
            )
            setCountryName()
        }

    }


    private fun setCountryName() {
        try {
            val cityList =
                SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.cityList
                    ?: ArrayList()
            binding.etVehicleCountry.setText(cityList.find {
                it.cityId == SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.city.orEmpty()
            }?.cityName.orEmpty())
            viewModel.cityId = cityList.find {
                it.cityId == SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.city.orEmpty()
            }?.cityId.orEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun clickHandler() {
        binding.tvSubmitVehicle.setOnSingleClickListener {
            if (validations()) {
                viewModel.updateVehiclesInfo(jsonObject = JSONObject().apply {
                    put("vehicleYear", binding.etVehicleMake.getValue())
                    put("vehicleNo", binding.etVehiclePlate.getValue())
                    put(
                        "vehicleMakeId",
                        viewModel.vehicleList.find { it.isSelected == true }?.id.toString()
                    )
                    put(
                        "colorId", viewModel.colorList.find { it.isSelected == true }?.id.toString()
                    )
                    put("city_id", viewModel.cityId)
                    put(
                        "request_ride_type",
                        SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)
                    )
                })
            }
        }
        binding.ivBackElectric.setOnClickListener { finish() }

        binding.etVehicleCountry.setOnClickListener {

            if (SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.city.isNullOrEmpty()
            ) {
                showSnackBar("Your Are Not In Operational Area")
            } else {
                val cityList =
                    SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.cityList
                        ?: ArrayList()
                val filteredCityList = cityList.filter { city ->
                    city.operatorAvailable?.contains(
                        SharedPreferencesManager.getInt(
                            SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID
                        )
                    ) == true
                } ?: emptyList()
                arrayAdapter(autoCompleteTextView = binding.etVehicleCountry,
                    list = filteredCityList.map { it.cityName.orEmpty() }) {
                    viewModel.cityId = filteredCityList[it].cityId.orEmpty()
                    binding.etVehicleType.setText("")
                    binding.etVehicleModel.setText("")
                    viewModel.getCityVehicles(
                        viewModel.cityId, SharedPreferencesManager.getInt(
                            SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID
                        ) ?: 0
                    )
                }
            }
        }

        binding.etVehicleType.setOnClickListener {
            arrayAdapter(autoCompleteTextView = binding.etVehicleType,
                list = viewModel.vehicleType.map { it.vehicleTypeName.orEmpty() }) {
                val data = viewModel.vehicleType[it]
                viewModel.vehicleType.map {
                    it.isSelected = it == data
                }
                binding.etVehicleModel.setText("")
                viewModel.vehicleList.map { it.isSelected = false }
            }
        }

        binding.etVehicleModel.setOnClickListener {
            val list =
                viewModel.vehicleList.filter { it.vehicleType == viewModel.vehicleType.find { it.isSelected }?.vehicleType }
            vehicleModelAdapter(spinner = binding.spinner, list = list.map {
                Pair(
                    it.modelName.orEmpty(), it.brand.orEmpty()
                )
            }) {
                val data = list[it]
                binding.etVehicleModel.setText(data.modelName.orEmpty())
                viewModel.vehicleList.map {
                    it.isSelected = it == data
                }
            }
        }


        binding.etVehicleColor.setOnClickListener {
            arrayAdapter(autoCompleteTextView = binding.etVehicleColor,
                list = viewModel.colorList.map { it.value.orEmpty() }) {
                val data = viewModel.colorList[it]
                viewModel.colorList.map {
                    it.isSelected = it == data
                }
            }
        }

        binding.etVehicleMake.setOnClickListener {
            arrayAdapter(autoCompleteTextView = binding.etVehicleMake, list = fetchYearsList())
        }
    }


    private fun validations(): Boolean = when {
        binding.etVehicleCountry.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_select_vehicle_country))
            false
        }

        binding.etVehicleType.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_select_vehicle_type))
            false
        }

        binding.etVehicleMake.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_select_vehicle_make))
            false
        }

        binding.etVehicleMake.getValue().length != 4 -> {
            showErrorMessage(getString(R.string.please_enter_correct_year))
            false
        }

        binding.etVehicleModel.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_select_vehicle_model))
            false
        }

        binding.etVehicleColor.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_select_vehicle_color))
            false
        }

        binding.etVehiclePlate.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_enter_licence_plate))
            false
        }

        else -> true
    }


    private fun observeCityVehicles() = viewModel.cityVehicles.observeData(this, onLoading = {
        showProgressDialog()
    }, onError = {
        hideProgressDialog()
        showErrorMessage(this)
    }, onSuccess = {
        hideProgressDialog()
        viewModel.vehicleType.clear()
        viewModel.vehicleType.addAll(this?.vehicleType ?: emptyList())
        viewModel.colorList.clear()
        viewModel.colorList.addAll(this?.colors ?: emptyList())
        viewModel.vehicleList.clear()
        viewModel.vehicleList.addAll(this?.vehicles ?: emptyList())
    })


    private fun observeVehicleUpdate() =
        viewModel.updateVehiclesInfo.observeData(this, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                ?.let {
                    it.login?.registrationStepCompleted?.isVehicleInfoCompleted = true
                    SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, it)
                }
            startActivity(
                Intent(
                    this@VehicleInfo, UploadDocuments::class.java
                )
            )
        }, onError = {
            hideProgressDialog()
            showErrorMessage(this)
        })

    inner class AppTypeAdapter : RecyclerView.Adapter<AppTypeAdapter.AppTypeViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AppTypeAdapter.AppTypeViewHolder {
            return AppTypeViewHolder(
                ItemAppTypesBinding.inflate(
                    LayoutInflater.from(this@VehicleInfo),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: AppTypeAdapter.AppTypeViewHolder, position: Int) {
            holder.bind(appTypeArrayList[position])
        }

        override fun getItemCount(): Int {
            return appTypeArrayList.size
        }

        inner class AppTypeViewHolder(private val bindingItem: ItemAppTypesBinding) :
            RecyclerView.ViewHolder(bindingItem.root) {

            fun bind(data: OperatorAvailablity) {
                if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == data.id) {
                    bindingItem.cardViewRoot.strokeWidth =
                        4 // Set stroke width to highlight the selected card
                    bindingItem.cardViewRoot.strokeColor =
                        ContextCompat.getColor(this@VehicleInfo, R.color.theme)
                } else {
                    bindingItem.cardViewRoot.strokeWidth = 0 // Remove stroke for unselected cards
                }

                bindingItem.tvTaxiBooking.text = data.name
                Glide.with(this@VehicleInfo).load(data.image)
                    .error(R.drawable.taxi_icon).into(bindingItem.ivAppImage)

                bindingItem.cardViewRoot.setOnSingleClickListener {
                    binding.etVehicleCountry.setText("")
                    binding.etVehicleType.setText("")
                    binding.etVehicleModel.setText("")
                    binding.etVehicleColor.setText("")
                    SharedPreferencesManager.put(
                        SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                        data.id ?: 0
                    )
                    notifyDataSetChanged()
                }
            }
        }
    }

}