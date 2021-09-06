package com.zachklipp.composeviewtreerepro

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

/**
 * Counter implemented using Compose's [SaveableStateRegistry] by the Compose helpers built around
 * it, i.e. [rememberSaveable].
 */
@Composable fun SaveableStateRegistryCounter(name: String, lifecycleSpy: LifecycleSpy) {
  var counter by rememberSaveable { mutableStateOf(0) }

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
@Composable fun SaveableStateRegistryCounterPreview() {
  SaveableStateRegistryCounter("demo", LifecycleSpy())
}
