package com.example.instagramdemo

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")

        save_new_post_btn.setOnClickListener {
            uploadImage()
        }
        //button has ben clicked get so get image from phone
        CropImage.activity()
            .setAspectRatio(1, 1)
            .start(this@AddPostActivity)

        image_post.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@AddPostActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //imageUri is the link or reference to the crop image
            imageUri = CropImage.getActivityResult(data).uri
            //temporary set it settings_profile_image view to see how it will look
            image_post.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Adding story")
        progressDialog.setMessage("Please wait, we are adding your story...")
        when {
            image_post == null -> Toast.makeText(
                this,
                "Please select image first",
                Toast.LENGTH_LONG
            ).show()
            post_description.text.isEmpty() -> Toast.makeText(
                this,
                "post description ca't be empty",
                Toast.LENGTH_LONG
            ).show()
            else -> {
                progressDialog.show()
                val fileRef =
                    storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        myUrl = task.result.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = ref.push().key

                        val userMap = HashMap<String, Any>()
                        userMap["postid"] = postId!!
                        userMap["description"] = post_description.text.toString()
                        userMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        userMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(userMap)

                        Toast.makeText(
                            this,
                            "your post has been successfully posted.",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }

}