package com.superapp_driver.view.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.superapp_driver.R
import com.superapp_driver.util.AppUtils
import com.superapp_driver.viewmodel.base.BaseViewModel
import org.greenrobot.eventbus.EventBus

abstract class BaseFragment<MyDataBinding : ViewDataBinding> : Fragment() {
    private var fragmentBaseViewModel: BaseViewModel? = null
    abstract fun initialiseFragmentBaseViewModel()
    protected var screenWidth = 0

    private lateinit var mViewDataBinding: MyDataBinding
    lateinit var mRootView: View
    private var mActivity: FragmentActivity? = null


    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        mRootView = mViewDataBinding.root

        return mRootView
    }

    open fun getViewDataBinding(): MyDataBinding {
        return mViewDataBinding
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialiseFragmentBaseViewModel()
//        (activity as AppCompatActivity?)!!.delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as BaseActivity<*>).lifecycleScope.launchWhenCreated {
            screenWidth = (activity as BaseActivity<*>).getScreenWidth()
        }
    }


    /**
     *
     * @param viewModel setting up the any [androidx.lifecycle.ViewModel] extending [BaseViewModel]
     * The main purpose to access any functionality for [BaseViewModel] like showing and hiding [LoadingDialog]
     *
     */
    protected fun setFragmentBaseViewModel(viewModel: BaseViewModel) {
        this.fragmentBaseViewModel = viewModel
    }

    fun showToastShort(message: String) {
        activity?.let {
            if (it is BaseActivity<*>)
                it.showToastShort(message)
        }
    }

    fun getDeviceId(): String {
        return AppUtils.getDeviceId()
    }

    fun showToastLong(message: String) {
        activity?.let {
            if (it is BaseActivity<*>)
                it.showToastLong(message)
        }
    }

    fun logoutUser() {
//        (activity as BaseActivity).logoutUser()
    }

    open fun showProgressDialog(type: Int = LoaderType.NORMAL, msg: String = "") {
        (activity as BaseActivity<*>?)?.showProgressDialog(type, msg)
    }

    open fun hideProgressDialog(type: Int = LoaderType.NORMAL, msg: String = "") {
        (activity as BaseActivity<*>?)?.hideProgressDialog(type, msg)
    }


    fun showNoInternetDialog() {
//        DialogUtils.showNoInternetDialog(requireContext())
    }


    fun screenMode(fullScreen: Boolean) {
        if (fullScreen) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//            val flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            requireActivity().getWindow().getDecorView().setSystemUiVisibility(flags)
        } else {
            requireActivity().window.addFlags(SYSTEM_UI_FLAG_VISIBLE)
        }
    }

    fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, requireView()).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun showSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        WindowInsetsControllerCompat(requireActivity().window, requireView()).let { controller ->
            controller.show(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars())
        }
    }

    fun backPressed(backLogic: () -> Unit) {
        activity?.onBackPressedDispatcher?.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backLogic()
                }
            })
    }

    fun noInternetToast() {
        showToastShort(getString(R.string.no_net_connection))
    }

    fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun unregisterEventBus() {
        EventBus.getDefault().unregister(this)
    }

    open fun hideKeyboard() {
        hideKeyboard(requireActivity())
    }

    private fun hideKeyboard(activity: Activity) {
        val inputManager = activity
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun showKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


}