package com.godston.rideshareapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.godston.rideshareapp.databinding.ActivityDriverHomeBinding
import com.godston.rideshareapp.utils.Common
import com.godston.rideshareapp.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.StringBuilder

class DriverHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDriverHomeBinding
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var profPic: ImageView
    private lateinit var waitingDialog: AlertDialog
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDriverHome.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        init()
    }

    private fun init() {
        storageRef = FirebaseStorage.getInstance().getReference()
        waitingDialog = AlertDialog.Builder(this)
            .setMessage("Loading...")
            .setCancelable(false).create()
        navView.setNavigationItemSelectedListener { it ->
            if (it.itemId == R.id.nav_logout) {
                val builder = AlertDialog.Builder(this@DriverHomeActivity)
                builder.setTitle("Logout")
                    .setMessage("Do you want to logout?")
                    .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton("Yes") { dialogInterface, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@DriverHomeActivity, LoginActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        dialogInterface.dismiss()
                        finish()
                    }.setCancelable(false)
                val dialog = builder.create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(android.R.color.black))
                }
                dialog.show()
            }
            true
        }

        val headerView = navView.getHeaderView(0)
        val txtName = headerView.findViewById<View>(R.id.txt_name) as TextView
        val txtPhone = headerView.findViewById<View>(R.id.txt_phone) as TextView
        val txtStar = headerView.findViewById<View>(R.id.txt_star) as TextView
        profPic = headerView.findViewById<View>(R.id.userIV) as ImageView
        txtName.text = FirebaseAuth.getInstance().currentUser?.uid
        txtName.text = FirebaseAuth.getInstance().currentUser?.uid /*Common.currentUser?.name*/
        txtPhone.text = FirebaseAuth.getInstance().currentUser?.uid // Common.currentUser?.phone
        txtStar.text = FirebaseAuth.getInstance().currentUser?.uid // StringBuilder().append(Common.currentUser?.rating)
        if (Common.currentUser != null && Common.currentUser!!.image != null && !TextUtils.isEmpty(
                Common.currentUser.image
            )
        ) {
            Glide.with(this)
                .load(Common.currentUser.image)
                .into(profPic)
        }
        profPic.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "select picture"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && requestCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                imageUri = data.data!!
                profPic.setImageURI(imageUri)
                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder = AlertDialog.Builder(this@DriverHomeActivity)
        builder.setTitle("Change Profile Picture")
            .setMessage("Do you want to change your profile picture?")
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("Yes") { dialogInterface, _ ->
                if (imageUri != null) {
                    waitingDialog.show()
                    val proPicFolder = storageRef.child("profPics/${FirebaseAuth.getInstance().currentUser!!.uid}")
                    proPicFolder.putFile(imageUri!!)
                        .addOnFailureListener { error ->
                            Snackbar.make(drawerLayout, error.message.toString(), Snackbar.LENGTH_LONG).show()
                            waitingDialog.dismiss()
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                proPicFolder.downloadUrl.addOnSuccessListener { uri ->
                                    val update_data = HashMap<String, Any>()
                                    update_data.put("profPif", uri.toString())
                                    UserUtils.updateUser(drawerLayout, update_data)
                                }
                            }
                            waitingDialog.dismiss()
                        }.addOnProgressListener { taskSnapShot ->
                            val progress = (100.0 * taskSnapShot.bytesTransferred / taskSnapShot.totalByteCount)
                            waitingDialog.setMessage(StringBuilder("Uploading: ").append(progress).append("%"))
                        }
                }
            }.setCancelable(false)
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(android.R.color.black))
        }
        dialog.show()
    }

    companion object {
        val PICK_IMAGE_REQUEST = 7070
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.driver_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    override fun onBackPressed() {
        startActivity(Intent(this@DriverHomeActivity, LoginActivity::class.java))
        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        super.onBackPressed()
    }
}
