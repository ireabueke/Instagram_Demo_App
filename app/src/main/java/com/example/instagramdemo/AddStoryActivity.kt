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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*

class AddStoryActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageStoryRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        //address to store story images in  database
        storageStoryRef = FirebaseStorage.getInstance().reference.child("Story Pictures")

        //call to get corp image from phone
        CropImage.activity()
            .setAspectRatio(9, 16)
            .start(this@AddStoryActivity)

    }

    //call by CropImage to get the crop image value
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //imageUri is the link or reference to the crop image
            imageUri = CropImage.getActivityResult(data).uri

            uploadStory()
        }
    }

    /**
     * upload the image to firebase
     * with reference to the user id, story id, time it was uploaded and time the story will appear and disappear**/
    private fun uploadStory() {
        when {
            imageUri == null -> Toast.makeText(this, "Please select image first", Toast.LENGTH_LONG)
                .show()

            else -> {
                //dialog to let the user know the image is been uploaded so he/she have to wait
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding story")
                progressDialog.setMessage("Please wait, we are adding your story...")
                progressDialog.show()

                //adding image to folder with name Story picture in StorageReference it file extension as .jpg
                val fileRef =
                    storageStoryRef!!.child(System.currentTimeMillis().toString() + ".jpg")
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
                        //reference that later retrieve in home fragment and passed to tory model
                        val stroryRef = FirebaseDatabase.getInstance().reference.child("Story")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        val storyId = stroryRef.push().key.toString()

                        val timeEnd =
                            System.currentTimeMillis() + 86400000//add one day to the current day

                        //populating story data in database
                        val storyMap = HashMap<String, Any>()
                        storyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
                        storyMap["timestart"] = ServerValue.TIMESTAMP
                        storyMap["timeend"] = timeEnd
                        storyMap["imageurl"] = myUrl
                        storyMap["storyid"] = storyId


                        stroryRef.child(storyId).updateChildren(storyMap)

                        Toast.makeText(
                            this,
                            "your post has been successfully posted.",
                            Toast.LENGTH_LONG
                        ).show()
                        //redirect the user to the main page
                        val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
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
