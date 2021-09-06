package com.zachklipp.composeviewtreerepro

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

const val ANDROID_INSTANCE = "Android Instance"
const val NESTED_ANDROID_INSTANCE = "Nested Android Instance"
const val NESTED_ANDROID_REGISTRY = "Nested Android Registry"
const val COMPOSE_SAVEABLE_REGISTRY = "Compose Saveable Registry"
const val COMPOSE_SAVED_REGISTRY = "Compose Saved Registry"

@Composable fun Counters(lifecycleSpy: LifecycleSpy) {
  Column {
    SaveableStateRegistryCounter(COMPOSE_SAVEABLE_REGISTRY, lifecycleSpy)
    StateRegistryCounter(COMPOSE_SAVED_REGISTRY, lifecycleSpy)
    AndroidView({
      AndroidInstanceStateCounter(it).apply {
        id = R.id.nested_android_instance_state_counter
        name = NESTED_ANDROID_INSTANCE
        this.lifecycleSpy = lifecycleSpy
      }
    })
    AndroidView({
      AndroidStateRegistryCounter(it).apply {
        id = R.id.nested_android_state_registry_counter
        name = NESTED_ANDROID_REGISTRY
        this.lifecycleSpy = lifecycleSpy
      }
    })
  }
}