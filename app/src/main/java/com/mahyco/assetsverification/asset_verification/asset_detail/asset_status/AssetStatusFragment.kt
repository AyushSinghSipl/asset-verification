package com.mahyco.assetsverification.asset_verification.asset_detail.asset_status

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.mahyco.assetsverification.HomeActivity
import com.mahyco.assetsverification.R
import com.mahyco.assetsverification.asset_verification.asset_detail.asset_status.adapter.*
import com.mahyco.assetsverification.asset_verification.asset_detail.asset_status.model.SaveAssetStatusParam
import com.mahyco.assetsverification.asset_verification.asset_detail.asset_verified.AssetVerifiedFragment
import com.mahyco.assetsverification.asset_verification.asset_detail.viewmodel.HomeViewModel
import com.mahyco.assetsverification.core.SharedPreference
import com.mahyco.assetsverification.databinding.FragmentAssetStatusBinding
import com.mahyco.cmr_app.core.Constant
import com.mahyco.rcbucounterboys2020.utils.EncryptDecryptManager
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import com.vansuita.pickimage.enums.EPickType
import com.vansuita.pickimage.listeners.IPickCancel
import com.vansuita.pickimage.listeners.IPickResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AssetStatusFragment : Fragment(), onCLick, onNpaCLick {
    // TODO: Rename and change types of parameters
    private var assetQRId: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentAssetStatusBinding
    var activity = getActivity() as HomeActivity?
    var menuList = ArrayList<NotInUseReasonModel>()
    var npaList = ArrayList<NotInUseReasonModel>()
    lateinit var notInUseReasonAdapter: NotInUseReasonAdapter
    lateinit var npaAdapter: NpaAdapter
    private val viewModel: HomeViewModel by viewModels()
    lateinit var status: String
    lateinit var reason: String
    var npa: String = ""
    var alert: AlertDialog? = null
    var imageBitmap: Bitmap? = null
    private val MY_CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST = 200
    var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            assetQRId = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAssetStatusBinding.inflate(inflater, container, false)
        val root: View = binding.root
        activity?.binding?.appBarHome?.titleToolbar?.text = "Asset Status"
        registerObserver()
        setUi()
        return root
    }

    private fun registerObserver() {
        viewModel!!.loadingLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                binding.llProgressBar.loader.visibility = View.VISIBLE

            } else {
                binding.llProgressBar.loader.visibility = View.GONE

            }
        })

        //In Case of error will show error in  toast message
        viewModel!!.errorLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null)
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })

        viewModel.SaveAssetStatusData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            var result = it

            if (result.status?.equals("Missmatch") != true) {

                if(photoFile?.exists()!!){
                    photoFile?.getCanonicalFile()?.delete();
                    if(photoFile?.exists()!!){
                       context?.deleteFile(photoFile?.getName());
                    }
                }

                val gson = Gson()
                val json = gson.toJson(result)
                Constant.addFragmentToActivity(
                    AssetVerifiedFragment.newInstance(json),
                    R.id.container,
                    this.parentFragmentManager,
                    AssetVerifiedFragment.TAG
                )

            }

        })


    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setUi() {

        val sd = SimpleDateFormat(
            "hh:mm aa" +
                    " - EEE,dd MMM"
        )
        val currentDate = sd.format(Date())
        binding.textViewTime.text = currentDate
        binding.chooseType.setOnClickListener {

            binding.typeCard.visibility = View.VISIBLE
        }

        binding.textViewInUse.setOnClickListener {
            binding.typeCard.visibility = View.GONE
            binding.typeSelection.text = "IN USE"
            binding.buttonVerify.visibility = View.VISIBLE
            binding.buttonPhotoUpload.visibility = View.VISIBLE
            binding.layoutNotInUse.visibility = View.GONE
            status = "IN USE"
        }

        binding.buttonPhotoUpload.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        }

        binding.buttonVerify.setOnClickListener {

            if (status.equals("IN USE")) {
                val sharedPreference = SharedPreference(requireContext())
                val emp_id = sharedPreference.getValueString(Constant.EMP_ID)
                val saveAssetStatusParam = SaveAssetStatusParam(
                    "IN USE",
                    emp_id,
                    assetQRId?.toInt(),
                    "",
                    npa
                )

                viewModel.saveAssetStatus(saveAssetStatusParam)
            } else {
                if (reason != null) {
                    val sharedPreference = SharedPreference(requireContext())
                    val emp_id = sharedPreference.getValueString(Constant.EMP_ID)
                    val saveAssetStatusParam = SaveAssetStatusParam(
                        "NOT IN USE",
                        emp_id,
                        assetQRId?.toInt(),
                        reason
                    )

                    viewModel.saveAssetStatus(saveAssetStatusParam)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please select a valid reason",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        binding.textViewNotInUse.setOnClickListener {
            status = "NOT IN USE"
            binding.typeCard.visibility = View.GONE
            binding.typeSelection.text = "NOT IN USE"
            binding.buttonVerify.visibility = View.GONE
            binding.buttonPhotoUpload.visibility = View.GONE
            binding.layoutNotInUse.visibility = View.VISIBLE
            binding.reasonSelection.text = "CHOOSE REASON"
        }
        binding.chooseReason.setOnClickListener {
            binding.ReasonCard.visibility = View.VISIBLE
            binding.layoutNotInUse.visibility = View.VISIBLE
            if (npa.equals("NPA")) {
                menuList = ArrayList()
                menuList.add(NotInUseReasonModel("HIGH REPAIRING COST", 1))
                menuList.add(NotInUseReasonModel("NOT REPAIRABLE CONDITION", 2))
                menuList.add(NotInUseReasonModel("DAMAGED ", 3))
//        menuList.add(NotInUseReasonModel("NPA",4))
                menuList.add(NotInUseReasonModel("TECHNOLOGY ABSOLUTE", 4))
            } else {
                menuList = ArrayList()
                menuList.add(NotInUseReasonModel("SEASONABLE USE", 1))
                menuList.add(NotInUseReasonModel("UNDER REPAIRING", 2))
            }

            notInUseReasonAdapter = NotInUseReasonAdapter(menuList, this)
            binding.recyclerViewReason.adapter = notInUseReasonAdapter
            binding.recyclerViewReason.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)


        }

        binding.chooseNPA.setOnClickListener {
            binding.npaCard.visibility = View.VISIBLE
        }


        npaList.add(NotInUseReasonModel("NPA", 1))
        npaList.add(NotInUseReasonModel("NOT NPA", 2))


        npaAdapter = NpaAdapter(npaList, this)
        binding.recyclerViewNpa.adapter = npaAdapter
        binding.recyclerViewNpa.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)


    }

    private fun clickImage() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_DENIED
        ) {

            PickImageDialog.build(PickSetup().setPickTypes(EPickType.CAMERA))
                .setOnPickResult(object : IPickResult {
                    override fun onPickResult(r: PickResult?) {
                        photoFile = createImageFile()

                        if (r?.bitmap != null) {
                            imageBitmap = r?.bitmap

                            try {
                                FileOutputStream(photoFile?.absolutePath).use { out ->
                                    r?.bitmap.compress(
                                        Bitmap.CompressFormat.PNG,
                                        100,
                                        out
                                    ) // bmp is your Bitmap instance
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            binding?.imageViewAsset?.setImageBitmap(r?.bitmap)
                            binding?.cardImage?.visibility = View.VISIBLE

                        }
                    }
                })
                .setOnPickCancel(object : IPickCancel {
                    override fun onCancelClick() {
                        //TODO: do what you have to if user clicked cancel
                    }
                }).show(this.childFragmentManager)
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                ),
                MY_CAMERA_PERMISSION_CODE
            )

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === CAMERA_REQUEST && resultCode === Activity.RESULT_OK) {

            imageBitmap = data?.getExtras()!!.get("data") as Bitmap?
            if (imageBitmap != null) {
                photoFile = createImageFile()
                try {
                    FileOutputStream(photoFile?.absolutePath).use { out ->
                        imageBitmap?.compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            out
                        ) // bmp is your Bitmap instance
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                binding?.imageViewAsset?.setImageBitmap(imageBitmap)
                binding?.cardImage?.visibility = View.VISIBLE

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(activity?.packageManager!!) != null) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST)
                    }
                } else {
                    if (alert != null) {
                        if (alert!!.isShowing) {
                            alert!!.dismiss()
                        }
                    }
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("You need to allow Camera permission to perform further operations")
                        .setCancelable(false)
                        .setPositiveButton(
                            "Allow"
                        ) { dialog, id ->
                            dialog.dismiss()
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri =
                                Uri.fromParts("package", requireContext().getPackageName(), null)
                            intent.data = uri
                            startActivity(intent)

                        }
                        .setNegativeButton(
                            "Deny"
                        ) { dialog, id -> }
                    alert = builder.create()
                    alert?.show()
//                    Toast.makeText(context, "camera permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName =  "asset_" + timeStamp + ""
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
        Log.e(TAG, "createImageFile: "+image.absolutePath )
        return image
    }

    companion object {

        val TAG = AssetStatusFragment::class.java.name

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            AssetStatusFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    // putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onReasonSelect(position: Int) {
        binding.ReasonCard.visibility = View.GONE
        binding.reasonSelection.text = menuList.get(position).name
        reason = menuList.get(position).name
        binding.buttonVerify.visibility = View.VISIBLE
        binding.buttonPhotoUpload.visibility = View.VISIBLE
    }

    override fun onNpaSelect(position: Int) {
        binding.buttonVerify.visibility = View.GONE
        binding.buttonPhotoUpload.visibility = View.GONE
        npa = npaList.get(position).name
        binding.npaCard.visibility = View.GONE
        binding.textViewSelectedNpa.text = npa
        binding.npaSelection.text = npa
        binding.textViewSelectedNpa.visibility = View.VISIBLE
        binding.chooseReason.visibility = View.VISIBLE
        binding.reasonSelection.text = "CHOOSE REASON"
        if (npa.equals("NPA")) {
            menuList = ArrayList()
            menuList.add(NotInUseReasonModel("HIGH REPAIRING COST", 1))
            menuList.add(NotInUseReasonModel("NOT REPAIRABLE CONDITION", 2))
            menuList.add(NotInUseReasonModel("DAMAGED ", 3))
//        menuList.add(NotInUseReasonModel("NPA",4))
            menuList.add(NotInUseReasonModel("TECHNOLOGY ABSOLUTE", 4))
        } else {
            menuList = ArrayList()
            menuList.add(NotInUseReasonModel("SEASONABLE USE", 1))
            menuList.add(NotInUseReasonModel("UNDER REPAIRING", 2))
        }
    }
}