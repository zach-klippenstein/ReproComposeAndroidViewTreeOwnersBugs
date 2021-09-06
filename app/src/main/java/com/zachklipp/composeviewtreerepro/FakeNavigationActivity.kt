package com.zachklipp.composeviewtreerepro

import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zachklipp.composeviewtreerepro.FakeNavigationActivity.Companion.COMPOSE_SAVED_REGISTRY
import com.zachklipp.composeviewtreerepro.FakeNavigationActivity.Companion.COMPOSE_SAVEABLE_REGISTRY
import com.zachklipp.composeviewtreerepro.FakeNavigationActivity.Companion.NESTED_ANDROID_INSTANCE
import com.zachklipp.composeviewtreerepro.FakeNavigationActivity.Companion.NESTED_ANDROID_REGISTRY

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
      it.setContent { App(lifecycleSpy) }
      container.addView(it, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    setContentView(container)
  }

  override fun onRetainCustomNonConfigurationInstance() = lifecycleSpy

  companion object {
    const val ANDROID_INSTANCE = "Android Instance"
    const val NESTED_ANDROID_INSTANCE = "Nested Android Instance"
    const val NESTED_ANDROID_REGISTRY = "Nested Android Registry"
    const val COMPOSE_SAVEABLE_REGISTRY = "Compose Saveable Registry"
    const val COMPOSE_SAVED_REGISTRY = "Compose Saved Registry"
  }
}

@Composable
fun App(lifecycleSpy: LifecycleSpy) {
  val fakeNav = FakeNavigationContainer.remember()

  Column {
    Counters(fakeNav, lifecycleSpy)
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

@Composable private fun Counters(
  fakeNav: FakeNavigationContainer,
  lifecycleSpy: LifecycleSpy
) {
  fakeNav.Content {
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
}

@Composable private fun ActiveLifecycles(lifecycleSpy: LifecycleSpy) {
  Column {
    Text("Active lifecycles", fontWeight = FontWeight.Bold)
    Text(
      "After clicking Reset Counters, there should not be any duplicate names in this list. " +
        "If there are, that means the lifecycle for that counter leaked. " +
        "Leaks will be shown in red.",
      color = Color.Gray
    )
    Column(
      Modifier.testTag("lifecycles")
        .padding(start = 8.dp)
    ) {
      val activeLifecycles = lifecycleSpy.activeLifecycles.collectAsState(emptyList())
      activeLifecycles.value.forEach {
        val string = buildAnnotatedString {
          append("[${it.generation}] ")
          withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
            append(it.name)
          }
          append(" state=${it.state}")
        }
        Text(string, color = if (it.isLeaked) Color.Red else Color.Unspecified)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
  App(LifecycleSpy())
}
