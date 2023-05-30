
package com.thenotesgiver.smooth_share.app

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

/**
 * created by: mahadev-code
 * date: 19.09.2018 10:11 AM
 */

@HiltAndroidApp
class FileExplorer : MultiDexApplication()  {



    override fun onCreate() {
        super.onCreate()
        instance = this


    }



    companion object {
        private var instance: FileExplorer? = null

        fun getContext(): FileExplorer {
            return instance!!
        }

    }
}
