package com.salonedriver.view.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.salonedriver.R
import com.salonedriver.customClasses.singleClick.setOnSingleClickListener
import com.salonedriver.databinding.ActivityVehicleInfoBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.arrayAdapter
import com.salonedriver.util.fetchYearsList
import com.salonedriver.util.getValue
import com.salonedriver.util.vehicleModelAdapter
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class VehicleInfo : BaseActivity<ActivityVehicleInfoBinding>() {


    lateinit var binding: ActivityVehicleInfoBinding
    private val viewModel by viewModels<OnBoardingVM>()

    override fun getLayoutId(): Int {
        return R.layout.activity_vehicle_info
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        clickHandler()
        setVehicleName()
        observeCityVehicles()
        observeVehicleUpdate()
        viewModel.getCityVehicles(
            SharedPreferencesManager.getModel<UserDataDC>(
                SharedPreferencesManager.Keys.USER_DATA
            )?.login?.city.orEmpty()
        )
    }


    private fun setVehicleName() {
        try {
            val cityList =
                SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.cityList
                    ?: ArrayList()
            binding.etVehicleCountry.setText(cityList.find {
                it.cityId == SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.city
            }?.cityName.orEmpty())
            viewModel.cityId = cityList.find {
                it.cityId == SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.city
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
                })
            }
        }
        binding.ivBackElectric.setOnClickListener { finish() }

        binding.etVehicleCountry.setOnClickListener {
            val cityList =
                SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.cityList
                    ?: ArrayList()
            arrayAdapter(autoCompleteTextView = binding.etVehicleCountry,
                list = cityList.map { it.cityName.orEmpty() }) {
                viewModel.cityId = cityList[it].cityId.orEmpty()
                binding.etVehicleType.setText("")
                binding.etVehicleModel.setText("")
                viewModel.getCityVehicles(
                    SharedPreferencesManager.getModel<UserDataDC>(
                        SharedPreferencesManager.Keys.USER_DATA
                    )?.login?.city.orEmpty()
                )
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

}