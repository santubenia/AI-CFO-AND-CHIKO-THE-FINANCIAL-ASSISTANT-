package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class AuthUserState(
    val isLoggedIn: Boolean = false,
    val isAnonymous: Boolean = true,
    val uid: String = "guest_user",
    val email: String = "guest@coe.app",
    val displayName: String = "CoE Member",
    val photoUrl: String? = null,
    val errorMessage: String? = null
)

class AuthManager(private val context: Context) {

    private var firebaseAuth: FirebaseAuth? = null
    private val _authState = MutableStateFlow(AuthUserState())
    val authState: StateFlow<AuthUserState> = _authState

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
                val current = firebaseAuth?.currentUser
                if (current != null) {
                    updateFromFirebaseUser(current)
                }
            } else {
                Log.d("AuthManager", "FirebaseApp not initialized, using local offline guest mode")
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Firebase Auth init failed, running in offline mode", e)
        }
    }

    private fun updateFromFirebaseUser(user: FirebaseUser) {
        _authState.value = AuthUserState(
            isLoggedIn = true,
            isAnonymous = user.isAnonymous,
            uid = user.uid,
            email = user.email ?: "investor@coe.app",
            displayName = user.displayName ?: "CoE Portfolio Member",
            photoUrl = user.photoUrl?.toString(),
            errorMessage = null
        )
    }

    /**
     * Signs in with Google using Jetpack CredentialManager and GetSignInWithGoogleOption.
     * Required standard identity provider in this environment.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<Boolean> = withContext(Dispatchers.Main) {
        try {
            val auth = firebaseAuth ?: throw IllegalStateException("Firebase is not initialized")
            val webClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                throw IllegalStateException("default_web_client_id is not available in resources", e)
            }

            val credentialManager = CredentialManager.create(activityContext)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                if (user != null) {
                    updateFromFirebaseUser(user)
                    Result.success(true)
                } else {
                    Result.failure(IllegalStateException("No Firebase user returned"))
                }
            } else {
                Result.failure(IllegalStateException("Unexpected credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("AuthManager", "User cancelled Google Sign-In prompt")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign-In failed", e)
            _authState.value = _authState.value.copy(errorMessage = e.localizedMessage)
            Result.failure(e)
        }
    }

    fun continueAsGuest() {
        _authState.value = AuthUserState(
            isLoggedIn = true,
            isAnonymous = true,
            uid = "guest_offline",
            email = "offline_guest@coe.app",
            displayName = "Offline Guest",
            errorMessage = null
        )
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        _authState.value = AuthUserState(
            isLoggedIn = false,
            isAnonymous = true,
            uid = "guest_user",
            email = "guest@coe.app",
            displayName = "CoE Member"
        )
    }
}
