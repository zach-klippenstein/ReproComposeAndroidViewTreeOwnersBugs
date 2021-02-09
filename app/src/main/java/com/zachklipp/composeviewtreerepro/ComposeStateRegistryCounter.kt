package com.zachklipp.composeviewtreerepro

import android.os.Bundle
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.AmbientUiSavedStateRegistry
import androidx.compose.runtime.savedinstancestate.UiSavedStateRegistry
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientLifecycleOwner
import androidx.compose.ui.platform.AmbientSavedStateRegistryOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.savedstate.SavedStateRegistry

/**
 * Counter implemented using the AndroidX (non-Compose) [SavedStateRegistry] from the
 * [AmbientSavedStateRegistryOwner].
 */
@Composable fun StateRegistryCounter(name: String, lifecycleSpy: LifecycleSpy) {
  var counter by nonComposeSavedInstanceState { 0 }

  val lifecycle = AmbientLifecycleOwner.current.lifecycle
  DisposableEffect(lifecycle, lifecycleSpy, name) {
    lifecycleSpy.spyOnLifecycle(lifecycle, name)
    onDispose {}
  }

  Row(Modifier.testTag(name)) {
    Text("$name Counter: $counter")
    Button(onClick = { counter++ }) {
      Text("+")
    }
  }
}

@Preview(showBackground = true)
@Composable fun StateRegistryCounterPreview() {
  StateRegistryCounter("demo", LifecycleSpy())
}

private const val BUNDLE_STATE_KEY = "state"

/**
 * Simplified clone of [savedInstanceState] that is implemented using the [SavedStateRegistry] from
 * the [AmbientSavedStateRegistryOwner], instead of using the [UiSavedStateRegistry] from the
 * [AmbientUiSavedStateRegistry].
 */
@OptIn(ExperimentalComposeApi::class)
@Composable private fun nonComposeSavedInstanceState(init: () -> Int): MutableState<Int> {
  val savedStateRegistryOwner = AmbientSavedStateRegistryOwner.current
  val savedStateRegistry: SavedStateRegistry = savedStateRegistryOwner.savedStateRegistry
  val registryKey = "saved_instance_state:${currentComposer.currentCompoundKeyHash}"

  // Restore or create the initial value.
  val state: MutableState<Int> = remember {
    val restoredBundle = savedStateRegistry.consumeRestoredStateForKey(registryKey)
    // This bundle will only ever hold one value so no need to make it unique.
    val initialValue = restoredBundle?.getInt(BUNDLE_STATE_KEY) ?: init()
    mutableStateOf(initialValue)
  }

  // Register with the registry.
  DisposableEffect(Unit) {
    savedStateRegistry.registerSavedStateProvider(registryKey) {
      Bundle().apply { putInt(BUNDLE_STATE_KEY, state.value) }
    }
    onDispose {
      savedStateRegistry.unregisterSavedStateProvider(registryKey)
    }
  }

  return state
}
