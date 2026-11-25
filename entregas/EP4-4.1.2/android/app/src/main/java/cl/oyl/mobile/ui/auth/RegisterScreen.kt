package cl.oyl.mobile.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.model.auth.RegisterData
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.FormField
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.Validation

@Composable
fun RegisterScreen(
    busy: Boolean,
    register: (RegisterData, () -> Unit) -> Unit,
    done: () -> Unit
) {
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var name by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Crear cuenta", "Completa tus datos para crear tu cuenta")
        FormField(name, { name = it; errors = errors - "name" }, "Nombre completo", error = errors["name"])
        FormField(alias, { alias = it; errors = errors - "alias" }, "Alias único", error = errors["alias"])
        FormField(password, { password = it; errors = errors - "password" }, "Contraseña (mínimo 8)", password = true, error = errors["password"])
        FormField(email, { email = it; errors = errors - "email" }, "Correo electrónico", error = errors["email"])
        FormField(phone, { phone = it; errors = errors - "phone" }, "Teléfono", error = errors["phone"])
        FormField(address, { address = it; errors = errors - "address" }, "Dirección", error = errors["address"])
        ActionButton("Registrar cliente", enabled = !busy, onClick = {
            val data = RegisterData(alias.trim(), password, name.trim(), email.trim(), phone.trim(), address.trim())
            errors = Validation.register(data)
            if (errors.isEmpty()) register(data, done)
        })
    }
}
