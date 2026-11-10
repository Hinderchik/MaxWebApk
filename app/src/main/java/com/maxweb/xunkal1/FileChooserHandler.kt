package com.maxweb.xunkal1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Обработчик выбора файлов для WebView
 * Поддерживает: фото, видео, аудио, документы
 */
class FileChooserHandler(private val activity: Activity) {

    companion object {
        private const val FILE_CHOOSER_RESULT_CODE = 1
        private const val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 2
        private const val CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 3
        private const val IMAGE_DIRECTORY_NAME = "MaxWebApp"
    }

    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var currentPhotoUri: Uri? = null

    /**
     * Показывает диалог выбора файла
     */
    fun showFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>,
        acceptType: String,
        capture: String?
    ) {
        uploadMessage = filePathCallback
        
        val isVideo = acceptType.contains("video", ignoreCase = true)
        val isAudio = acceptType.contains("audio", ignoreCase = true)
        val isImage = acceptType.contains("image", ignoreCase = true) || acceptType == "*/*"
        
        // Если capture="environment" — сразу запускаем камеру
        if (capture == "environment" && isImage) {
            launchCamera()
            return
        }
        
        if (capture == "user" && isVideo) {
            launchVideoCamera()
            return
        }

        // Иначе показываем выбор файла
        val intent = createFileIntent(acceptType, isImage, isVideo, isAudio)
        activity.startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE)
    }

    /**
     * Создаёт Intent для выбора файлов
     */
    private fun createFileIntent(
        acceptType: String,
        isImage: Boolean,
        isVideo: Boolean,
        isAudio: Boolean
    ): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        
        val mimeTypes = mutableListOf<String>()
        
        when {
            acceptType.isNotEmpty() && acceptType != "*/*" -> {
                mimeTypes.add(acceptType)
            }
            isImage -> {
                mimeTypes.add("image/*")
            }
            isVideo -> {
                mimeTypes.add("video/*")
            }
            isAudio -> {
                mimeTypes.add("audio/*")
            }
            else -> {
                mimeTypes.add("*/*")
            }
        }

        if (mimeTypes.size > 1) {
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
        } else {
            intent.type = mimeTypes.firstOrNull() ?: "*/*"
        }

        return Intent.createChooser(intent, "Выберите файл")
    }

    /**
     * Запуск камеры для фото
     */
    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        
        // Создаём файл для сохранения
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            photoFile
        )
        
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        
        activity.startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
    }

    /**
     * Запуск камеры для видео
     */
    private fun launchVideoCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        
        // Ограничиваем размер и длительность
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60) // 60 секунд
        
        activity.startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE)
    }

    /**
     * Создаёт файл для изображения
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: activity.filesDir
        
        val directory = File(storageDir, IMAGE_DIRECTORY_NAME)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        return File.createTempFile(imageFileName, ".jpg", directory)
    }

    /**
     * Обработка результата выбора файла
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (resultCode != Activity.RESULT_OK) {
            uploadMessage?.onReceiveValue(null)
            uploadMessage = null
            return false
        }

        when (requestCode) {
            FILE_CHOOSER_RESULT_CODE -> {
                val uris = getFileUrisFromIntent(data)
                uploadMessage?.onReceiveValue(uris)
                uploadMessage = null
                return true
            }
            
            CAMERA_CAPTURE_IMAGE_REQUEST_CODE -> {
                currentPhotoUri?.let { uri ->
                    uploadMessage?.onReceiveValue(arrayOf(uri))
                    uploadMessage = null
                }
                return true
            }
            
            CAMERA_CAPTURE_VIDEO_REQUEST_CODE -> {
                val uri = data?.data
                if (uri != null) {
                    uploadMessage?.onReceiveValue(arrayOf(uri))
                    uploadMessage = null
                }
                return true
            }
        }
        
        return false
    }

    /**
     * Извлекает URI файлов из Intent
     */
    private fun getFileUrisFromIntent(data: Intent?): Array<Uri> {
        val uris = mutableListOf<Uri>()
        
        // Одиночный файл
        data?.data?.let { uris.add(it) }
        
        // Несколько файлов
        data?.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i).uri?.let { uris.add(it) }
            }
        }
        
        return if (uris.isEmpty()) {
            emptyArray()
        } else {
            uris.toTypedArray()
        }
    }

    /**
     * Получение разрешений для камеры и хранилища
     */
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO
        )
    }
}
