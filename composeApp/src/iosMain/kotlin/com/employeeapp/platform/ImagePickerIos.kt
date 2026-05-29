package com.employeeapp.platform

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.employeeapp.domain.model.DocumentInfo

// ─────────────────────────────────────────────────────────────────────────────
// iOS actual — Image Picker
// TODO: Wire to PHPickerViewController / UIImagePickerController via interop
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun ImagePickerBottomSheet(
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(bottom = 40.dp)) {
            Text(
                "Select Profile Photo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider()
            Text(
                "iOS photo picker — connect PHPickerViewController via Swift interop.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// iOS actual — Document Picker
// TODO: Wire to UIDocumentPickerViewController via Swift interop
// ─────────────────────────────────────────────────────────────────────────────
@Composable
actual fun DocumentPickerButton(onDocumentSelected: (DocumentInfo) -> Unit) {
    OutlinedButton(
        onClick = { /* TODO: UIDocumentPickerViewController interop */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.FileUpload, null)
        Spacer(Modifier.width(8.dp))
        Text("Upload Resume (PDF / DOC / DOCX)")
    }
}
