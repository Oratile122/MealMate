package com.example.mealmate

// FirebaseAuthManager - helper class that wraps Firebase Authentication
// Handles: register, login, Google SSO, logout, and session checks

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class FirebaseAuthManager(private val context: Context) {

    private val TAG = "FirebaseAuthManager"

    // Firebase Authentication instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Google Sign-In client - configured for Firebase Auth
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Returns the currently logged-in user (or null)
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Returns true if a user is currently signed in
    fun isLoggedIn(): Boolean = auth.currentUser != null

    // Returns the Google Sign-In intent (used to launch the Google picker)
    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    /**
     * Registers a new user with email and password.
     * Also sets the display name.
     * Calls onResult(true) on success, or onResult(false, errorMessage) on failure.
     */
    fun register(
        email: String,
        password: String,
        displayName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Update the display name
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    user?.updateProfile(profileUpdate)?.addOnCompleteListener {
                        Log.d(TAG, "User registered: ${user.email}")
                        onResult(true, null)
                    }
                } else {
                    val error = task.exception?.message ?: "Registration failed"
                    Log.e(TAG, "Registration error: $error")
                    onResult(false, error)
                }
            }
    }

    /**
     * Logs in an existing user with email and password.
     * Calls onResult(true) on success, or onResult(false, errorMessage) on failure.
     */
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User logged in: ${auth.currentUser?.email}")
                    onResult(true, null)
                } else {
                    val error = task.exception?.message ?: "Login failed"
                    Log.e(TAG, "Login error: $error")
                    onResult(false, error)
                }
            }
    }

    /**
     * Signs in with a Google ID token (received from the Google Sign-In activity).
     * Calls onResult(true) on success.
     */
    fun signInWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Google sign-in successful: ${auth.currentUser?.email}")
                    onResult(true, null)
                } else {
                    val error = task.exception?.message ?: "Google sign-in failed"
                    Log.e(TAG, "Google sign-in error: $error")
                    onResult(false, error)
                }
            }
    }

    /**
     * Signs out from both Firebase and Google.
     * The callback runs when both sign-outs are done.
     */
    fun logout(onComplete: () -> Unit) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "User logged out")
            onComplete()
        }
    }
}