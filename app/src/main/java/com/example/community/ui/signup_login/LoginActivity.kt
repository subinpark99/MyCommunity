package com.example.community.ui.signup_login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.community.data.MyApplication
import com.example.community.data.entity.User
import com.example.community.databinding.ActivityLoginBinding
import com.example.community.ui.other.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val userRef = Firebase.database.getReference("user")
    private var gson: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        auth = Firebase.auth
        initLoginButton()
    }

    private fun initLoginButton() {
        binding.loginBtn.setOnClickListener {
            val email = binding.putEmailTv.text.toString()
            val password = binding.putPasswordTv.text.toString()

            if (email.isEmpty() or password.isEmpty()) {
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var users: User

                        auth.currentUser?.let { it1 ->
                            userRef.child(it1.uid).get().addOnSuccessListener {
                                if (it != null) {

                                    users = it.getValue(User::class.java)!!

                                    saveData(auth.currentUser!!.uid, users,
                                        users.fcmtoken?.get("token") as String
                                    )
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun saveData(uid: String, user: User,token:String) {

        val userJson = gson.toJson(user)
        MyApplication.prefs.setUser("user", userJson)  // current user 정보 저장
        MyApplication.prefs.setToken("token", token)
        MyApplication.prefs.setUid("uid", uid) // current user uid 저장
    }

    private fun init() {

        binding.signupIv.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        // 상태바 없애기
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}