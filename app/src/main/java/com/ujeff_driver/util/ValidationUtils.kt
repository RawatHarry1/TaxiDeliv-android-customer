package com.ujeff_driver.util

import java.util.regex.Pattern


object ValidationUtils {

    @JvmStatic
    fun isEmailValid(emailId: String): Boolean {
        val emailPattern = ("[A-Z0-9a-z.-_+]+[A-Z0-9a-z]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,50}")
        return if (emailId.length < 3 || emailId.length > 265) false else {
            emailId.matches(Regex(emailPattern))
        }
    }

    @JvmStatic
    fun isPasswordValid(password: String): Boolean {
        val passwordREGEX = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#?~`!@$%^)(+=/|,;':.<>&*_-])[A-Za-z\\d#?!@$%^&*_-]{8,16}$"
        )

        return passwordREGEX.matcher(password).matches()
    }

    @JvmStatic
    fun isNameValid(password: String): Boolean {
        val passwordREGEX = Pattern.compile(
            "[a-z][A-Z][A-Za-z]"
        )

        return passwordREGEX.matcher(password).matches()
    }

    fun validName(name: String): Boolean {
        for (i in 0 until name.length) {
            val c: Char = name.get(i)
            if (!(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z') && !(c.toString().equals(" "))) {
                return false
            }
        }
        return true
    }

    fun validUniqueName(name: String): Boolean {
        for (i in 0 until name.length) {
            val c: Char = name.get(i)
            if (!(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z') && !(c.toString()
                    .equals(".")) && !(c.toString().equals("_"))
                && !(c in '0'..'9')
            ) {
                return false
            }
        }
        return true
    }
}