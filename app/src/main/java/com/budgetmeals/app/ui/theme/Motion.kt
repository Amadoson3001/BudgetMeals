package com.budgetmeals.app.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

object Motion {
    const val FastMs = 150
    const val ShortMs = 150
    const val MediumMs = 250
    const val LongMs = 300
    const val NavigationMs = 350

    // Material 3 Emphasized easing curves
    val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    val FadeTween: TweenSpec<Float> = tween(ShortMs, easing = EmphasizedDecelerate)
    val ColorTween: TweenSpec<Color> = tween(ShortMs, easing = EmphasizedDecelerate)
    val SettleBackTween: TweenSpec<Float> = tween(200, easing = EmphasizedDecelerate)
    val ProgressTween: TweenSpec<Float> = tween(LongMs, easing = EmphasizedDecelerate)
    val CardExpandTween: TweenSpec<IntSize> = tween(MediumMs, easing = EmphasizedDecelerate)

    // Restrained spring feedback; large surfaces use emphasized easing instead.
    val MicroSpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val PlacementSpring: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    // Material fade through: the outgoing content clears before the new content appears.
    fun fadeThrough(): ContentTransform =
        (fadeIn(tween(210, delayMillis = 90, easing = EmphasizedDecelerate)) +
            scaleIn(tween(LongMs, easing = EmphasizedDecelerate), initialScale = 0.92f))
            .togetherWith(fadeOut(tween(90, easing = EmphasizedAccelerate)))

    // A short shared X axis reinforces forward/back navigation without full-screen travel.
    fun sharedAxisX(forward: Boolean, distancePx: Int): ContentTransform {
        val direction = if (forward) 1 else -1
        return (slideInHorizontally(tween(NavigationMs, easing = Emphasized)) { direction * distancePx } +
            fadeIn(tween(210, delayMillis = 90, easing = EmphasizedDecelerate)))
            .togetherWith(
                slideOutHorizontally(tween(NavigationMs, easing = Emphasized)) { -direction * distancePx } +
                    fadeOut(tween(90, easing = EmphasizedAccelerate)),
            )
    }

    val FabEnter: EnterTransition =
        fadeIn(tween(ShortMs, easing = EmphasizedDecelerate)) +
            scaleIn(tween(LongMs, easing = EmphasizedDecelerate), initialScale = 0.6f) +
            slideInVertically(tween(LongMs, easing = EmphasizedDecelerate)) { it / 3 }
    val FabExit: ExitTransition =
        fadeOut(tween(ShortMs, easing = EmphasizedAccelerate)) +
            scaleOut(tween(ShortMs, easing = EmphasizedAccelerate), targetScale = 0.8f)

    val ExpandEnter: EnterTransition =
        expandVertically(tween(MediumMs, easing = EmphasizedDecelerate), expandFrom = Alignment.Top) +
            fadeIn(tween(ShortMs, easing = EmphasizedDecelerate))
    val ExpandExit: ExitTransition =
        shrinkVertically(tween(MediumMs, easing = EmphasizedAccelerate), shrinkTowards = Alignment.Top) +
            fadeOut(tween(ShortMs, easing = EmphasizedAccelerate))

    // Directional side-by-side continuous slide for bottom navigation tabs.
    // Pager-driven navigation retains continuous, gesture-following movement.
    fun tabEnter(forward: Boolean): EnterTransition =
        if (forward) {
            slideInHorizontally(tween(durationMillis = NavigationMs, easing = Emphasized)) { fullWidth -> fullWidth }
        } else {
            slideInHorizontally(tween(durationMillis = NavigationMs, easing = Emphasized)) { fullWidth -> -fullWidth }
        }

    fun tabExit(forward: Boolean): ExitTransition =
        if (forward) {
            slideOutHorizontally(tween(durationMillis = NavigationMs, easing = Emphasized)) { fullWidth -> -fullWidth }
        } else {
            slideOutHorizontally(tween(durationMillis = NavigationMs, easing = Emphasized)) { fullWidth -> fullWidth }
        }

    fun tabSlide(forward: Boolean): ContentTransform =
        tabEnter(forward) togetherWith tabExit(forward)

    val TabEnter: EnterTransition = tabEnter(true)
    val TabExit: ExitTransition = tabExit(true)

    // Full-travel slide for detail screens (100% width)
    val DetailEnterForward: EnterTransition =
        slideInHorizontally(tween(durationMillis = MediumMs, easing = EmphasizedDecelerate)) { fullWidth -> fullWidth }

    val DetailExitForward: ExitTransition =
        fadeOut(tween(durationMillis = ShortMs, easing = EmphasizedAccelerate))

    val DetailEnterBackward: EnterTransition =
        fadeIn(tween(durationMillis = ShortMs, easing = EmphasizedDecelerate))

    val DetailExitBackward: ExitTransition =
        slideOutHorizontally(tween(durationMillis = MediumMs, easing = EmphasizedAccelerate)) { fullWidth -> fullWidth }

    // Smooth transition for content swapping inside a bottom sheet (e.g. QuickAdd -> Form)
    val SheetContentEnter: EnterTransition =
        fadeIn(tween(210, delayMillis = 90, easing = EmphasizedDecelerate)) +
            slideInVertically(tween(LongMs, easing = Emphasized)) { it / 12 }

    val SheetContentExit: ExitTransition =
        fadeOut(tween(90, easing = EmphasizedAccelerate)) +
            slideOutVertically(tween(LongMs, easing = Emphasized)) { -it / 12 }

    // Aliases for compatibility
    val SlideInFullForward: EnterTransition = DetailEnterForward
    val SlideInFullBackward: EnterTransition = DetailEnterBackward
    val DriftExitForward: ExitTransition = DetailExitForward
    val DriftExitBackward: ExitTransition = DetailExitBackward
    val GestureSettleEnter: EnterTransition = fadeIn(FadeTween)
    val GesturePopExit: ExitTransition = ExitTransition.None
}
