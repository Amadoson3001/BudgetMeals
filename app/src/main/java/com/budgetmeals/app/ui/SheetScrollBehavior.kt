package com.budgetmeals.app.ui

import android.os.SystemClock
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

internal class SheetScrollBehavior {
    val scrollStates = mutableSetOf<ScrollState>()
    var gestureStartedAtTop = true
    var returnToTopInProgress = false
    var pointerPressed = false
    var lastReturnedToTopTimestamp = 0L
    var lastDragDownTimestamp = 0L
    var lastDragY = 0f

    fun isScrolledDown(): Boolean =
        scrollStates.any { it.value > 0 || it.canScrollBackward }

    fun canStartSheetDrag(now: Long = SystemClock.uptimeMillis()): Boolean {
        if (isScrolledDown()) return false
        if (scrollStates.any { it.isScrollInProgress }) return false
        if (returnToTopInProgress) return false
        if (lastReturnedToTopTimestamp > 0L && (now - lastReturnedToTopTimestamp) in 0..150L) return false
        return true
    }

    fun onPointerDown() {
        pointerPressed = true
        gestureStartedAtTop = canStartSheetDrag()
    }

    fun onAllPointersUp(dragY: Float = 0f) {
        pointerPressed = false
        val now = SystemClock.uptimeMillis()
        if (dragY > 15f && gestureStartedAtTop) {
            lastDragDownTimestamp = now
            lastDragY = dragY
        }
        if (!isScrolledDown() && scrollStates.none { it.isScrollInProgress }) {
            returnToTopInProgress = false
            lastReturnedToTopTimestamp = now
        }
    }

    fun isRecentPhysicalDragDown(now: Long = SystemClock.uptimeMillis()): Boolean {
        if (pointerPressed && gestureStartedAtTop) return true
        return (lastDragDownTimestamp > 0L && (now - lastDragDownTimestamp) in 0..600L && lastDragY > 15f)
    }
}

private val LocalSheetScrollBehavior = staticCompositionLocalOf<SheetScrollBehavior?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScrollAwareModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    behavior: SheetScrollBehavior = remember(sheetState) { SheetScrollBehavior() },
    content: @Composable ColumnScope.() -> Unit,
) {
    // Material 3 1.3.1 keeps the platform Back callback from the dialog's creation.
    // Replacing only SheetState leaves Back targeting the previous sheet (e.g. Quick Add).
    // Keep the dialog, its predictive animation and its callbacks with the same state.
    key(sheetState) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            modifier = modifier.pointerInput(behavior) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    behavior.onPointerDown()
                    var totalDragY = 0f
                    try {
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.firstOrNull { it.id == down.id }?.let { change ->
                                totalDragY += change.positionChange().y
                            }
                        } while (event.changes.any { it.pressed })
                    } finally {
                        behavior.onAllPointersUp(totalDragY)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = {
                CompositionLocalProvider(LocalSheetScrollBehavior provides behavior) {
                    BottomSheetDefaults.DragHandle(
                        Modifier.blockSheetDragWhenScrolled().testTag("sheet_drag_handle"),
                    )
                }
            },
        ) {
            CompositionLocalProvider(LocalSheetScrollBehavior provides behavior) { content() }
        }
    }
}

/** The content consumes its scroll first; only a new gesture at the top may drag the sheet. */
@Composable
internal fun Modifier.sheetVerticalScroll(scrollState: ScrollState): Modifier {
    val behavior = LocalSheetScrollBehavior.current
    DisposableEffect(behavior, scrollState) {
        behavior?.scrollStates?.add(scrollState)
        onDispose { behavior?.scrollStates?.remove(scrollState) }
    }

    LaunchedEffect(behavior, scrollState) {
        if (behavior != null) {
            snapshotFlow { (scrollState.value > 0) to scrollState.isScrollInProgress }
                .distinctUntilChanged()
                .collect { (scrolled, inProgress) ->
                    if (scrolled) {
                        behavior.returnToTopInProgress = true
                    } else if (!inProgress && !behavior.pointerPressed) {
                        behavior.returnToTopInProgress = false
                        behavior.lastReturnedToTopTimestamp = SystemClock.uptimeMillis()
                    }
                }
        }
    }

    val connection = remember(behavior) {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val blockSheet = behavior == null || !behavior.gestureStartedAtTop ||
                    behavior.returnToTopInProgress || behavior.isScrolledDown()
                return if (blockSheet && available.y > 0f) {
                    Offset(0f, available.y)
                } else Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val blockSheet = behavior == null || !behavior.gestureStartedAtTop ||
                    behavior.returnToTopInProgress || behavior.isScrolledDown()
                return if (blockSheet && available.y > 0f) {
                    Velocity(0f, available.y)
                } else Velocity.Zero
            }
        }
    }
    return nestedScroll(connection).verticalScroll(scrollState)
}

/** Fixed headers, footers and the handle must follow the same rule without blocking taps. */
@Composable
internal fun Modifier.blockSheetDragWhenScrolled(): Modifier {
    val behavior = LocalSheetScrollBehavior.current ?: return this
    return pointerInput(behavior) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (!behavior.canStartSheetDrag()) {
                var distance = Offset.Zero
                var verticalDrag = false
                do {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    event.changes.firstOrNull { it.id == down.id }?.let { change ->
                        distance += change.positionChange()
                        if (abs(distance.y) > viewConfiguration.touchSlop && abs(distance.y) > abs(distance.x)) {
                            verticalDrag = true
                        }
                    }
                    if (verticalDrag) event.changes.forEach { it.consume() }
                } while (event.changes.any { it.pressed })
            }
        }
    }
}
