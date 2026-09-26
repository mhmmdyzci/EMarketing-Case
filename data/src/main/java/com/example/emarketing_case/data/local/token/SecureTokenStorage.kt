package com.example.emarketing_case.data.local.token

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.repository.TokenStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import kotlin.coroutines.cancellation.CancellationException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.tokenDataStore by preferencesDataStore(name = DATASTORE_NAME)

@Singleton
internal class SecureTokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) : TokenStorage {
    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var isCacheInitialized = false

    private val cacheMutex = Mutex()

    override suspend fun save(session: AuthSession) {
        cacheMutex.withLock {
            val encryptedAccessToken = encrypt(session.accessToken)
            val encryptedRefreshToken = encrypt(session.refreshToken)

            context.tokenDataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = encryptedAccessToken
                preferences[REFRESH_TOKEN_KEY] = encryptedRefreshToken
            }
            cachedAccessToken = session.accessToken
            isCacheInitialized = true
        }
    }

    override suspend fun restoreIfNeeded() {
        if (isCacheInitialized) return

        cacheMutex.withLock {
            if (!isCacheInitialized) {
                cachedAccessToken = readSession()?.accessToken
                isCacheInitialized = true
            }
        }
    }

    override fun currentAccessToken(): String? = cachedAccessToken

    override suspend fun clear() {
        cacheMutex.withLock {
            clearStoredTokens()
            cachedAccessToken = null
            isCacheInitialized = true
        }
    }

    private suspend fun readSession(): AuthSession? =
        try {
            val preferences = context.tokenDataStore.data.first()
            val encryptedAccessToken = preferences[ACCESS_TOKEN_KEY]
            val encryptedRefreshToken = preferences[REFRESH_TOKEN_KEY]

            if (encryptedAccessToken == null && encryptedRefreshToken == null) {
                null
            } else {
                AuthSession(
                    accessToken = decrypt(requireNotNull(encryptedAccessToken)),
                    refreshToken = decrypt(requireNotNull(encryptedRefreshToken)),
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            try {
                clearStoredTokens()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
            }
            null
        }

    private suspend fun clearStoredTokens() {
        context.tokenDataStore.edit { preferences -> preferences.clear() }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        }
        val encryptedBytes = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        val encodedIv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encodedValue = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        return "$encodedIv:$encodedValue"
    }

    private fun decrypt(value: String): String {
        val parts = value.split(ENCRYPTED_VALUE_SEPARATOR, limit = 2)
        require(parts.size == 2) { "Invalid encrypted token value." }

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv),
            )
        }
        return String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build(),
                )
            }
            .generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "emarketing_session_key"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val ENCRYPTED_VALUE_SEPARATOR = ":"

        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }
}

private const val DATASTORE_NAME = "secure_token_storage"
