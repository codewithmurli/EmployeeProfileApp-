package com.employeeapp.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.employeeapp.domain.model.DocumentInfo
import java.io.File

// ─────────────────────────────────────────────────────────────────────────────
// Android actual — Image Picker Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun ImagePickerBottomSheet(
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                onImageSelected(uri.toString())
                onDismiss()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onImageSelected(it.toString())
            onDismiss()
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera(context, cameraLauncher) { pendingCameraUri = it }
        }
    }

    val storagePermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) galleryLauncher.launch("image/*")
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(bottom = 40.dp)) {
            Text(
                text = "Select Profile Photo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Take Photo") },
                supportingContent = { Text("Use your camera") },
                leadingContent = {
                    Icon(Icons.Default.CameraAlt, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp))
                },
                modifier = Modifier.clickable {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) launchCamera(context, cameraLauncher) { pendingCameraUri = it }
                    else cameraPermLauncher.launch(Manifest.permission.CAMERA)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Choose from Gallery") },
                supportingContent = { Text("Pick from your photos") },
                leadingContent = {
                    Icon(Icons.Default.Photo, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp))
                },
                modifier = Modifier.clickable {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        galleryLauncher.launch("image/*")
                    } else {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) galleryLauncher.launch("image/*")
                        else storagePermLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Android actual — Document Picker
// ─────────────────────────────────────────────────────────────────────────────
@Composable
actual fun DocumentPickerButton(onDocumentSelected: (DocumentInfo) -> Unit) {
    val context = LocalContext.current

    val documentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                val name = if (nameIndex >= 0) cursor.getString(nameIndex) ?: "document" else "document"
                val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                onDocumentSelected(DocumentInfo(uri.toString(), name, size, mimeType))
            }
        }
    }

    OutlinedButton(
        onClick = {
            documentLauncher.launch(
                arrayOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.FileUpload, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Upload Resume (PDF / DOC / DOCX)")
    }
}

// ── helpers ──────────────────────────────────────────────────────────────────

private fun launchCamera(
    context: Context,
    launcher: ActivityResultLauncher<Uri>,
    onUri: (Uri) -> Unit
) {
    val file = File(context.cacheDir, "photos").also { it.mkdirs() }
        .let { File.createTempFile("IMG_${System.currentTimeMillis()}_", ".jpg", it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    onUri(uri)
    launcher.launch(uri)
}
