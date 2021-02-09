package com.zachklipp.composeviewtreerepro

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Stateful helper for spying on the lifecycles of views and composables.
 */
@Suppress("SuspiciousCollectionReassignment")
@Stable
class LifecycleSpy {

  private var observers = MutableStateFlow(emptyList<NamedObserver>())
  private var generation = 0

  val activeLifecycles: Flow<List<LifecycleInfo>>
    get() = observers.map { list ->
      list.toLifecycleInfos()
    }

  fun incrementGeneration() {
    generation++
  }

  fun spyOnLifecycle(lifecycle: Lifecycle, name: String) {
    NamedObserver(generation, name) { observers.value -= it }
      .also {
        observers.value += it
        lifecycle.addObserver(it)
      }
  }

  private fun List<NamedObserver>.toLifecycleInfos(): List<LifecycleInfo> {
    val generationsByName = groupBy { it.name }
      .mapValues { (_, v) -> v.map { it.generation } }

    return map { namedObserver ->
      LifecycleInfo(
        generation = namedObserver.generation,
        name = namedObserver.name,
        state = namedObserver.state,
        isLeaked = generationsByName.getValue(namedObserver.name).let {
          it.size > 1 && namedObserver.generation != it.maxOrNull()
        }
      )
    }
  }

  @Immutable
  data class LifecycleInfo(
    val generation: Int,
    val name: String,
    val state: Lifecycle.State,
    val isLeaked: Boolean
  )

  private class NamedObserver(
    val generation: Int,
    val name: String,
    private val onDestroyed: (NamedObserver) -> Unit
  ) : LifecycleEventObserver {
    var state = State.INITIALIZED
      private set

    override fun onStateChanged(source: LifecycleOwner, event: Event) {
      state = event.targetState
      if (event == ON_DESTROY) onDestroyed(this)
    }
  }
}
