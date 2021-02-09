package com.zachklipp.composeviewtreerepro

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.ViewTreeSavedStateRegistryOwner

private const val BUNDLE_STATE_KEY = "state"

/**
 * TODO write documentation
 */
@SuppressLint("SetTextI18n")
class AndroidStateRegistryCounter(context: Context) : LinearLayout(context) {

  private val text: TextView = TextView(context)
  private val button: Button = Button(context)
  private lateinit var savedStateRegistry: SavedStateRegistry
  private var counter: Int = -1

  private val registryKey get() = "${AndroidStateRegistryCounter::class.java.name}:$id"

  var name: String = ""
    set(value) {
      field = value
      tag = value
    }

  lateinit var lifecycleSpy: LifecycleSpy

  init {
    orientation = HORIZONTAL
    addView(text, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    addView(button, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

    button.text = "+"
    button.setOnClickListener {
      counter++
      updateText()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    check(id != View.NO_ID) { "Expected view to have an ID." }

    val savedStateRegistryOwner = ViewTreeSavedStateRegistryOwner.get(this)!!
    // TODO factor out
    savedStateRegistry = savedStateRegistryOwner.savedStateRegistry
    val restoredBundle = savedStateRegistry.consumeRestoredStateForKey(registryKey)
    counter = restoredBundle?.getInt(BUNDLE_STATE_KEY) ?: 0

    savedStateRegistry.registerSavedStateProvider(registryKey) {
      Bundle().apply { putInt(BUNDLE_STATE_KEY, counter) }
    }

    val lifecycle = ViewTreeLifecycleOwner.get(this)!!.lifecycle
    lifecycleSpy.spyOnLifecycle(lifecycle, name)

    updateText()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    savedStateRegistry.unregisterSavedStateProvider(registryKey)
  }

  private fun updateText() {
    text.text = "$name Counter: $counter"
  }
}
