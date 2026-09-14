package com.example

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ui.util.Formatters
import com.example.ui.util.TurkishCurrencyVisualTransformation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCurrencyFormatting() {
    val formatted = Formatters.formatCurrency(1250.0)
    assertTrue(formatted.contains("1.250") || formatted.contains("1250"))
  }

  @Test
  fun testTodayRecognition() {
    val now = System.currentTimeMillis()
    assertTrue(Formatters.isToday(now))
  }

  @Test
  fun testCapitalizeFirstChar() {
    assertEquals("Elektrik faturası", Formatters.capitalizeFirstChar("elektrik faturası"))
    assertEquals("Market alışverişi", Formatters.capitalizeFirstChar("market alışverişi"))
    assertEquals("", Formatters.capitalizeFirstChar(""))
  }

  @Test
  fun testUserSpecificRequirements() {
    // ATM/POS Kullanıcı İstekleri:
    // Kullanıcı 1 girdi ➔ Ham Veri: 1 ➔ Hesaplama: 1 / 100 = 0.01 ➔ Ekranda: 0,01
    // Kullanıcı 8 girdi ➔ Ham Veri: 18 ➔ Hesaplama: 18 / 100 = 0.18 ➔ Ekranda: 0,18
    // Kullanıcı 6 5 0 3 5 ➔ Ham Veri: 1865035 ➔ Hesaplama: 1865035 / 100 = 18650.35 ➔ Ekranda: 18.650,35
    var tfv = TextFieldValue("")
    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("1", TextRange(1)), tfv)
    assertEquals("0,01", tfv.text)
    assertEquals(4, tfv.selection.end)
    assertEquals(0.01, Formatters.parseAmountInput(tfv.text)!!, 0.0001)

    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("0,018", TextRange(5)), tfv)
    assertEquals("0,18", tfv.text)
    assertEquals(4, tfv.selection.end)
    assertEquals(0.18, Formatters.parseAmountInput(tfv.text)!!, 0.0001)

    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("0,186", TextRange(5)), tfv)
    assertEquals("1,86", tfv.text)

    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("1,865", TextRange(5)), tfv)
    assertEquals("18,65", tfv.text)

    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("18,650", TextRange(6)), tfv)
    assertEquals("186,50", tfv.text)

    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("186,503", TextRange(7)), tfv)
    assertEquals("1.865,03", tfv.text)

    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("1.865,035", TextRange(9)), tfv)
    assertEquals("18.650,35", tfv.text)
    assertEquals(9, tfv.selection.end)
    assertEquals(18650.35, Formatters.parseAmountInput(tfv.text)!!, 0.0001)

    // Nokta veya virgül basıldığında yoksayılmalı
    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("18.650,35,", TextRange(10)), tfv)
    assertEquals("18.650,35", tfv.text)

    tfv = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("18.650,35.", TextRange(10)), tfv)
    assertEquals("18.650,35", tfv.text)

    // 125025 yazıldığında "1.250,25" olmalı
    var tfv1250 = TextFieldValue("")
    tfv1250 = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("125025", TextRange(6)), tfv1250)
    assertEquals("1.250,25", tfv1250.text)
    assertEquals(8, tfv1250.selection.end)
    assertEquals(1250.25, Formatters.parseAmountInput(tfv1250.text)!!, 0.0001)

    // Geri silme (backspace) testi: son basamak silinmeli
    tfv1250 = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("1.250,2", TextRange(7)), tfv1250)
    assertEquals("125,02", tfv1250.text)

    tfv1250 = Formatters.formatAtmCurrencyWithCursor(TextFieldValue("125,0", TextRange(5)), tfv1250)
    assertEquals("12,50", tfv1250.text)

    // Double'dan formata çevirme testi
    assertEquals("18.650,35", Formatters.formatAmountFromDouble(18650.35))
    assertEquals("1.250,25", Formatters.formatAmountFromDouble(1250.25))
    assertEquals("50.000,00", Formatters.formatAmountFromDouble(50000.0))
    assertEquals("0,01", Formatters.formatAmountFromDouble(0.01))
  }

  @Test
  fun testAmountInputFormattingAndParsing() {
    assertEquals("0,01", Formatters.formatCentsToTurkishLira(1L))
    assertEquals("0,18", Formatters.formatCentsToTurkishLira(18L))
    assertEquals("18.650,35", Formatters.formatCentsToTurkishLira(1865035L))
    assertEquals("1.250,25", Formatters.formatCentsToTurkishLira(125025L))

    assertEquals(0.01, Formatters.parseAmountInput("0,01")!!, 0.0001)
    assertEquals(0.18, Formatters.parseAmountInput("0,18")!!, 0.0001)
    assertEquals(18650.35, Formatters.parseAmountInput("18.650,35")!!, 0.0001)
    assertEquals(1250.25, Formatters.parseAmountInput("1.250,25")!!, 0.0001)
    assertEquals(1250.25, Formatters.parseAmountInput("125025")!!, 0.0001)
  }

  @Test
  fun testCursorPositioningWhileTyping() {
    // ATM/POS Sağdan Sola Giriş:
    // 1 -> 0,01
    var tfv = TextFieldValue("")
    tfv = Formatters.formatAmountWithCursor(TextFieldValue("1", TextRange(1)), tfv)
    assertEquals("0,01", tfv.text)
    assertEquals(4, tfv.selection.end)

    // 8 -> 0,18
    tfv = Formatters.formatAmountWithCursor(TextFieldValue("0,018", TextRange(5)), tfv)
    assertEquals("0,18", tfv.text)
    assertEquals(4, tfv.selection.end)

    // 6 -> 1,86
    tfv = Formatters.formatAmountWithCursor(TextFieldValue("0,186", TextRange(5)), tfv)
    assertEquals("1,86", tfv.text)
    assertEquals(4, tfv.selection.end)

    // 5 -> 18,65
    tfv = Formatters.formatAmountWithCursor(TextFieldValue("1,865", TextRange(5)), tfv)
    assertEquals("18,65", tfv.text)
    assertEquals(5, tfv.selection.end)

    // 0 -> 186,50
    tfv = Formatters.formatAmountWithCursor(TextFieldValue("18,650", TextRange(6)), tfv)
    assertEquals("186,50", tfv.text)
    assertEquals(6, tfv.selection.end)

    // 3 -> 1.865,03
    tfv = Formatters.formatAmountWithCursor(TextFieldValue("186,503", TextRange(7)), tfv)
    assertEquals("1.865,03", tfv.text)
    assertEquals(8, tfv.selection.end)

    // 5 -> 18.650,35
    tfv = Formatters.formatAmountWithCursor(TextFieldValue("1.865,035", TextRange(9)), tfv)
    assertEquals("18.650,35", tfv.text)
    assertEquals(9, tfv.selection.end)

    // Sayı doğru parse edilmeli
    assertEquals(18650.35, Formatters.parseAmountInput(tfv.text)!!, 0.001)

    // Geri silme (backspace) testleri
    tfv = Formatters.formatAmountWithCursor(TextFieldValue("1.865,03", TextRange(8)), tfv)
    assertEquals("1.865,03", tfv.text)
    assertEquals(8, tfv.selection.end)

    tfv = Formatters.formatAmountWithCursor(TextFieldValue("186,50", TextRange(6)), tfv)
    assertEquals("186,50", tfv.text)
    assertEquals(6, tfv.selection.end)

    tfv = Formatters.formatAmountWithCursor(TextFieldValue("18,65", TextRange(5)), tfv)
    assertEquals("18,65", tfv.text)
    assertEquals(5, tfv.selection.end)

    tfv = Formatters.formatAmountWithCursor(TextFieldValue("1,86", TextRange(4)), tfv)
    assertEquals("1,86", tfv.text)
    assertEquals(4, tfv.selection.end)

    tfv = Formatters.formatAmountWithCursor(TextFieldValue("0,18", TextRange(4)), tfv)
    assertEquals("0,18", tfv.text)
    assertEquals(4, tfv.selection.end)

    tfv = Formatters.formatAmountWithCursor(TextFieldValue("0,01", TextRange(4)), tfv)
    assertEquals("0,01", tfv.text)
    assertEquals(4, tfv.selection.end)

    tfv = Formatters.formatAmountWithCursor(TextFieldValue("", TextRange(0)), tfv)
    assertEquals("", tfv.text)
    assertEquals(0, tfv.selection.end)
  }

  @Test
  fun testCleanRawAmount() {
    assertEquals("1250", Formatters.cleanRawAmount("1250"))
    assertEquals("1250,", Formatters.cleanRawAmount("1250."))
    assertEquals("1250,", Formatters.cleanRawAmount("1250,"))
    assertEquals("1250,2", Formatters.cleanRawAmount("1250.2"))
    assertEquals("1250,25", Formatters.cleanRawAmount("1250.25"))
    assertEquals("1250,25", Formatters.cleanRawAmount("1.250.25"))
    assertEquals("1250,25", Formatters.cleanRawAmount("1.250,25"))
    assertEquals("50000", Formatters.cleanRawAmount("50000"))
    assertEquals("50000", Formatters.cleanRawAmount("50.000"))
    assertEquals("18256,25", Formatters.cleanRawAmount("18.256.25"))
  }

  @Test
  fun testTurkishCurrencyVisualTransformation() {
    val vt = TurkishCurrencyVisualTransformation()

    // 1250 -> "1.250"
    val res1 = vt.filter(AnnotatedString("1250"))
    assertEquals("1.250", res1.text.text)
    assertEquals(5, res1.offsetMapping.originalToTransformed(4))

    // 1250, -> "1.250,"
    val res2 = vt.filter(AnnotatedString("1250,"))
    assertEquals("1.250,", res2.text.text)
    assertEquals(6, res2.offsetMapping.originalToTransformed(5))

    // 1250,25 -> "1.250,25"
    val res3 = vt.filter(AnnotatedString("1250,25"))
    assertEquals("1.250,25", res3.text.text)
    assertEquals(8, res3.offsetMapping.originalToTransformed(7))

    // 50000 -> "50.000"
    val res4 = vt.filter(AnnotatedString("50000"))
    assertEquals("50.000", res4.text.text)
    assertEquals(6, res4.offsetMapping.originalToTransformed(5))

    // parse test
    assertEquals(1250.25, Formatters.parseAmountInput(res3.text.text)!!, 0.001)
    assertEquals(50000.0, Formatters.parseAmountInput(res4.text.text)!!, 0.001)
  }
}

