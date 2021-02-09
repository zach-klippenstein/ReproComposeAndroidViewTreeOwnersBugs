# Description

This repository contains an Android Compose project that demonstrates a number of issues in the
Compose <-> Android View integration as of Compose 1.0.0-alpha11.

## Components

This project reproduces a number of integration scenarios and has UI tests for all of them.
All scenarios are versions of the same basic setup. They interact with both state saving and
lifecycles. Each scenario is a view/composable component that displays a counter and lets you
increment the counter by clicking a button. Each scenario attempts to save and restore its counter
value using some view state mechanism. Each scenario also gets the nearest `Lifecycle` and observes
it until it's destroyed.

The individual components are:

 - **Android Instance**: Android `View` hosted directly in the main view tree, with no `ComposeView`
   above it, using `onSaveInstanceState` and `onRestoreInstanceState`. This is effectively the
   control case, since no Compose is involved.
 - **Nested Android Instance**: Android `View` nested inside a `ComposeView`, using
   `onSaveInstanceState`/`onRestoreInstanceState`.
 - **Nested Android Registry**: Android `View` nested inside a `ComposeView`, using
   `ViewTreeSavedStateRegistryOwner` to save/restore its counter.
 - **Compose UI Registry**: A Composable using the standard Compose idioms for saving state,
   `saveInstanceState()` (uses `UiSavedStateRegistry` under the hood).
 - **Compose Registry**: A Composable using the `AmbientSavedStateRegistry` to get the nearest
   non-Compose `SavedStateRegistry`.

## Restoration scenarios

There are two cases that are tested using this project:

 - Entire activity is destroyed and recreated, e.g. for a config change.
 - Simple navigation container (`FakeNavigationContainer`) written in Compose simulates navigating
   away from and then back to a screen containing all of the components listed above except the control. This container uses `RestorableStateProvider` to save its childrens' states, and also provides a separate `Lifecycle` for its content each time a fake "navigation" event is triggered using `AmbientLifecycleOwner`.

# Run the UI tests

The easiest way to see what's wrong is to simply load the project in Android Studio and run the
`ReproTests` test suite in `androidTest`.

# Issues

The following issues are demonstrated by this project:

 1. Android `onRestoreInstanceState` for Android views nested in ComposeViews is very broken, even
    when all known views have their IDs set:
    1. It's not called after config changes.
    2. It's not called when restoring a composition from a `UiSavedStateRegistry`.
 2. Nested Android views don't see a custom state registry passed through the
    `AmbientUiSavedStateRegistry`. They instead see the `SavedStateRegistry` of the composition's
    `ComposeView`.
      - Proposed fix: `AndroidView` should read `AmbientUiSavedStateRegistry`, wrap it in a
        `SavedStateRegistry`, and set it as the `ViewTreeSavedStateRegistryOwner` on the nested
        view.
 3. The `Lifecycle` provided by the `AmbientLifecycleOwner` is never seen by nested Android views.
    This is demonstrated by the lifecycles from the Android components "leaking" in the repro app.
      - Proposed fix: `AndroidView` should read the `AmbientLifecycleOwner` and set it as the
        `ViewTreeLifecycleOwner` on the nested view.
 4. There are two sources of truth for saved state in compose: `AmbientUiSavedStateRegistry` and
    `AmbientSavedStateRegistry`. This contributes to 1.1 above, and also makes the
    "Compose Registry" case possible.
      - This isn't really a _bug_ per say, it's a code smell that seems likely to cause bugs as
        demonstrated here.
      - Proposed fix: Since both `UiSavedStateRegistry` and `SavedStateRegistry` have essentially
        the same API shape, and `ComposeView` already wraps the incoming `SavedStateRegistry` as a
        `UiSavedStateRegistry`, it seems to me like `AmbientSavedStateRegistry` should simply not
        exist, and instead, when a `SavedStateRegistry` is required, it should wrap the
        `UiSavedStateRegistry` from the ambient.

# Impact

These issues will [likely affect](https://android-review.googlesource.com/c/platform/frameworks/support/+/1577946)
the Jetpack Navigation library (which sets the `SavedStateRegistry` ambient), as well as any other
navigation libraries that are written in Compose, and try to do the right thing by providing
appropriate lifecycle and saved state registries via the Compose ambients.
