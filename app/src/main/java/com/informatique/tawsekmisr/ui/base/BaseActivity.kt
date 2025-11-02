package com.informatique.tawsekmisr.ui.base

import android.content.Context
import androidx.activity.ComponentActivity
import com.informatique.tawsekmisr.data.helpers.LocaleHelper
import com.informatique.tawsekmisr.ui.activities.BaseActivityEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

abstract class BaseActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // ✅ Fetch dependency manually because Hilt injection hasn't happened yet
        val entryPoint = EntryPointAccessors.fromApplication(
            newBase.applicationContext,
            BaseActivityEntryPoint::class.java
        )

        val languageDataStore = entryPoint.languageDataStore()

        val lang = runBlocking { languageDataStore.languageFlow.first() }
        val localizedContext = LocaleHelper.wrapContext(newBase, lang)
        super.attachBaseContext(localizedContext)
    }
}
