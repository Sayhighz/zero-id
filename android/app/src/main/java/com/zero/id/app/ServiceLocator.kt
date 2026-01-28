package com.zero.id.app

import android.content.Context
import com.zero.id.app.security.ProfileStorage

object ServiceLocator {
    private var profileStorage: ProfileStorage? = null

    fun provideProfileStorage(context: Context): ProfileStorage {
        return profileStorage ?: synchronized(this) {
            profileStorage ?: ProfileStorage(context).also { profileStorage = it }
        }
    }
}
