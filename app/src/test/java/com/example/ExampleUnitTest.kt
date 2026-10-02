package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPackageNameValidation() {
    val validRegex = Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$")
    assertTrue(validRegex.matches("com.example.webapp"))
    assertTrue(validRegex.matches("com.company.my_app_123"))
    assertFalse(validRegex.matches("invalid"))
    assertFalse(validRegex.matches("com.example..empty"))
    assertFalse(validRegex.matches("com.123start.num"))
  }

  @Test
  fun testProjectTemplatesAvailability() {
    val templates = com.example.data.model.ProjectTemplates.templates
    assertTrue(templates.isNotEmpty())
    val emptyTmpl = templates.find { it.id == "empty" }
    assertNotNull(emptyTmpl)
    assertTrue(emptyTmpl!!.defaultFiles.containsKey("index.html"))
    assertTrue(emptyTmpl.defaultFiles.containsKey("css/style.css"))
    assertTrue(emptyTmpl.defaultFiles.containsKey("js/app.js"))
  }
}
