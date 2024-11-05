package com.superapp_customer.view.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.superapp_customer.R
import com.superapp_customer.amazonS3.AmazonS3
import com.superapp_customer.dialogs.CustomProgressDialog
import com.superapp_customer.util.AppUtils
import com.superapp_customer.util.showSnackBar
import com.superapp_customer.viewmodel.base.BaseViewModel
import org.greenrobot.eventbus.EventBus


abstract class BaseActivity<MyDataBinding : ViewDataBinding> : AppCompatActivity() {
    private lateinit var baseActivityBinding: MyDataBinding
    private var baseViewModel: BaseViewModel? = null
    private var dialogView: Dialog? = null
    val amazonS3 by lazy { AmazonS3() }
    private lateinit var mInstance: CustomProgressDialog


    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStatusBarTransparent()
//        baseActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_base)
//        adjustFontScale()
        //setLayout()
        mInstance = CustomProgressDialog()
        performDataBinding()
        // For Disabling screenshots
        //      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

    }

    private fun performDataBinding() {
        baseActivityBinding = DataBindingUtil.setContentView(this, getLayoutId())
    }

    open fun getViewDataBinding(): MyDataBinding {
        return baseActivityBinding
    }

    /**
     *
     * @param viewModel setting up the any [androidx.lifecycle.ViewModel] extending [BaseViewModel]
     * The main purpose to access any functionality for [BaseViewModel] like showing and hiding [LoadingDialog]
     *
     */
//    fun setBaseViewModel(viewModel: BaseViewModel) {
//        this.baseViewModel = viewModel
//        baseViewModel?.loadingState?.observe(this, Observer { loadingState ->
//            if (loadingState != null) {
//                when (loadingState) {
//                    is LoadingState.LOADING -> showProgressDialog(
//                        loadingState.type,
//                        loadingState.msg
//                    )
//                    is LoadingState.LOADED -> hideProgressDialog(
//                        loadingState.type,
//                        loadingState.msg
//                    )
//                }
//            }
//        })
//    }

    private fun setStatusBarTransparent() {
        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.statusBarColor = Color.TRANSPARENT
    }


    /**
     * Method is used to set the layout in the Base Activity.
     * Layout params of the inserted child is match parent
     */
    /*private fun setLayout() {
        if (resourceId != -1) {
            removeLayout()
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT
                , RelativeLayout.LayoutParams.MATCH_PARENT
            )
            val layoutInflater =
                getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(resourceId, null)
            baseActivityBinding.rlBaseContainer.addView(view, layoutParams)
        }
    }*/

    /**
     * hides keyboard onClick anywhere besides edit text
     *
     * @param ev
     * @return
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) && view is EditText && !view.javaClass.name.startsWith(
                "android.webkit."
            )
        ) {
            val scrcoords = IntArray(2)
            view.getLocationOnScreen(scrcoords)
            val x = ev.rawX + view.getLeft() - scrcoords[0]
            val y = ev.rawY + view.getTop() - scrcoords[1]
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) (this.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager).hideSoftInputFromWindow(
                this.window.decorView.applicationWindowToken,
                0
            )
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * Method is used by the sub class for passing the id of the layout ot be inflated in the relative layout
     *
     * @return id of the resource to be inflated
     */
    //protected abstract val resourceId: Int
//
    fun addFragment(container: Int, fragment: BaseFragment<MyDataBinding>?, tag: String?) {
        if (supportFragmentManager.findFragmentByTag(tag) == null) supportFragmentManager.beginTransaction()
            .add(container, fragment!!, tag)
            .commit()
    }
//
//    fun addFragmentWithBackstack(
//        container: Int,
//        fragment: BaseFragment?,
//        tag: String?
//    ) {
//        supportFragmentManager.beginTransaction()
//            .add(container, fragment!!, tag)
//            .addToBackStack(tag)
//            .commit()
//    }
//
//    fun replaceFragment(container: Int, fragment: BaseFragment?, tag: String?) {
//        if (supportFragmentManager.findFragmentByTag(tag) == null) supportFragmentManager.beginTransaction()
//            .replace(container, fragment!!, tag)
//            .commit()
//    }
//
//    fun showFragment(container: Int, fragment: BaseFragment?, tag: String?) {
//        val fm: FragmentManager = supportFragmentManager
//        fm.beginTransaction()
////            .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
//            .show(fragment!!)
//            .commit()
//    }
//
//    fun replaceFragmentWithBackstack(
//        container: Int,
//        fragment: BaseFragment?,
//        tag: String?
//    ) {
//        supportFragmentManager.beginTransaction()
//            .replace(container, fragment!!, tag)
//            .addToBackStack(tag)
//            .commit()
//    }
//
//    fun replaceFragmentWithBackstackWithStateLoss(
//        container: Int,
//        fragment: BaseFragment?,
//        tag: String?
//    ) {
//        supportFragmentManager.beginTransaction()
//            .replace(container, fragment!!, tag)
//            .addToBackStack(tag)
//            .commitAllowingStateLoss()
//    }

    /**
     * This method is used to remove the view already present as a child in relative layout.
     */
    /*private fun removeLayout() {
        if (baseActivityBinding.rlBaseContainer.childCount >= 1) baseActivityBinding.rlBaseContainer.removeAllViews()
    }*/

    /**
     * hiding the progress dialog
     *
     * @param type type of dialog or kind of sheemer effect
     * @param msg
     */
//    open fun showProgressDialog(type: Int, msg: String) {
//        if (!isDestroyed) {
//            when (type) {
//                LoaderType.NORMAL -> {
//                    dialogView = Dialog(this)
//                    dialogView?.requestWindowFeature(Window.FEATURE_NO_TITLE)
//
//                    val view = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
//                    dialogView?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                    dialogView?.setContentView(view)
//                    dialogView?.setCancelable(false)
//                    dialogView?.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                    //view.tvProgressHeading.text = msg
//                    dialogView?.show()
//                }
//            }
//        }
//    }

    /**
     * hiding the progress dialog
     *
     * @param type type of dialog or kind of sheemer effect
     * @param msg
     */
    open fun hideProgressDialog() {
        try {
            mInstance.dismiss()
        } catch (e: Exception) {

        }
    }

    open fun showProgressDialog() {
        if (!isDestroyed) {
            mInstance.show(this@BaseActivity)
        }
    }
//    fun showNoInternetDialog() {
//        DialogUtils.showNoInternetDialog(this)
//    }

    fun showToastShort(message: String) {
        showSnackBar(message)
    }

    fun showToastLong(message: String) {
        showSnackBar(message)
    }



    /**
     * function to get height of bottom navigation
     *
     * @return height
     */
    open fun bottomNavigationHeight(): Int {
        val resourceId =
            resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    fun popFragment() {
        supportFragmentManager.popBackStackImmediate()
    }

//    fun getDeviceId(): String {
//        return AppUtils.getDeviceId()
//    }

    /*fun logoutUser() {
        SharedPreferencesManager.clearAllPreferences()
        val intent = Intent(VenusApp.appContext, OnBordingActivity::class.java)
        intent.putExtra(IntentConstants.SCREEN_TYPE, IntentConstants.KEY_LOGOUT)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity()

    }*/


    fun getScreenWidth(): Int = AppUtils.getScreenWidth(this)

    fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, this.baseActivityBinding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun showSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, this.baseActivityBinding.root).let { controller ->
            controller.show(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars())
        }
    }

    fun adjustFontScale() {
        val configuration = resources.configuration
        configuration.fontScale = 1f
        val metrics = resources.displayMetrics
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        baseContext.resources.updateConfiguration(configuration, metrics)
    }

    fun noInternetToast() {
        showToastShort(getString(R.string.no_net_connection))
    }

    fun unregisterEventBus() {
        //EventBus.getDefault().unregister(this)
    }

    fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            //EventBus.getDefault().register(this)
        }
    }

    override fun onStart() {
        super.onStart()
        //EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        //EventBus.getDefault().unregister(this)
    }


}