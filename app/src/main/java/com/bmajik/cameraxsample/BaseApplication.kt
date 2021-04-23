package com.bmajik.cameraxsample

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

class BaseApplication: Application(),CameraXConfig.Provider {
    override fun onCreate() {
        super.onCreate()

    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}