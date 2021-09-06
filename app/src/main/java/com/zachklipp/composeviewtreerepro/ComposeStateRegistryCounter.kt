package com.zachklipp.composeviewtreerepro

import android.os.Bundle
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.savedstate.SavedStateRegistry

/**
 * Counter implemented using the AndroidX (non-Compose) [SavedStateRegistry] from the
 * [LocalSavedStateRegistryOwner].
 */
@Composable fun StateRegistryCounter(name: String, lifecycleSpy: LifecycleSpy) {
  var counter by nonComposeSavedInstanceState { 0 }

  val lifecycle = LocalLifecycleOwner.current.lifecycle
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
 * Simplified clone of [rememberSaveable] that is implemented using the [SavedStateRegistry] from
 * the [LocalSavedStateRegistryOwner], instead of using the [SaveableStateRegistry] from the
 * [LocalSaveableStateRegistry].
 */
@OptIn(ExperimentalComposeApi::class)
@Composable private fun nonComposeSavedInstanceState(init: () -> Int): MutableState<Int> {
  val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
  val savedStateRegistry: SavedStateRegistry = savedStateRegistryOwner.savedStateRegistry
  val registryKey = "saved_instance_state:$currentCompositeKeyHash"

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
