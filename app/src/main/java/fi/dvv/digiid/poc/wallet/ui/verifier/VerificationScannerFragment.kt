package fi.dvv.digiid.poc.wallet.ui.verifier

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.wallet.databinding.FragmentVerificationScannerBinding
import fi.dvv.digiid.poc.wallet.ui.common.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VerificationScannerFragment : Fragment() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewSurfaceProvider: Preview.SurfaceProvider

    private val viewModel: VerificationViewModel by activityViewModels()

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) startCamera()
            else Timber.w("User denied camera permissions")
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentVerificationScannerBinding.inflate(inflater, container, false)

        previewSurfaceProvider = binding.scannerPreviewView.surfaceProvider
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraPermissionRequest.launch(Manifest.permission.CAMERA)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        launchAndRepeatWithViewLifecycle {
            viewModel.credentialScannedEvent.collect {
                findNavController().navigate(VerificationScannerFragmentDirections.toVerificationResult())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewSurfaceProvider)

            val qrAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            qrAnalyzer.setAnalyzer(cameraExecutor, ZxingQrCodeAnalyzer {
                runBlocking { viewModel.processQRCode(it) }
            })

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    qrAnalyzer
                )
            } catch (exc: Exception) {
                Timber.e("Use case binding failed: $exc")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
}