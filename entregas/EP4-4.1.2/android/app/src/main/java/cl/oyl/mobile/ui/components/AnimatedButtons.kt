package cl.oyl.mobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Respuesta visual de presión sin retrasar la acción ni cambiar su accesibilidad. */
@Composable
private fun pressMotion(source: MutableInteractionSource): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .95f else 1f, spring(dampingRatio = .65f), label = "Presión del botón")
    return Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
}
@Composable
fun OylButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    val source = remember { MutableInteractionSource() }
    Button(onClick = onClick, modifier = modifier.then(pressMotion(source)), enabled = enabled, interactionSource = source, content = content)
}
@Composable
fun OylOutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    val source = remember { MutableInteractionSource() }
    OutlinedButton(onClick = onClick, modifier = modifier.then(pressMotion(source)), enabled = enabled, interactionSource = source, content = content)
}
@Composable
fun OylTextButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    val source = remember { MutableInteractionSource() }
    TextButton(onClick = onClick, modifier = modifier.then(pressMotion(source)), enabled = enabled, interactionSource = source, content = content)
}
@Composable
fun OylIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    val source = remember { MutableInteractionSource() }
    IconButton(onClick = onClick, modifier = modifier.then(pressMotion(source)), enabled = enabled, interactionSource = source, content = content)
}
