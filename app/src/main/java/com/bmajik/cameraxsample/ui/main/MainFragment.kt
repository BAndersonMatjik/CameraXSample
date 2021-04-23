package com.bmajik.cameraxsample.ui.main

import android.graphics.*
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bmajik.cameraxsample.R
import com.bmajik.cameraxsample.databinding.MainFragmentBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    val job = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture: ImageCapture


    val bitmapF = MutableLiveData<Bitmap?>()
    lateinit var camera: Camera
    lateinit var preview: Preview

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))


        bitmapF.observe(viewLifecycleOwner, {
            if (bitmapF != null) {
                binding.previewView.visibility = View.INVISIBLE
                binding.imgView.visibility = View.VISIBLE
                binding.imgView.setImageBitmap(it)
            }
        })

        binding.btnPhoto.setOnClickListener {
            onCLick()
        }

        binding.btndelete.setOnClickListener {
            onDelete()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        preview = Preview.Builder()
                .build()

        var cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        imageCapture = ImageCapture.Builder()
                .build()

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, imageCapture, preview)
    }


    fun onCLick() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {


//                image.toBitmap().apply {
//                    bitmapF.postValue(this)
//                }

                uiScope.launch(Dispatchers.Default) {
                    image.toBitmap().apply {
                        bitmapF.postValue(this)
                        image.close()
                    }
                }

                super.onCaptureSuccess(image)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }
        })
    }

    fun onDelete() {
        binding.previewView.visibility = View.VISIBLE
        binding.imgView.visibility = View.INVISIBLE
    }

    fun ImageProxy.toBitmap(): Bitmap {
        val startTime = System.currentTimeMillis()
        val buffer: ByteBuffer = planes[0].buffer // :- This line is where error was occurring

        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val endTime = System.currentTimeMillis()
        Log.d("Beanchmark", "toBitmap: ${endTime-startTime} ms THREAD ${Thread.currentThread().name}")
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}