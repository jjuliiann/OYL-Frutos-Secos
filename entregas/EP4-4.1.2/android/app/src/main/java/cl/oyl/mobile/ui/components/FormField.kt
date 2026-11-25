package cl.oyl.mobile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun FormField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    numeric: Boolean = false,
    password: Boolean = false,
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        trailingIcon = { if (error != null) Icon(Icons.Default.ErrorOutline, contentDescription = "Error") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = when {
            numeric -> KeyboardType.Number
            password -> KeyboardType.Password
            else -> KeyboardType.Text
        }),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None
    )
}
