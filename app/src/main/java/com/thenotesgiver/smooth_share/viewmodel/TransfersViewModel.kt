package com.thenotesgiver.smooth_share.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.thenotesgiver.smooth_share.data.WebDataRepository
import com.thenotesgiver.smooth_share.database.model.WebTransfer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject internal constructor(
    private val webDataRepository: WebDataRepository,
) : ViewModel() {

    val transfersHistory = liveData {
        val webTransfers = webDataRepository.getReceivedContents()

        emitSource(webTransfers)
    }

    fun deleteTransferHistory(webTransfer: WebTransfer) {
        viewModelScope.launch(Dispatchers.IO) {
            webDataRepository.remove(webTransfer)
        }
    }
}