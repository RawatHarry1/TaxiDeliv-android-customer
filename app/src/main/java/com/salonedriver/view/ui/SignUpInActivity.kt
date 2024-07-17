package com.salonedriver.view.ui

import android.content.Intent
import android.os.Bundle
import com.salonedriver.R
import com.salonedriver.databinding.ActivitySignUpInBinding
import com.salonedriver.view.base.BaseActivity

class SignUpInActivity : BaseActivity<ActivitySignUpInBinding>() {

    private lateinit var binding: ActivitySignUpInBinding

    override fun getLayoutId(): Int {
        return R.layout.activity_sign_up_in
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()

        binding.tvSignIn.setOnClickListener { startActivity(Intent(this, SignIn::class.java)) }
        binding.tvHaveAnAccount.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SignUp::class.java
                )
            )
        }
    }
}