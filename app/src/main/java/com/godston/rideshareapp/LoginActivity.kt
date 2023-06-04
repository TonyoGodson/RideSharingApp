package com.godston.rideshareapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.godston.rideshareapp.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class LoginActivity : AppCompatActivity() {
    companion object {
        private val LOGIN_REQUEST_CODE = 1010
    }

    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var database: FirebaseDatabase
    private lateinit var driverInfoRef: DatabaseReference
    private lateinit var binding: ActivityLoginBinding

    override fun onStart() {
        super.onStart()
//        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        binding.loginSignupTv.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }
        binding.apply {
            loginBtn.setOnClickListener {
                when {
                    TextUtils.isEmpty(emailEt.text.toString().trim { it <= ' ' }) -> {
                        Toast.makeText(
                            this@LoginActivity,
                            "Please Enter Your Email",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    TextUtils.isEmpty(passwordEt.text.toString().trim { it <= ' ' }) -> {
                        Toast.makeText(
                            this@LoginActivity,
                            "Please Enter Your Password",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        val email: String = emailEt.text.toString().trim { it <= ' ' }
                        val password: String = passwordEt.text.toString().trim { it <= ' ' }

                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(
                                OnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Login Successfully.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        val intent = Intent(this@LoginActivity, DriverHomeActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        val firebaseUser: FirebaseUser = task.result!!.user!!
                                        intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                                        intent.putExtra("email_id", email)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            task.exception!!.message.toString(),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                    }
                }
            }
        }
    }

    private fun init() {
        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(com.godston.rideshareapp.utils.Common.DRIVER_INFO_REFERENCE)
        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null) {
                checkUserFromFirebase()
            } else showLoginLayout()
        }
    }

    private fun checkUserFromFirebase() {
        driverInfoRef
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@LoginActivity, "This User Already Exists", Toast.LENGTH_LONG).show()
                    } else {
//                        showRegisterLayout()
                    }
                }
            })
    }

//    private fun showRegisterLayout() {
//        TODO("Not yet implemented")
//    }

    private fun showLoginLayout() {
        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.activity_login)
            .setPhoneButtonId(R.id.loginWithPhone_btn)
            .setGoogleButtonId(R.id.loginWithGoogle_btn)
            .build()

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setTheme(R.style.Theme_RideShareApp)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            LOGIN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    response!!.error!!.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
