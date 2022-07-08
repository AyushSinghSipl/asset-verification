package com.mahyco.assetsverification.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.integration.android.IntentIntegrator
import com.mahyco.assetsverification.R
import com.mahyco.assetsverification.asset_verification.asset_detail.viewmodel.HomeViewModel
import com.mahyco.assetsverification.core.Messageclass
import com.mahyco.assetsverification.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var codeScanner: CodeScanner
    private val MY_CAMERA_REQUEST_CODE = 100
    var msclass: Messageclass? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
      
        codeScanner = CodeScanner(requireContext(), binding.scannerView)
        setUi()
        return root
    }

    private fun setUi() {
        binding.btnAstVerification.setOnClickListener {
            scanQr()
        }
    }

    private fun scanQr() {
        binding.scannerView.visibility = View.VISIBLE
        binding.btnAstVerification.visibility = View.GONE

        msclass = Messageclass(context)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                MY_CAMERA_REQUEST_CODE
            )
        } else {
            // Parameters (default values)
            codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
            codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
            codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
            codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
            codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
            codeScanner.isFlashEnabled = false // Whether to enable flash or not
            codeScanner.decodeCallback = DecodeCallback {
                activity?.runOnUiThread {
                    findNavController().navigate(R.id.action_nav_home_to_nav_asset_detail)

                    binding.scannerView.visibility = View.GONE
                    binding.btnAstVerification.visibility = View.VISIBLE
                }
            }
            codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                activity?.runOnUiThread {
                    Toast.makeText(
                        context, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            binding.scannerView.setOnClickListener {
                codeScanner.startPreview()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.scannerView.visibility == View.VISIBLE) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                codeScanner.camera =
                    CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
                codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
                // ex. listOf(BarcodeFormat.QR_CODE)
                codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
                codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
                codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
                codeScanner.isFlashEnabled = false // Whether to enable flash or not

                // Callbacks
                codeScanner.decodeCallback = DecodeCallback {
                    activity?.runOnUiThread {

                        findNavController().navigate(R.id.action_nav_home_to_nav_asset_detail)
                        binding.scannerView.visibility = View.GONE
                        binding.btnAstVerification.visibility = View.VISIBLE
                    }
                }
                codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context, "Camera initialization error: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


                binding.scannerView.setOnClickListener {
                    codeScanner.startPreview()
                }
            }
        } else {
//                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            msclass?.showMessage("camera permission denied")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            var result = IntentIntegrator.parseActivityResult(resultCode, data)
            if (result != null) {
                Toast.makeText(context, "scan complete ", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.action_nav_home_to_nav_asset_detail)
                binding.scannerView.visibility = View.GONE
                binding.btnAstVerification.visibility = View.VISIBLE
            }
        }
    }
}