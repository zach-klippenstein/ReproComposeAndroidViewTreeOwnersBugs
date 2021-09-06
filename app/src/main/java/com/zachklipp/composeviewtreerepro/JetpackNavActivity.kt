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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class JetpackNavActivity : AppCompatActivity() {

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
      it.setContent { JetpackNavApp(lifecycleSpy) }
      container.addView(it, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    setContentView(container)
  }

  override fun onRetainCustomNonConfigurationInstance() = lifecycleSpy
}

@Composable
fun JetpackNavApp(lifecycleSpy: LifecycleSpy) {
  val navController = rememberNavController()
  val mainScreenActive = remember { MutableStateFlow(true) }

  NavHost(navController, startDestination = "one") {
    composable("one") {
      Column {
        Counters(lifecycleSpy)
        Button(onClick = {
          // Resetting should destroy and recreate the lifecycles.
          lifecycleSpy.incrementGeneration()
          navController.navigate("two")
        }) {
          Text("Reset Counters")
        }
        ActiveLifecycles(lifecycleSpy)
      }

      DisposableEffect(mainScreenActive) {
        mainScreenActive.value = true
        onDispose {
          mainScreenActive.value = false
        }
      }
    }
    composable("two") {
      Text("Resetting counters…")

      LaunchedEffect(mainScreenActive) {
        // As soon as navigation is complete, navigate back.
        mainScreenActive.first { !it }
        navController.popBackStack("one", inclusive = false)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun JetpackNavAppPreview() {
  JetpackNavApp(LifecycleSpy())
}
