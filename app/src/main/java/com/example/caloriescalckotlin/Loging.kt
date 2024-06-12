package com.example.caloriescalckotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

class Loging : AppCompatActivity() {
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth

    private var mAuth: FirebaseAuth? = null

    // [END declare_auth]
    private var mGoogleSignInClient: GoogleSignInClient? = null
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loging)
        auth = Firebase.auth

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null){
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException){
                Log.d("authLog", "Api exception")
            }
        }
    }

    private fun getClient():GoogleSignInClient{
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun signIgWithGoogle(){
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener{
            if(it.isSuccessful){
                Log.d("authLog", "Google signIn done")
                val intent = Intent(this, Calculate::class.java)
                startActivity(intent)
            } else {
                Log.d("authLog", "Google signIn error")
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
        if (currentUser != null) {
            Companion.signedInAccountId = currentUser.uid
            Log.d("UserEmail", currentUser.email!!)
            val intent = Intent(this, Calculate::class.java)
            startActivity(intent)
        }
    }

    fun singOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Companion.signedInAccountId = user.uid
            val intent = Intent(this, Calculate::class.java)
            startActivity(intent)
        }
    }
//
//    val signedInAccountId: String?
//        get() = Companion.signedInAccountId
//
    fun onClickSingUp(view: View?) {
        signIgWithGoogle()
        //Intent intent = new Intent(this, Main.class);
        //startActivity(intent);
    }
//
    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
        var signedInAccountId: String? = null
    }
}