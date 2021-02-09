package com.zachklipp.composeviewtreerepro

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasSubstring
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zachklipp.composeviewtreerepro.MainActivity.Companion.ANDROID_INSTANCE
import com.zachklipp.composeviewtreerepro.MainActivity.Companion.COMPOSE_REGISTRY
import com.zachklipp.composeviewtreerepro.MainActivity.Companion.COMPOSE_UI_REGISTRY
import com.zachklipp.composeviewtreerepro.MainActivity.Companion.NESTED_ANDROID_INSTANCE
import com.zachklipp.composeviewtreerepro.MainActivity.Companion.NESTED_ANDROID_REGISTRY
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReproTests {

  @Rule @JvmField val composeRule = createAndroidComposeRule<MainActivity>()

  // region Config Changes

  @Test fun config_change_state_android_instance() {
    assertAndroidCounter(ANDROID_INSTANCE, 0)
    incAndroidCounter(ANDROID_INSTANCE)
    assertAndroidCounter(ANDROID_INSTANCE, 1)

    simulateConfigChange()

    assertAndroidCounter(ANDROID_INSTANCE, 1)
  }

  @Test fun config_change_state_nested_android_instance() {
    assertAndroidCounter(NESTED_ANDROID_INSTANCE, 0)
    incAndroidCounter(NESTED_ANDROID_INSTANCE)
    assertAndroidCounter(NESTED_ANDROID_INSTANCE, 1)

    simulateConfigChange()

    assertAndroidCounter(NESTED_ANDROID_INSTANCE, 1)
  }

  @Test fun config_change_state_nested_android_registry() {
    assertAndroidCounter(NESTED_ANDROID_REGISTRY, 0)
    incAndroidCounter(NESTED_ANDROID_REGISTRY)
    assertAndroidCounter(NESTED_ANDROID_REGISTRY, 1)

    simulateConfigChange()

    assertAndroidCounter(NESTED_ANDROID_REGISTRY, 1)
  }

  @Test fun config_change_state_compose_ui_registry() {
    assertComposeCounter(COMPOSE_UI_REGISTRY, 0)
    incComposeCounter(COMPOSE_UI_REGISTRY)
    assertComposeCounter(COMPOSE_UI_REGISTRY, 1)

    simulateConfigChange()

    assertComposeCounter(COMPOSE_UI_REGISTRY, 1)
  }

  @Test fun config_change_state_compose_registry() {
    assertComposeCounter(COMPOSE_REGISTRY, 0)
    incComposeCounter(COMPOSE_REGISTRY)
    assertComposeCounter(COMPOSE_REGISTRY, 1)

    simulateConfigChange()

    assertComposeCounter(COMPOSE_REGISTRY, 1)
  }

  @Test fun config_change_lifecycle_android_instance() {
    assertActiveLifecycle(ANDROID_INSTANCE, 1).assertCountEquals(1)

    simulateConfigChange()

    assertActiveLifecycle(ANDROID_INSTANCE, 1).assertCountEquals(0)
    assertActiveLifecycle(ANDROID_INSTANCE, 2).assertCountEquals(1)
  }

  @Test fun config_change_lifecycle_nested_android_instance() {
    assertActiveLifecycle(NESTED_ANDROID_INSTANCE, 1).assertCountEquals(1)

    simulateConfigChange()

    assertActiveLifecycle(NESTED_ANDROID_INSTANCE, 1).assertCountEquals(0)
    assertActiveLifecycle(NESTED_ANDROID_INSTANCE, 2).assertCountEquals(1)
  }

  @Test fun config_change_lifecycle_nested_android_registry() {
    assertActiveLifecycle(NESTED_ANDROID_REGISTRY, 1).assertCountEquals(1)

    simulateConfigChange()

    assertActiveLifecycle(NESTED_ANDROID_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(NESTED_ANDROID_REGISTRY, 2).assertCountEquals(1)
  }

  @Test fun config_change_lifecycle_compose_ui_registry() {
    assertActiveLifecycle(COMPOSE_UI_REGISTRY, 1).assertCountEquals(1)

    simulateConfigChange()

    assertActiveLifecycle(COMPOSE_UI_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(COMPOSE_UI_REGISTRY, 2).assertCountEquals(1)
  }

  @Test fun config_change_lifecycle_compose_registry() {
    assertActiveLifecycle(COMPOSE_REGISTRY, 1).assertCountEquals(1)

    simulateConfigChange()

    assertActiveLifecycle(COMPOSE_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(COMPOSE_REGISTRY, 2).assertCountEquals(1)
  }

  // endregion
  // region Fake Navigation

  @Test fun navigation_state_android_instance() {
    assertAndroidCounter(ANDROID_INSTANCE, 0)
    incAndroidCounter(ANDROID_INSTANCE)
    assertAndroidCounter(ANDROID_INSTANCE, 1)

    resetCounters()

    assertAndroidCounter(ANDROID_INSTANCE, 1)
  }

  @Test fun navigation_state_nested_android_instance() {
    assertAndroidCounter(NESTED_ANDROID_INSTANCE, 0)
    incAndroidCounter(NESTED_ANDROID_INSTANCE)
    assertAndroidCounter(NESTED_ANDROID_INSTANCE, 1)

    resetCounters()

    assertAndroidCounter(NESTED_ANDROID_INSTANCE, 1)
  }

  @Test fun navigation_state_nested_android_registry() {
    assertAndroidCounter(NESTED_ANDROID_REGISTRY, 0)
    incAndroidCounter(NESTED_ANDROID_REGISTRY)
    assertAndroidCounter(NESTED_ANDROID_REGISTRY, 1)

    resetCounters()

    assertAndroidCounter(NESTED_ANDROID_REGISTRY, 1)
  }

  @Test fun navigation_state_compose_ui_registry() {
    assertComposeCounter(COMPOSE_UI_REGISTRY, 0)
    incComposeCounter(COMPOSE_UI_REGISTRY)
    assertComposeCounter(COMPOSE_UI_REGISTRY, 1)

    resetCounters()

    assertComposeCounter(COMPOSE_UI_REGISTRY, 1)
  }

  @Test fun navigation_state_compose_registry() {
    assertComposeCounter(COMPOSE_REGISTRY, 0)
    incComposeCounter(COMPOSE_REGISTRY)
    assertComposeCounter(COMPOSE_REGISTRY, 1)

    resetCounters()

    assertComposeCounter(COMPOSE_REGISTRY, 1)
  }

  @Test fun navigation_lifecycle_android_instance() {
    assertActiveLifecycle(ANDROID_INSTANCE, 1).assertCountEquals(1)

    resetCounters()

    assertActiveLifecycle(ANDROID_INSTANCE, 1).assertCountEquals(0)
    assertActiveLifecycle(ANDROID_INSTANCE, 2).assertCountEquals(1)
  }

  @Test fun navigation_lifecycle_nested_android_instance() {
    assertActiveLifecycle(NESTED_ANDROID_INSTANCE, 1).assertCountEquals(1)

    resetCounters()

    assertActiveLifecycle(NESTED_ANDROID_INSTANCE, 1).assertCountEquals(0)
    assertActiveLifecycle(NESTED_ANDROID_INSTANCE, 2).assertCountEquals(1)
  }

  @Test fun navigation_lifecycle_nested_android_registry() {
    assertActiveLifecycle(NESTED_ANDROID_REGISTRY, 1).assertCountEquals(1)

    resetCounters()

    assertActiveLifecycle(NESTED_ANDROID_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(NESTED_ANDROID_REGISTRY, 2).assertCountEquals(1)
  }

  @Test fun navigation_lifecycle_compose_ui_registry() {
    assertActiveLifecycle(COMPOSE_UI_REGISTRY, 1).assertCountEquals(1)

    resetCounters()

    assertActiveLifecycle(COMPOSE_UI_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(COMPOSE_UI_REGISTRY, 2).assertCountEquals(1)
  }

  @Test fun navigation_lifecycle_compose_registry() {
    assertActiveLifecycle(COMPOSE_REGISTRY, 1).assertCountEquals(1)

    resetCounters()

    assertActiveLifecycle(COMPOSE_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(COMPOSE_REGISTRY, 2).assertCountEquals(1)
  }

  // endregion

  private fun simulateConfigChange() {
    composeRule.activityRule.scenario.recreate()
  }

  private fun resetCounters() {
    composeRule.onNodeWithText("Reset Counters")
      .performClick()
  }

  private fun assertAndroidCounter(name: String, value: Int) {
    onView(allOf(withSubstring("Counter: $value"), inAndroidCounter(name)))
      .check(matches(isDisplayed()))
  }

  private fun incAndroidCounter(name: String) {
    onView(allOf(withText("+"), inAndroidCounter(name)))
      .perform(click())
  }

  private fun assertComposeCounter(name: String, value: Int) {
    inComposeCounter(name)
      .filterToOne(hasSubstring("Counter: $value"))
      .assertIsDisplayed()
  }

  private fun incComposeCounter(name: String) {
    inComposeCounter(name)
      .filterToOne(hasText("+"))
      .performClick()
  }

  private fun assertActiveLifecycle(name: String, generation: Int) =
    composeRule.onNodeWithTag("lifecycles")
      .onChildren()
      .filter(hasSubstring("[$generation] $name"))

  private fun inComposeCounter(name: String) =
    composeRule.onNodeWithTag(name)
      .onChildren()

  private fun inAndroidCounter(name: String) =
    withParent(withTagValue(equalTo(name)))
}
