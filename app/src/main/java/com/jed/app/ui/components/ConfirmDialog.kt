package com.jed.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.RedAlert

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(2.dp, MediumGray, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, color = OffWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(message, color = OffWhite.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MediumGray),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text(cancelText, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive) RedAlert else OrangeAccent
                        ),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text(confirmText, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
