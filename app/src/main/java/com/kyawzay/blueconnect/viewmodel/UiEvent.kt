package com.kyawzay.blueconnect.viewmodel

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class NavigateTo(val route: String) : UiEvent()
}
