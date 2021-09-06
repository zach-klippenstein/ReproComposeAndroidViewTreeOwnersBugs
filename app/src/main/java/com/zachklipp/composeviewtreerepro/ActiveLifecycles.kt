package com.zachklipp.composeviewtreerepro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable fun ActiveLifecycles(lifecycleSpy: LifecycleSpy) {
  Column {
    Text("Active lifecycles", fontWeight = FontWeight.Bold)
    Text(
      "After clicking Reset Counters, there should not be any duplicate names in this list. " +
        "If there are, that means the lifecycle for that counter leaked. " +
        "Leaks will be shown in red.",
      color = Color.Gray
    )
    Column(
      Modifier
        .testTag("lifecycles")
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
