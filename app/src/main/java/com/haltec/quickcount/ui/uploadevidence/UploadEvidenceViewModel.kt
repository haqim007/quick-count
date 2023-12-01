package com.haltec.quickcount.ui.uploadevidence

import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.VoteEvidence
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
    
    fun fetchPrevData(){
        if (state.value.tps != null && state.value.election != null){
            repository.getPrevData(state.value.tps!!, state.value.election!!).launchCollectLatest {
                it.handle(object : ResourceHandler<List<VoteEvidence>>{
                    override fun onSuccess(data: List<VoteEvidence>?) {
                        setFormState(it.data ?: emptyList())
                    }

                    override fun onLoading() {
                        _state.update { state ->
                            state.copy(
                                hasLoadPrevData = false
                            )
                        }
                    }

                    override fun onError(message: String?, data: List<VoteEvidence>?, code: Int?) {
                        setFormState(it.data ?: emptyList())
                    }
                })
            }
        }
    }

    private fun setFormState(data: List<VoteEvidence>) {
        val copyFormState = state.value.formState
        data.forEach { voteEvidence ->
            copyFormState[voteEvidence.type] =
                FormInputState(
                    image = voteEvidence.file,
                    imageUrl = voteEvidence.fileUrl,
                    description = voteEvidence.description
                )
        }
        _state.update { state ->
            state.copy(
                hasLoadPrevData = true,
                formState = copyFormState
            )
        }
    }

    fun setImage(file: File){
        _state.update { state ->
            state.type?.let {type ->
                val formState = state.formState
                state.formState[type]?.let {formInputState ->
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
                        formState[type]!!.description,
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
                    formState[type]?.image != null
                )
            }
        }
    }
    
    fun clearState(){
        _state.update { state ->
            state.copy(
                hasLoadPrevData = false,
                tps = null,
                election = null,
                formState = hashMapOf(),
                allowToSubmit = false,
                type = null,
                submitResult = Resource.Idle()
            )
        }
    }
}

typealias Type = String

data class UploadEvidenceUiState(
    val hasLoadPrevData: Boolean = false,
    val tps: TPS? = null,
    val election: Election? = null,
    val formState: HashMap<Type, FormInputState?> = hashMapOf(),
    val allowToSubmit: Boolean = false,
    val type: Type? = null,
    val submitResult: Resource<VoteEvidence> = Resource.Idle()
)

data class FormInputState(
    val image: File? = null,
    val imageUrl: String? = null,
    val description: String? = null,
)