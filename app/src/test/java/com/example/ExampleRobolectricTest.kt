package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BoosterMode
import com.example.model.DnsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HyperGame", appName)
  }

  @Test
  fun `verify booster modes targets`() {
    assertEquals(120, BoosterMode.ULTRA_BEAST.targetFps)
    assertEquals(90, BoosterMode.ESPORTS_BALANCED.targetFps)
    assertEquals(60, BoosterMode.BATTERY_SAVER.targetFps)
  }

  @Test
  fun `verify dns providers configured`() {
    assertTrue(DnsProvider.values().any { it.primaryIp == "1.1.1.1" })
    assertTrue(DnsProvider.values().any { it.primaryIp == "8.8.8.8" })
    val cloudflare = DnsProvider.CLOUDFLARE
    assertNotNull(cloudflare.dohHostname)
    assertEquals("one.one.one.one", cloudflare.dohHostname)
  }
}
