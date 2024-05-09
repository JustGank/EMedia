package com.xjl.emedia.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class EMediaPermissionUtil {
    companion object {

        fun getAllNotGrantedPermissions(context: Context): Array<String> {
            val permissions = mutableSetOf<String>()
            permissions.addAll(getPickerNotGrantedPermissions(context))
            permissions.addAll(getCameraNotGrantedPermissions(context))
            return permissions.toTypedArray()
        }

        fun getPickerNotGrantedPermissions(context: Context): Array<String> {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                }
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_MEDIA_VIDEO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    }
                }
            }
            permissions.addAll(getManagerExternalStorageNotGrantedPermissions(context))
            return permissions.toTypedArray()
        }

        fun getCameraNotGrantedPermissions(context: Context): Array<String> {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            )
            permissions.addAll(getManagerExternalStorageNotGrantedPermissions(context))
            return permissions.toTypedArray()
        }

        fun getManagerExternalStorageNotGrantedPermissions(context: Context): Array<String> {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            return permissions.toTypedArray()
        }
    }
}