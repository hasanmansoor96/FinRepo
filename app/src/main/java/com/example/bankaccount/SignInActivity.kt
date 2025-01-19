package com.example.bankaccount

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    // Register the result launcher for FirebaseUI Sign-In
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Sign-in successful
            val response = IdpResponse.fromResultIntent(result.data)
            val user = FirebaseAuth.getInstance().currentUser
            navigateToMainActivity() // Navigate to the main screen
        } else {
            // Sign-in failed
            val response = IdpResponse.fromResultIntent(result.data)
            response?.error?.let {
                // Log or handle the error (e.g., show a toast message)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is already signed in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already signed in; navigate to MainActivity
            navigateToMainActivity()
        } else {
            // Launch the FirebaseUI Sign-In flow
            launchSignInFlow()
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build() // Add Google as a sign-in provider
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false) // Disable Smart Lock for simplicity
            .build()

        signInLauncher.launch(signInIntent) // Launch the FirebaseUI Sign-In flow
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close SignInActivity
    }
}
