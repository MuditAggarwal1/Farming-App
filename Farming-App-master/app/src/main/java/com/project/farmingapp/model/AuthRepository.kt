package com.project.farmingapp.model

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

class AuthRepository {

    lateinit var googleSignInClient: GoogleSignInClient
    val firebaseAuth = FirebaseAuth.getInstance()
    lateinit var firebaseDb: FirebaseFirestore
    val data = MutableLiveData<String>()
    fun signInWithEmail(
        email: String,
        password: String,
        otherData: HashMap<String, Serializable?>
    ): LiveData<String> {

        firebaseDb = FirebaseFirestore.getInstance()

        val data2 = MutableLiveData<String>()
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseDb!!.collection("users").document("${email}")
                    .set(otherData)
                    .addOnSuccessListener {
                        data.value = "Success"
                    }
                    .addOnFailureListener { Exception ->
                        {
                            data.value = "Failure"
                        }
                    }

            } else if (it.isCanceled) {
                data.value = "Failure"
            }
        }.addOnFailureListener {
            Log.d("AuthRepo", it.message)
            data.value = it.message
        }
        return data
    }

    fun signInToGoogle(
        idToken: String,
        email: String,
        otherData: HashMap<String, Serializable?>
    ): LiveData<String> {
        firebaseDb = FirebaseFirestore.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val userDocRef = firebaseDb!!.collection("users").document("${email}")

                    userDocRef.get().addOnSuccessListener {
                        data.value = "Success"
                        if(it.exists()){
                            Log.d("User", "User Exists")
                        } else{
                            Log.d("User", "User Does not Exists")
                            firebaseDb!!.collection("users").document("${email}")
                                .set(otherData)
                                .addOnSuccessListener {
                                    data.value = "Success"
                                }
                                .addOnFailureListener { Exception ->
                                    {
                                        data.value = "Failure"
                                    }
                                }
                        }
                    }

                    val user = firebaseAuth!!.currentUser
                } else {
                    data.value = "Failure"
                }
            }

        return data
    }


    //login
    fun logInWithEmail(
        email: String,
        password: String
    ): LiveData<String> {

        firebaseDb = FirebaseFirestore.getInstance()

        val data = MutableLiveData<String>()
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                data.value = "Success"

            } else if (it.isCanceled) {
                data.value = "Failure"
            }

        }.addOnFailureListener {
            Log.d("AuthRepo", it.message)
            data.value = it.message
        }
        return data
    }
}