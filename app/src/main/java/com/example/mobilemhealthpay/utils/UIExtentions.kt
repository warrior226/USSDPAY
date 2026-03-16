package com.example.mobilemhealthpay.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers

fun ViewModel.contextIO() = viewModelScope.coroutineContext + Dispatchers.IO


