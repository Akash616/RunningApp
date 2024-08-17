package com.akashgupta.runningapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.akashgupta.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    //we don't need to define in module bec. dagger know how to create RunDAO, (Only one parameter)
    val mainRepository: MainRepository
): ViewModel() {
}