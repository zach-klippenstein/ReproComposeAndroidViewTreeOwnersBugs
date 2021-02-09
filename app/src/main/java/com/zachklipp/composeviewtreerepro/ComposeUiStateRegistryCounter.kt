package com.zachklipp.composeviewtreerepro

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.UiSavedStateRegistry
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

/**
 * Counter implemented using Compose's [UiSavedStateRegistry] by the Compose helpers built around
 * it, i.e. [savedInstanceState].
 */
@Composable fun UiStateRegistryCounter(name: String, lifecycleSpy: LifecycleSpy) {
  var counter by savedInstanceState { 0 }

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
@Composable fun UiStateRegistryCounterPreview() {
  UiStateRegistryCounter("demo", LifecycleSpy())
}
