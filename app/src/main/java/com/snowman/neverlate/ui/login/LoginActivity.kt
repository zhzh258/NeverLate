package com.snowman.neverlate.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.snowman.neverlate.MainActivity
import com.snowman.neverlate.R


class LoginActivity : AppCompatActivity() {

    private val TAG = "loginActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var loginBtn: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        if (auth.currentUser != null) {
            goHostActivity()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

        loginBtn = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener {
            login()
        }

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: ApiException) {
            Log.w(TAG, "signInWithCredential:failure", e)
            Toast.makeText(this, "Authentication failed. 1", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        // Save user data to Firestore
                        saveUserDataToFirestore(it.uid, it.email ?: "", it.displayName)
                        goHostActivity()
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed. 2", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun login() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun goHostActivity() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun saveUserDataToFirestore(userId: String, email: String, displayName: String?) {
        // Access Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Create a new user document in Firestore
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName
            // Add other user information as needed (e.g., first name, last name, etc.)
        )

        // Specify the collection and document path for the user data
        val userRef = db.collection("users").document(userId)

        // Set the user document with the user data
        userRef.set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved successfully to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving user data to Firestore: $e")
            }
    }
}