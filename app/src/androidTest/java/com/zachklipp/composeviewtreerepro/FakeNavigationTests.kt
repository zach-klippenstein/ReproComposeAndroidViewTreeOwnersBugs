package com.zachklipp.composeviewtreerepro

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FakeNavigationTests : AbstractTests<FakeNavigationActivity>() {
  override val composeRule = createAndroidComposeRule<FakeNavigationActivity>()
}
