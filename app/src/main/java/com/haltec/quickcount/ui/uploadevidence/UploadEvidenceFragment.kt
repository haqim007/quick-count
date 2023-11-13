package com.haltec.quickcount.ui.uploadevidence

import android.Manifest
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.data.util.rotateBitmap
import com.haltec.quickcount.databinding.FragmentUploadEvidenceBinding
import com.haltec.quickcount.domain.model.ElectionStatus
import com.haltec.quickcount.domain.model.VoteEvidence
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import com.haltec.quickcount.ui.camera.CameraActivity
import com.haltec.quickcount.ui.camera.CameraActivity.Companion.IS_BACK_CAMERA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import java.io.File

@AndroidEntryPoint
class UploadEvidenceFragment : BaseFragment() {
    
    private lateinit var binding: FragmentUploadEvidenceBinding
    private val viewModel: UploadEvidenceViewModel by hiltNavGraphViewModels(R.id.authorized_nav_graph)
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isBackCamera: Boolean = true
    private val uploadEvidenceDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_RESULT) {
            val myFile = it.data?.getSerializableExtra(CameraActivity.PICTURE) as File
            viewModel.setImage(myFile)
            isBackCamera = it.data?.getBooleanExtra(IS_BACK_CAMERA, true) as Boolean
            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )
            binding.ivPhotoResult.setImageBitmap(result)
            binding.ivPhotoResult.isVisible = true
            binding.btnEditPhoto.isVisible = true
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUploadEvidenceBinding.inflate(layoutInflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        val args: UploadEvidenceFragmentArgs by navArgs()
        val tps = args.tps
        val election = args.election
        val isEditable = election.statusVote != ElectionStatus.VERIFIED
        viewModel.setTpsElection(tps, election)
        viewModel.fetchPrevData()
        binding.apply { 
            
            btnBack.setOnClickListener { 
                findNavController().navigateUp()
                viewModel.clearState()
            }
            
            tvElectionName.text = election.title
            
            etNotes.addTextChangedListener { 
                viewModel.setDescription(it.toString())
            }

            setupTypeName()

            setupCamera()
            
            viewModel.state.map { it.allowToSubmit }.launchCollectLatest { 
                btnUpload.isEnabled = it
                btnUpload.isClickable = it
            }
            
            btnUpload.isVisible = isEditable
            btnEditPhoto.isVisible = isEditable
            mcvVerifiedMessage.isVisible = !isEditable
            
            btnUpload.setOnClickListener { 
                if(
                    isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) && 
                    isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
                ){
                    fusedLocationClient.lastLocation.addOnSuccessListener {location ->
                        if (location != null){
                            viewModel.upload(location.latitude, location.longitude)
                        }else{
                            uploadEvidenceDialog
                                .setTitle(R.string.error_occured)
                                .setMessage(R.string.current_location_is_not_detected)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                    }
                }else{
                    mainViewModel.showCameraPermissionDialog()
                }
            }

            observeSubmitResult()

            ivPhotoResult.setOnClickListener {
                viewModel.state.value.apply { 
                    formState[type]?.image?.let {
                        zoomImage(it)
                    }
                }
            }
            
            // Observe type changes
            observeTypeChange()
            
        }
        
        return binding.root
    }

    private fun FragmentUploadEvidenceBinding.observeTypeChange() {
        viewModel.state.map { it.type }.launchCollectLatest {
            viewModel.state.value.formState[it]?.let { formInputState ->
                etNotes.setText(formInputState.description)
                if (formInputState.image != null) {
                    ivPhotoResult.isVisible = true
                    btnEditPhoto.isVisible = true
                    val result = rotateBitmap(
                        BitmapFactory.decodeFile(formInputState.image.path),
                        isBackCamera
                    )
                    ivPhotoResult.setImageBitmap(result)
                }else if(formInputState.imageUrl != null){
                    Glide.with(requireContext())
                        .load(formInputState.imageUrl)
                        .into(ivPhotoResult)
                    ivPhotoResult.isVisible = true
                    btnEditPhoto.isVisible = true
                } else {
                    ivPhotoResult.isVisible = false
                    btnEditPhoto.isVisible = false
                }
                etNotes.setText(formInputState.description)
                etNotes.clearFocus()

            } ?: run {
                ivPhotoResult.isVisible = false
                btnEditPhoto.isVisible = false
                etNotes.text = null
                etNotes.clearFocus()
            }
        }
    }

    private fun FragmentUploadEvidenceBinding.observeSubmitResult() {
        viewModel.state.map { it.submitResult }.launchCollectLatest {
            it.handle(object : ResourceHandler<VoteEvidence> {

                override fun onSuccess(data: VoteEvidence?) {

                    val notSelectedChipsTitles = arrayListOf<String>()
                    for (i in 0 until cgType.childCount) {
                        val chip = cgType.getChildAt(i) as Chip
                        val chipTitle = chip.text.toString()
                        if (chipTitle != viewModel.state.value.type) {
                            notSelectedChipsTitles.add(chipTitle)
                        }
                    }

                    uploadEvidenceDialog
                        .setTitle(R.string.upload_succeed)
                        .setMessage(
                            getString(
                                R.string.upload_succeed_message,
                                notSelectedChipsTitles.joinToString(", ")
                            )
                        )
                        .setIcon(R.drawable.ic_success)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show()
                }

                override fun onError(message: String?, data: VoteEvidence?) {
                    var title = ""
                    var content = ""
                    message?.let {
                        title = getString(R.string.warning)
                        content = it
                    } ?: run {
                        title = getString(R.string.error_occured)
                        content = getString(R.string.please_contact_admin)
                    }
                    uploadEvidenceDialog
                        .setTitle(title)
                        .setMessage(content)
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show()

                }

                override fun onAll(resource: Resource<VoteEvidence>) {
                    hsvChips.isVisible = resource !is Resource.Loading
                    mcvTakePhoto.isVisible = resource !is Resource.Loading
                    tilNotes.isVisible = resource !is Resource.Loading
                    btnUpload.isVisible = resource !is Resource.Loading

                    layoutLoader.lavAnimation.setAnimation(R.raw.send_loading)
                    layoutLoader.lavAnimation.playAnimation()
                    layoutLoader.lavAnimation.isVisible = resource is Resource.Loading
                }
            })
        }
    }

    private fun FragmentUploadEvidenceBinding.zoomImage(file: File){
        llExpandedPhoto.isVisible = true
        clMainContent.alpha = 0.1F
        val result = rotateBitmap(
            BitmapFactory.decodeFile(file.path),
            isBackCamera
        )
        ivExpandedPhotoResult.setImageBitmap(result)
        btnCloseExpand.setOnClickListener {
            llExpandedPhoto.isVisible = false
            clMainContent.alpha = 1.0F
        }
    }
    
    private fun FragmentUploadEvidenceBinding.setupTypeName() {
        viewModel.setType(cgType.findViewById<Chip>(cgType.checkedChipId).text.toString())
        cgType.setOnCheckedStateChangeListener { _, checkedIds -> 
            val typeChip = cgType.findViewById<Chip>(checkedIds[0])
            if (typeChip.text.toString() != viewModel.state.value.type) {
                viewModel.setType(typeChip.text.toString())
            }
        }
    }

    private fun FragmentUploadEvidenceBinding.setupCamera() {
        llTakePhoto.setOnClickListener {
            launchCamera()
        }
        btnEditPhoto.setOnClickListener { 
            launchCamera()
        }
    }
    
    private fun launchCamera(){
        if (!isPermissionGranted(CAMERA)) {
            mainViewModel.showCameraPermissionDialog()
        } else {
            val intent = Intent(requireContext(), CameraActivity::class.java)
            intent.putExtra(TYPE_NAME, viewModel.state.value.type)
            launcherIntentCameraX.launch(intent)
        }
    }

    companion object{
        const val CAMERA_RESULT = 100
        const val TYPE_NAME = "TYPE NAME"
    }
}