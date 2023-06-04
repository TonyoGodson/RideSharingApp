package com.godston.rideshareapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.godston.rideshareapp.databinding.ActivityRegisterBinding
import com.godston.rideshareapp.utils.Common
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var driverInfoRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)
        binding.apply {
            signupLoginTv.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                finish()
            }
            submitBtn.setOnClickListener {
                when {
                    TextUtils.isEmpty(nameEt.text.toString().trim { it <= ' ' }) -> {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Please Enter Your Name",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    TextUtils.isEmpty(phoneEt.text.toString().trim { it <= ' ' }) -> {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Please Enter Your Phone Number",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    TextUtils.isEmpty(emailEt.text.toString().trim { it <= ' ' }) -> {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Please Enter Your Email",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    TextUtils.isEmpty(passwordEt.text.toString().trim { it <= ' ' }) -> {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Please Enter Your Password",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        val name: String = nameEt.text.toString().trim { it <= ' ' }
                        val phone: String = phoneEt.text.toString().trim { it <= ' ' }
                        val email: String = emailEt.text.toString().trim { it <= ' ' }
                        val password: String = passwordEt.text.toString().trim { it <= ' ' }

                        val model = DriverInfoModel()
                        model.name = name
                        model.phone = phone
                        model.email = email
                        model.password = password
                        model.rating = 0.0

                        driverInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .setValue(model)
                            .addOnFailureListener { error ->
                                Toast.makeText(this@RegisterActivity, error.message, Toast.LENGTH_LONG).show()
                            }
                            .addOnSuccessListener {
                                Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }


                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(
                                OnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val firebaseUser: FirebaseUser = task.result!!.user!!
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            "Signed Up Successfully.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        intent.putExtra("user_id", firebaseUser.uid)
                                        intent.putExtra("email_id", email)
                                        intent.putExtra("name", name)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@RegisterActivity,
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
}
