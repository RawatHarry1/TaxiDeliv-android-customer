package com.salonedriver.view.base

sealed class LoadingState {
    class LOADING(var type: Int = LoaderType.NORMAL, var msg: String = "") : LoadingState()
    class LOADED(var type: Int = LoaderType.NORMAL, var msg: String = "") : LoadingState()
}
