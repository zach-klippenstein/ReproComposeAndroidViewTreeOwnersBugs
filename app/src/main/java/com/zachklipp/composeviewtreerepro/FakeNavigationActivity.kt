package com.zachklipp.composeviewtreerepro

import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview

class FakeNavigationActivity : AppCompatActivity() {

  private lateinit var lifecycleSpy: LifecycleSpy

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val container = LinearLayout(this)
    container.orientation = VERTICAL

    lifecycleSpy = (lastCustomNonConfigurationInstance as LifecycleSpy?) ?: LifecycleSpy()
    lifecycleSpy.incrementGeneration()

    AndroidInstanceStateCounter(this).also {
      it.id = R.id.android_instance_state_counter
      it.name = ANDROID_INSTANCE
      it.lifecycleSpy = lifecycleSpy
      container.addView(it, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    ComposeView(this).also {
      it.id = R.id.compose_view
      it.setContent { FakeNavigationApp(lifecycleSpy) }
      container.addView(it, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    setContentView(container)
  }

  override fun onRetainCustomNonConfigurationInstance() = lifecycleSpy
}

@Composable
fun FakeNavigationApp(lifecycleSpy: LifecycleSpy) {
  val fakeNav = FakeNavigationContainer.remember()

  Column {
    fakeNav.Content {
      Counters(lifecycleSpy)
    }
    Button(onClick = {
      // Resetting should destroy and recreate the lifecycles.
      lifecycleSpy.incrementGeneration()
      fakeNav.reset()
    }) {
      Text("Reset Counters")
    }
    ActiveLifecycles(lifecycleSpy)
  }
}

@Preview(showBackground = true)
@Composable
fun FakeNavigationAppPreview() {
  FakeNavigationApp(LifecycleSpy())
}
