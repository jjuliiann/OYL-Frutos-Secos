package cl.oyl.mobile.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.FormField
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.Validation

@Composable
fun LoginScreen(
    busy: Boolean,
    login: (String, String, () -> Unit) -> Unit,
    register: () -> Unit,
    done: () -> Unit
) {
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ScreenTitle("Iniciar sesión", "Ingresa como cliente o administrador")
        FormField(alias, { alias = it; errors = errors - "alias" }, "Alias", error = errors["alias"])
        FormField(password, { password = it; errors = errors - "password" }, "Contraseña", password = true, error = errors["password"])
        ActionButton("Ingresar", enabled = !busy, onClick = { errors = Validation.login(alias, password); if (errors.isEmpty()) login(alias, password, done) })
        OylOutlinedButton(onClick = register, modifier = Modifier.fillMaxWidth()) { Text("Crear cuenta de cliente") }

    }
}
