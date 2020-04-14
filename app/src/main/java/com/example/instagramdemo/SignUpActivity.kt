package com.example.instagramdemo

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_in_link_btn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        sigup_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = full_name_sigup.text.toString()
        val userName = user_name_sigup.text.toString()
        val email = email_sigup.text.toString()
        val password = password_signup.text.toString()

        when {
            fullName.isEmpty() -> Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT)
                .show()
            userName.isEmpty() -> Toast.makeText(this, "user name is required", Toast.LENGTH_SHORT)
                .show()
            email.isEmpty() -> Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            password.isEmpty() -> Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT)
                .show()

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("Sign up")
                progressDialog.setMessage("SigningUp")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        saveUserInfo(fullName, userName, email, progressDialog)
                    } else {
                        val message = it.exception!!.toString()
                        Toast.makeText(this, "message: $message", Toast.LENGTH_SHORT).show()
                        mAuth.signOut()
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun saveUserInfo(
        fullName: String,
        userName: String,
        email: String,
        progressDialog: ProgressDialog
    ) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "hey i am using my instagram clone app"
        userMap["image"] =
            "https://firebasestorage.googleapis.com/v0/b/play-3690d.appspot.com/o/Default%20Images%2FWIN_20200402_21_21_22_Pro.jpg?alt=media&token=cf4851da-4c25-4916-9802-27aad89c6547"

        userRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Account has been created successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID).setValue(true)

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = it.exception!!.toString()
                    Toast.makeText(this, "message: $message", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}
