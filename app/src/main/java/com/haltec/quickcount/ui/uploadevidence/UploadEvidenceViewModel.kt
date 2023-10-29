package com.haltec.quickcount.ui.uploadevidence

import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.UploadEvidenceResult
import com.haltec.quickcount.domain.repository.IUploadEvidenceRepository
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadEvidenceViewModel @Inject constructor(
    private val repository: IUploadEvidenceRepository
) : BaseViewModel<UploadEvidenceUiState>() {
    override val _state = MutableStateFlow(UploadEvidenceUiState())

    fun setTpsElection(tps: TPS, election: Election){
        _state.update { state ->
            state.copy(
                tps = tps,
                election = election
            )
        }
        validateAll()
    }
    
    fun setImage(file: File){
        _state.update { state ->
            state.type?.let {type ->
                val formState = state.formState
                state.formState[type]?.let {formInputState ->
//                    state.formState[type]
                    formState[type] = formInputState.copy(
                        image = file
                    )
                } ?: run {
                    state.formState.put(type, FormInputState(image = file))
                }
                state.copy(
                    formState = formState
                )
            } ?: state
        }
        validateAll()
    }
    
    fun setDescription(value: String){
        _state.update { state ->
            state.type?.let {
                val formState = state.formState
                formState[it] = state.formState[it]?.copy(
                    description = value
                )?: run {
                    state.formState.put(it, FormInputState(description = value))
                }
                state.copy(
                    formState = formState
                )
            } ?: state
        }
        validateAll()
    }
    
    fun setType(value: Type){
        _state.update { state ->
            state.copy(
                type = value
            )
        }
        validateAll()
    }
    
    fun upload(latitude: Double, longitude: Double) {
        viewModelScope.launch { 
            state.value.apply {
                if (allowToSubmit){
                    repository.upload(
                        tps!!,
                        election!!,
                        latitude,
                        longitude,
                        type!!,
                        formState[type]!!.description!!,
                        formState[type]!!.image!!
                    ).collectLatest { 
                        _state.update { state -> state.copy(submitResult = it) }
                    }
                }
            }
        }
    }
    
    private fun validateAll(){
        state.value.apply { 
            _state.update { state ->
                state.copy(
                    allowToSubmit = tps != null &&
                    election != null &&
                    !type.isNullOrEmpty() &&
                    formState.size > 0 &&
                    formState[type]?.image != null &&
                    !formState[type]?.description.isNullOrEmpty()
                )
            }
        }
    }
}

typealias Type = String

data class UploadEvidenceUiState(
    val tps: TPS? = null,
    val election: Election? = null,
    val formState: HashMap<Type, FormInputState?> = hashMapOf(),
    val allowToSubmit: Boolean = false,
    val type: Type? = null,
    val submitResult: Resource<UploadEvidenceResult> = Resource.Idle()
)

data class FormInputState(
    val image: File? = null,
    val description: String? = null,
)