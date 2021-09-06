package com.zachklipp.composeviewtreerepro

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
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
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

abstract class AbstractTests<A : ComponentActivity> {

  @get:Rule abstract val composeRule: AndroidComposeTestRule<ActivityScenarioRule<A>, A>

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

  @Test fun config_change_state_compose_saveable_registry() {
    assertComposeCounter(COMPOSE_SAVEABLE_REGISTRY, 0)
    incComposeCounter(COMPOSE_SAVEABLE_REGISTRY)
    assertComposeCounter(COMPOSE_SAVEABLE_REGISTRY, 1)

    simulateConfigChange()

    assertComposeCounter(COMPOSE_SAVEABLE_REGISTRY, 1)
  }

  @Test fun config_change_state_compose_saved_registry() {
    assertComposeCounter(COMPOSE_SAVED_REGISTRY, 0)
    incComposeCounter(COMPOSE_SAVED_REGISTRY)
    assertComposeCounter(COMPOSE_SAVED_REGISTRY, 1)

    simulateConfigChange()

    assertComposeCounter(COMPOSE_SAVED_REGISTRY, 1)
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

  @Test fun config_change_lifecycle_compose_saveable_registry() {
    assertActiveLifecycle(COMPOSE_SAVEABLE_REGISTRY, 1).assertCountEquals(1)

    simulateConfigChange()

    assertActiveLifecycle(COMPOSE_SAVEABLE_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(COMPOSE_SAVEABLE_REGISTRY, 2).assertCountEquals(1)
  }

  @Test fun config_change_lifecycle_compose_saved_registry() {
    assertActiveLifecycle(COMPOSE_SAVED_REGISTRY, 1).assertCountEquals(1)

    simulateConfigChange()

    assertActiveLifecycle(COMPOSE_SAVED_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(COMPOSE_SAVED_REGISTRY, 2).assertCountEquals(1)
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

  @Test fun navigation_state_compose_saveable_registry() {
    assertComposeCounter(COMPOSE_SAVEABLE_REGISTRY, 0)
    incComposeCounter(COMPOSE_SAVEABLE_REGISTRY)
    assertComposeCounter(COMPOSE_SAVEABLE_REGISTRY, 1)

    resetCounters()

    assertComposeCounter(COMPOSE_SAVEABLE_REGISTRY, 1)
  }

  @Test fun navigation_state_compose_saved_registry() {
    assertComposeCounter(COMPOSE_SAVED_REGISTRY, 0)
    incComposeCounter(COMPOSE_SAVED_REGISTRY)
    assertComposeCounter(COMPOSE_SAVED_REGISTRY, 1)

    resetCounters()

    assertComposeCounter(COMPOSE_SAVED_REGISTRY, 1)
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

  @Test fun navigation_lifecycle_compose_saveable_registry() {
    assertActiveLifecycle(COMPOSE_SAVEABLE_REGISTRY, 1).assertCountEquals(1)

    resetCounters()

    assertActiveLifecycle(COMPOSE_SAVEABLE_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(COMPOSE_SAVEABLE_REGISTRY, 2).assertCountEquals(1)
  }

  @Test fun navigation_lifecycle_compose_saved_registry() {
    assertActiveLifecycle(COMPOSE_SAVED_REGISTRY, 1).assertCountEquals(1)

    resetCounters()

    assertActiveLifecycle(COMPOSE_SAVED_REGISTRY, 1).assertCountEquals(0)
    assertActiveLifecycle(COMPOSE_SAVED_REGISTRY, 2).assertCountEquals(1)
  }

  // endregion

  private fun simulateConfigChange() {
    composeRule.activityRule.scenario.recreate()
  }

  private fun resetCounters() {
    composeRule.onNodeWithText("Reset Counters")
      .performClick()

    composeRule.onNodeWithText("Resetting counters…")
      .assertDoesNotExist()
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
      .filterToOne(hasText("Counter: $value", substring = true))
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
      .filter(hasText("[$generation] $name", substring = true))

  private fun inComposeCounter(name: String) =
    composeRule.onNodeWithTag(name)
      .onChildren()

  private fun inAndroidCounter(name: String) =
    withParent(withTagValue(equalTo(name)))
}
