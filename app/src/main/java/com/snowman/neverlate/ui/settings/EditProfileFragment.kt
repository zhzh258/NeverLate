package com.snowman.neverlate.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snowman.neverlate.databinding.FragmentEditProfileBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedUserViewModel
import com.snowman.neverlate.model.types.IUser
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.provider.Settings
import android.net.Uri
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.snowman.neverlate.R


class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()
    private val firebaseManager = FirebaseManager.getInstance()
    private var profileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        observeSettings()
        binding.btnSave.setOnClickListener {
            updateUserDetails()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.changePicture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGalleryForImage()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeSettings() {
        sharedUserViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                initProfileData(it)
            }
        }
    }

    private fun initProfileData(me: IUser) {
        binding.usernameET.setText(me.displayName)
        binding.addressET.setText(me.address)
        binding.phoneNumberET.setText(me.phoneNumber)
        binding.AboutMeET.setText(me.status)
        Glide.with(binding.profilePicture)
            .load(me.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.profilePicture)
    }

    private fun updateUserDetails() {
        val username = binding.usernameET.text.toString().trim()
        val address = binding.addressET.text.toString().trim()
        val phoneNumber = binding.phoneNumberET.text.toString().trim()
        val aboutMe = binding.AboutMeET.text.toString().trim()

        val uri = profileUri
        if (uri != null) {
            firebaseManager.saveImageToStorage(uri, "pfp") { url ->
                if (url != null) {
                    val updatedUserData = mapOf(
                        "displayName" to username,
                        "address" to address,
                        "phoneNumber" to phoneNumber,
                        "status" to aboutMe,
                        "photoURL" to url
                    )
                    save(updatedUserData)
                }
            }
        } else {
            val updatedUserData = mapOf(
                "displayName" to username,
                "address" to address,
                "phoneNumber" to phoneNumber,
                "status" to aboutMe,
            )
            save(updatedUserData)
        }
    }

    private fun save(updatedUserData: Map<String, Any>) {
        FirebaseManager.getInstance().editUserProfile(updatedUserData,
            onSuccess = {
                Toast.makeText(context, "User details updated successfully.", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(R.id.nav_profile)
                it.updateUserViewModel(sharedUserViewModel)
            },
            onFailure = { exception ->
                Toast.makeText(
                    context,
                    "Failed to update user details: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val STORAGE_PERMISSION_CODE = 1001
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            profileUri = data.data

            Glide.with(this)
                .load(profileUri)
                .circleCrop()
                .into(binding.profilePicture)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryForImage()
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                    showPermissionSettingsDialog()
                } else {
                    Toast.makeText(
                        context,
                        "Permission denied. Unable to change picture without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun showPermissionSettingsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Permission Required")
        builder.setMessage("This permission is needed for accessing your gallery to set a profile picture. Please enable it in app settings.")
        builder.setPositiveButton("Go to Settings") { dialog, which ->
            openAppSettings()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}