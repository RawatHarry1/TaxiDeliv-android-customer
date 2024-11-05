package com.superapp_customer.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object CoroutinesBase {
    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }

    fun async(work: suspend (() -> Any))= CoroutineScope(Dispatchers.Main).async {
        work()
    }

    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }

    fun default(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Default).launch {
            work()
        }

    fun unconfined(work: suspend () -> Unit) =
        CoroutineScope(Dispatchers.Unconfined).launch {

        }
}