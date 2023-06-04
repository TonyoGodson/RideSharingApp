package com.godston.rideshareapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.godston.rideshareapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var emailId: String
    private var name: String? = null
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("user_id").toString()
        emailId = intent.getStringExtra("email_id").toString()
        name = intent.getStringExtra("name").toString()

        binding.inTv.text = "Welcome $name.\nYour User ID is $userId and your email is $emailId."
        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java) )
        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        super.onBackPressed()
    }
}