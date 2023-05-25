package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.utils.SingleLiveEvent

class AuthenticationViewModel : ViewModel() {

    val authenticationStateEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()

    init {
        getLoginState()
    }

    val isLogIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null


    fun getLoginState() {
        authenticationStateEvent.postValue(isLogIn)
    }
}