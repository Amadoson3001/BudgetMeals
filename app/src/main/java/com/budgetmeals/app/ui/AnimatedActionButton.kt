package com.budgetmeals.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.budgetmeals.app.ui.icons.AppIcons
import com.budgetmeals.app.ui.theme.Motion

internal data class FabAction(val label: String, val sheet: AppSheet)

@Composable
internal fun AnimatedActionButton(
    action: FabAction?,
    visible: Boolean,
    onAction: (AppSheet) -> Unit,
    expanded: Boolean = true,
    enabled: Boolean = true,
) {
    // Retain the previous action while the FAB completes its exit (including the More tab).
    var lastAction by remember { mutableStateOf(action) }
    LaunchedEffect(action) {
        if (action != null) lastAction = action
    }
    val visibility = remember { MutableTransitionState(false) }
    visibility.targetState = visible && action != null
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = Motion.MicroSpring,
        label = "fabPress",
    )

    AnimatedVisibility(
        visibleState = visibility,
        enter = Motion.FabEnter,
        exit = Motion.FabExit,
    ) {
        val displayedAction = action ?: lastAction
        if (displayedAction != null) {
            ExtendedFloatingActionButton(
                onClick = { if (visible && enabled && action != null) onAction(action.sheet) },
                expanded = expanded,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .then(if (!expanded) Modifier.semantics { contentDescription = displayedAction.label } else Modifier),
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
                interactionSource = interactionSource,
                icon = {
                    Icon(
                        AppIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                text = {
                    AnimatedContent(
                        targetState = displayedAction.label,
                        transitionSpec = {
                            Motion.fadeThrough().using(SizeTransform(clip = false) { _, _ -> Motion.CardExpandTween })
                        },
                        contentAlignment = Alignment.CenterEnd,
                        label = "fabAction",
                    ) { label ->
                        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    }
                },
            )
        }
    }
}
