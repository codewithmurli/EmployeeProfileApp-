package com.employeeapp.platform

import androidx.compose.runtime.Composable
import com.employeeapp.domain.model.DocumentInfo

/**
 * Platform-specific image picker bottom sheet.
 * Android: camera + gallery with runtime permissions.
 * iOS: PHPickerViewController via UIImagePickerController.
 */
@Composable
expect fun ImagePickerBottomSheet(
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
)

/**
 * Platform-specific document picker button.
 * Android: ActivityResultContracts.OpenDocument()
 * iOS: UIDocumentPickerViewController
 */
@Composable
expect fun DocumentPickerButton(
    onDocumentSelected: (DocumentInfo) -> Unit
)
