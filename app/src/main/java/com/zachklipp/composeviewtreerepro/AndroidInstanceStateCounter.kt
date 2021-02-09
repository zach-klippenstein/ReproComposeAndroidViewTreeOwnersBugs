package com.zachklipp.composeviewtreerepro

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewTreeLifecycleOwner

/**
 * TODO write documentation
 */
@SuppressLint("SetTextI18n")
class AndroidInstanceStateCounter(context: Context) : LinearLayout(context) {

  private val text: TextView = TextView(context)
  private val button: Button = Button(context)
  private var counter: Int = 0

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

    val lifecycle = ViewTreeLifecycleOwner.get(this)!!.lifecycle
    lifecycleSpy.spyOnLifecycle(lifecycle, name)

    updateText()
  }

  override fun onSaveInstanceState(): Parcelable {
    return SavedState(counter, super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state is SavedState) {
      counter = state.counter
      updateText()
      super.onRestoreInstanceState(state.superState)
    } else {
      super.onRestoreInstanceState(state)
    }
  }

  private fun updateText() {
    text.text = "$name Counter: $counter"
  }

  private class SavedState : BaseSavedState {
    val counter: Int

    constructor(counter: Int, superState: Parcelable?) : super(superState) {
      this.counter = counter
    }

    constructor(source: Parcel) : super(source) {
      this.counter = source.readInt()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(out: Parcel, flags: Int) {
      super.writeToParcel(out, flags)
      out.writeInt(counter)
    }

    companion object CREATOR : Creator<SavedState> {
      override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
      override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
    }
  }
}
