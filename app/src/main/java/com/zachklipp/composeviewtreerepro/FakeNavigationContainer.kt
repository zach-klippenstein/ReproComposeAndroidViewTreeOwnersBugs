package com.zachklipp.composeviewtreerepro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.Providers
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.AmbientUiSavedStateRegistry
import androidx.compose.runtime.savedinstancestate.ExperimentalRestorableStateHolder
import androidx.compose.runtime.savedinstancestate.UiSavedStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AmbientLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * A composable that implements the state save/restore and lifecycle functionality of a typical
 * backstack container. It doesn't actually have a history or anything.
 *
 * The content is composed directly, but allows the caller to force the content be skipped and then
 * re-composed from scratch. The content is provided a
 * [AmbientUiSavedStateRegistry] and a [AmbientLifecycleOwner].
 * The state registry can be used by the child to retain state, and the lifecycle will be destroyed
 * and re-created when the content is reset.
 */
@OptIn(ExperimentalRestorableStateHolder::class, ExperimentalCoroutinesApi::class)
@Stable
class FakeNavigationContainer private constructor(
  private var restoredValues: Map<String, List<Any?>>?,
  private val canBeSaved: (Any) -> Boolean
) {

  private var resetting by mutableStateOf(false)
  private var contentLifecycle = ContentLifecycleOwner()

  private var stateRegistry: UiSavedStateRegistry =
    UiSavedStateRegistry(restoredValues, canBeSaved = canBeSaved)

  @Composable fun Content(content: @Composable () -> Unit) {
    if (!resetting) {
      val parentLifecycle = AmbientLifecycleOwner.current.lifecycle
      DisposableEffect(parentLifecycle) {
        parentLifecycle.addObserver(contentLifecycle)
        onDispose {
          parentLifecycle.removeObserver(contentLifecycle)
        }
      }

      // Can't use Unit because it's not Bundleable.
      Providers(
        AmbientLifecycleOwner provides contentLifecycle,
        AmbientUiSavedStateRegistry provides stateRegistry,
        content = content
      ) /*{
        // // TODO do this manually to demonstrate more issues
        // stateHolder.RestorableStateProvider(key = "", content)
      }*/
    } else {
      // Save childrens' state before skipping it.
      restoredValues = stateRegistry.performSave()

      // Skip the actual content this composition pass, reset the lifecycle, then trigger another
      // composition that will re-compose the content.
      SideEffect {
        // Destroy and re-create the lifecycle owner to ensure that any code that kept a reference
        // to the owner doesn't see the new lifecycle.
        // In a real navigation container, this would be done on navigation.
        contentLifecycle.destroy()
        contentLifecycle = ContentLifecycleOwner()

        stateRegistry = UiSavedStateRegistry(restoredValues, canBeSaved = canBeSaved)

        // Trigger another composition pass to compose the content again.
        resetting = false
      }
    }
  }

  /**
   * Forces the content displayed by a [FakeNavigationContainer] to be skipped and then recomposed.
   */
  fun reset() {
    // Setting this property will trigger a recomposition on the next frame.
    resetting = true
  }

  private fun save(): Map<String, List<Any?>>? {
    restoredValues = stateRegistry.performSave()
    return restoredValues
  }

  private class ContentLifecycleOwner : LifecycleOwner, LifecycleEventObserver {
    private val lifecycle = LifecycleRegistry(this)

    fun destroy() {
      lifecycle.currentState = DESTROYED
    }

    override fun onStateChanged(source: LifecycleOwner, event: Event) {
      if (lifecycle.currentState != DESTROYED) {
        lifecycle.currentState = event.targetState
      }
    }

    override fun getLifecycle(): Lifecycle = lifecycle
  }

  companion object {
    @OptIn(ExperimentalComposeApi::class)
    @Composable fun remember(): FakeNavigationContainer {
      // Can't use RestorableStateHolder because we need to be able to ask for its saved values and
      // recreate manually.
      val parentRegistry = AmbientUiSavedStateRegistry.current
      val parentKey = currentComposer.currentCompoundKeyHash.toString()
      val container = remember {
        @Suppress("UNCHECKED_CAST")
        val restoredValues = parentRegistry?.consumeRestored(parentKey) as Map<String, List<Any?>>?
        FakeNavigationContainer(restoredValues,
          canBeSaved = { parentRegistry?.canBeSaved(it) ?: true })
      }

      if (parentRegistry != null) {
        DisposableEffect(Unit) {
          val valueProvider = { container.save() }
          parentRegistry.registerProvider(parentKey, valueProvider)
          onDispose {
            parentRegistry.unregisterProvider(parentKey, valueProvider)
          }
        }
      }

      return container
    }
  }
}
