package com.haltec.quickcount.ui.uploadevidence

import android.Manifest
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.Rotate
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.databinding.FragmentUploadEvidenceBinding
import com.haltec.quickcount.domain.model.SubmitVoteStatus
import com.haltec.quickcount.domain.model.VoteEvidence
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import com.haltec.quickcount.ui.camera.CameraActivity
import com.haltec.quickcount.ui.camera.CameraActivity.Companion.IS_BACK_CAMERA
import com.haltec.quickcount.util.lowerAllWords
import com.haltec.quickcount.util.rotateBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import java.io.File

@AndroidEntryPoint
class UploadEvidenceFragment : BaseFragment() {
    
    private lateinit var binding: FragmentUploadEvidenceBinding
    private val viewModel: UploadEvidenceViewModel by viewModels()
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
        val isEditable = election.statusVote != SubmitVoteStatus.VERIFIED
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
                    it.isEnabled = false
                    it.isClickable = false
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                        override fun isCancellationRequested() = false
                    }).addOnSuccessListener { location ->
                        if (location != null){
                            viewModel.upload(location.latitude, location.longitude)
                        }else{
                            uploadEvidenceDialog
                                .setTitle(R.string.error_occured)
                                .setMessage(R.string.current_location_is_not_detected)
                                .setPositiveButton(R.string.ok, null)
                                .show()

                            it.isEnabled = true
                            it.isClickable = true
                        }
                    }
                }else{
                    mainViewModel.showLocationPermissionDialog()
                }
            }

            observeSubmitResult()

            ivPhotoResult.setOnClickListener {
                viewModel.state.value.apply { 
                    formState[type]?.image?.let {
                        zoomImage(it)
                    } ?: run {
                        formState[type]?.imageUrl?.let {
                            zoomImage(it)
                        }
                    }
                    
                }
            }
            
            // Observe type changes
            observeTypeChange()
            
            viewModel.state.map { it.hasLoadPrevData }.launchCollectLatest {
                if (it){
                    viewModel.setType(cgType.findViewById<Chip>(cgType.checkedChipId).text.toString())
                }
            }
            
        }
        
        return binding.root
    }

    private fun FragmentUploadEvidenceBinding.observeTypeChange() {
        viewModel.state.map { it.type }.launchCollectLatest {type ->
            // show card box on every change of state type
            binding.llTakePhoto.isVisible = true
            
            viewModel.state.value.formState[type]?.let { formInputState ->
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
                        .error(R.drawable.broken_image_270)
                        .transform(Rotate(90))
                        .into(ivPhotoResult)
                    ivPhotoResult.isVisible = true
                    btnEditPhoto.isVisible = true
                    
                    // hide card box in case image broken
                    binding.llTakePhoto.isVisible = false
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
                            getString(R.string.upload_succeed_message)
                        )
                        .setIcon(R.drawable.ic_success)
                        .setPositiveButton(getString(R.string.ok)){_,_ ->
                            findNavController().navigateUp()
                            viewModel.clearState()
                        }
                        .show()
                }

                override fun onError(message: String?, data: VoteEvidence?, code: Int?) {

                    var title: String = ""
                    var mMessage: String? = ""
                    if (code == CustomThrowable.UNKNOWN_HOST_EXCEPTION){
                        title = getString(R.string.no_internet_connection)
                        mMessage = getString(R.string.will_be_sent_once_internet_available)
                    }else{
                        message?.let {
                            title = getString(R.string.warning)
                            mMessage = it
                        } ?: run {
                            title = getString(R.string.error_occured)
                            mMessage = getString(R.string.please_contact_admin)
                        }
                    }
                    
                    uploadEvidenceDialog
                        .setTitle(title)
                        .setMessage(mMessage)
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show()

                }

                override fun onAll(resource: Resource<VoteEvidence>) {
                    hsvChips.isVisible = resource !is Resource.Loading
                    mcvTakePhoto.isVisible = resource !is Resource.Loading
                    tilNotes.isVisible = resource !is Resource.Loading
                    btnUpload.isVisible = resource !is Resource.Loading
                    btnUpload.isEnabled = resource !is Resource.Loading
                    btnUpload.isClickable = resource !is Resource.Loading

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

    private fun FragmentUploadEvidenceBinding.zoomImage(fileUrl: String){
        llExpandedPhoto.isVisible = true
        clMainContent.alpha = 0.1F
        Glide.with(requireContext())
            .asBitmap()
            .load(fileUrl)
            .error(R.drawable.broken_image_270)
            .transform(Rotate(90))
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // The 'resource' variable now contains the loaded Bitmap
                    // You can use the Bitmap as needed
                    // For example, set it in an ImageView
                    ivExpandedPhotoResult.setImageBitmap(resource)
                }
            })
        btnCloseExpand.setOnClickListener {
            llExpandedPhoto.isVisible = false
            clMainContent.alpha = 1.0F
        }
    }
    
    private fun FragmentUploadEvidenceBinding.setupTypeName() {
        
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
            intent.putExtra(FILE_NAME_EXTRA, 
                lowerAllWords("${viewModel.state.value.type}_${viewModel.state.value.tps?.id}_${viewModel.state.value.election?.id}")
                )
            launcherIntentCameraX.launch(intent)
        }
    }

    companion object{
        const val CAMERA_RESULT = 100
        const val FILE_NAME_EXTRA = "filename"
    }
}