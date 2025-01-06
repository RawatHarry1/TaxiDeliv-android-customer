package com.mcabs_driver.view.ui

import android.content.Intent
import android.os.Bundle
import com.mcabs_driver.R
import com.mcabs_driver.databinding.ActivitySignUpInBinding
import com.mcabs_driver.view.base.BaseActivity

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