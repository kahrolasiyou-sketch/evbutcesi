package com.example.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Formatters {
    private val turkishLocale = Locale("tr", "TR")

    private val currencyFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(turkishLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        DecimalFormat("#,##0.00", symbols)
    }

    private val integerCurrencyFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(turkishLocale).apply {
            groupingSeparator = '.'
        }
        DecimalFormat("#,##0", symbols)
    }

    fun formatCurrency(amount: Double, showDecimals: Boolean = true): String {
        return if (showDecimals && (amount % 1.0 != 0.0)) {
            "₺" + currencyFormat.format(amount)
        } else {
            "₺" + integerCurrencyFormat.format(amount)
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", turkishLocale)
        return sdf.format(timestamp)
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM", turkishLocale)
        return sdf.format(timestamp)
    }

    fun formatMonthYear(calendar: Calendar): String {
        val sdf = SimpleDateFormat("MMMM yyyy", turkishLocale)
        val formatted = sdf.format(calendar.time)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(turkishLocale) else it.toString() }
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, (month - 1).coerceIn(0, 11))
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return formatMonthYear(cal)
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", turkishLocale)
        return sdf.format(timestamp)
    }

    fun isToday(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(timestamp: Long): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }
        return yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    fun getRelativeDayString(timestamp: Long): String {
        return when {
            isToday(timestamp) -> "Bugün"
            isYesterday(timestamp) -> "Dün"
            else -> formatDate(timestamp)
        }
    }

    /**
     * ATM / POS Cihazı Mantığı ile Kuruş (Tam Sayı) Formatlama:
     * Gerçek Tutar = rawCents / 100.0
     * Örnekler:
     * rawCents = 1L -> "0,01"
     * rawCents = 18L -> "0,18"
     * rawCents = 1865035L -> "18.650,35"
     * rawCents = 125025L -> "1.250,25"
     */
    fun formatCentsToTurkishLira(cents: Long): String {
        if (cents <= 0L) return "0,00"
        val amount = cents / 100.0
        val symbols = DecimalFormatSymbols(turkishLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,##0.00", symbols)
        return formatter.format(amount)
    }

    /**
     * ATM / POS tarzı sağdan sola kayan TextFieldValue maskeleme fonksiyonu.
     *
     * Kurallar:
     * 1. Kullanıcı klavyeden sadece rakam tuşlayabilir (0-9). Nokta ve virgül devre dışıdır/yoksayılır.
     * 2. Girilen ham değer her zaman kuruş (en küçük birim) olarak kabul edilir.
     *    Matematiksel Formül: Gerçek Tutar = Girilen_Tam_Sayı / 100
     * 3. Adım adım çalışma:
     *    1 ➔ 0,01
     *    18 ➔ 0,18
     *    186 ➔ 1,86
     *    1865 ➔ 18,65
     *    18650 ➔ 186,50
     *    186503 ➔ 1.865,03
     *    1865035 ➔ 18.650,35
     *    125025 ➔ 1.250,25
     * 4. Silme (backspace) işleminde en sağdaki son basamak atılır.
     */
    fun formatAtmCurrencyWithCursor(
        newTfv: TextFieldValue,
        previous: TextFieldValue = TextFieldValue()
    ): TextFieldValue {
        val prevDigits = previous.text.filter { it.isDigit() }.trimStart('0')
        val newText = newTfv.text

        if (newText.isEmpty()) {
            return TextFieldValue("", TextRange(0))
        }

        val lengthDiff = newText.length - previous.text.length
        val cursor = newTfv.selection.end.coerceIn(0, newText.length)

        val newDigits = when {
            // Silme (backspace) işlemi: son basamağı at
            lengthDiff < 0 -> {
                if (prevDigits.isNotEmpty()) prevDigits.dropLast(1) else ""
            }
            // Çoklu karakter girişi / yapıştırma (paste)
            lengthDiff > 1 -> {
                newText.filter { it.isDigit() }.take(12)
            }
            // Tek karakter girişi
            lengthDiff == 1 && cursor > 0 -> {
                val typedChar = newText[cursor - 1]
                if (typedChar.isDigit()) {
                    (prevDigits + typedChar).take(12)
                } else {
                    // Nokta, virgül veya başka semboller tamamen yoksayılır
                    prevDigits
                }
            }
            // Değişiklik yok veya harici güncelleme
            else -> {
                val digits = newText.filter { it.isDigit() }.take(12)
                if (digits.isNotEmpty()) digits else prevDigits
            }
        }.trimStart('0')

        if (newDigits.isEmpty()) {
            return TextFieldValue("", TextRange(0))
        }

        val cents = newDigits.toLongOrNull() ?: 0L
        if (cents <= 0L) {
            return TextFieldValue("", TextRange(0))
        }

        val formatted = formatCentsToTurkishLira(cents)
        return TextFieldValue(
            text = formatted,
            selection = TextRange(formatted.length)
        )
    }

    /**
     * Tutar girişi sırasında otomatik ATM / POS para maskesi.
     */
    fun formatAmountWithCursor(
        input: TextFieldValue,
        previous: TextFieldValue = TextFieldValue()
    ): TextFieldValue {
        return formatAtmCurrencyWithCursor(input, previous)
    }

    /**
     * Tutar metnini veya rakam string'ini ATM / POS kuruş mantığına göre formatlar.
     */
    fun formatAmountInput(raw: String): String {
        if (raw.isBlank()) return ""
        val digits = raw.filter { it.isDigit() }.trimStart('0')
        if (digits.isEmpty()) return ""
        val cents = digits.toLongOrNull() ?: return ""
        return formatCentsToTurkishLira(cents)
    }

    /**
     * Sayısal (Double) tutarı ATM / POS kuruş mantığı ile Türkçe para formatına dönüştürür.
     * Örnek: 18650.35 -> "18.650,35"
     * Örnek: 1250.25 -> "1.250,25"
     * Örnek: 50000.0 -> "50.000,00"
     * Örnek: 0.01 -> "0,01"
     */
    fun formatAmountFromDouble(amount: Double): String {
        if (amount <= 0.0) return ""
        val cents = kotlin.math.round(amount * 100.0).toLong()
        return formatCentsToTurkishLira(cents)
    }

    fun formatAmountPlain(amount: Double): String {
        return formatAmountFromDouble(amount)
    }

    /**
     * ATM / POS Mantığı ile Tutar Çözümleme (Parsing):
     * Veritabanına veya hesaplama fonksiyonlarına veriyi gönderirken string manipülasyonu yapmaz.
     * Hafızadaki tam sayı (kuruş) değerini 100'e bölerek elde edilen float/decimal değeri döndürür.
     *
     * Matematiksel Formül: Gerçek Tutar = Girilen_Tam_Sayı / 100.0
     * Örnek: "18.650,35" ➔ 1865035 / 100.0 = 18650.35
     * Örnek: "1.250,25"  ➔ 125025 / 100.0 = 1250.25
     * Örnek: "0,01"      ➔ 1 / 100.0 = 0.01
     * Örnek: "0,18"      ➔ 18 / 100.0 = 0.18
     * Örnek: "1865035"   ➔ 1865035 / 100.0 = 18650.35 (ham tam sayı)
     */
    fun parseAmountInput(formatted: String): Double? {
        if (formatted.isBlank()) return null
        val trimmed = formatted.trim()

        // Eğer metinde virgül varsa, ATM/POS ekranından veya kuruşlu girdiden gelmiştir
        // Rakamları alıp 100'e bölerek tam değeri buluruz:
        // "18.650,35" -> 1865035 / 100.0 = 18650.35
        // "1.250,25"  -> 125025 / 100.0 = 1250.25
        // "0,01"      -> 1 / 100.0 = 0.01
        if (trimmed.contains(',')) {
            val digits = trimmed.filter { it.isDigit() }
            if (digits.isEmpty()) return null
            val cents = digits.toLongOrNull() ?: return null
            if (cents <= 0L) return null
            return cents / 100.0
        }

        // Eğer virgül yok ama binlik nokta varsa (örn. eski "50.000" lira girdisi)
        if (trimmed.contains('.')) {
            val noDots = trimmed.replace(".", "")
            val lira = noDots.toDoubleOrNull()
            if (lira != null && lira > 0) return lira
        }

        // Virgül ve nokta yoksa, ham kuruş tamsayısı olarak kabul et (örn. "1865035" -> 18650.35)
        val digits = trimmed.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        val cents = digits.toLongOrNull() ?: return null
        if (cents <= 0L) return null
        return cents / 100.0
    }

    /**
     * Ham tutar metnini temizler.
     * Binlik noktalarını temizler, nokta veya virgülü tek bir virgül (kuruş) ayracına dönüştürür.
     * Örnek:
     * "1250" -> "1250"
     * "1250." -> "1250,"
     * "1250.25" -> "1250,25"
     * "1.250.25" -> "1250,25"
     * "1.250,25" -> "1250,25"
     * "50.000" -> "50000"
     */
    fun cleanRawAmount(input: String): String {
        if (input.isBlank()) return ""

        val lastComma = input.lastIndexOf(',')
        val lastDot = input.lastIndexOf('.')

        val decimalIndex: Int = when {
            lastComma != -1 -> lastComma
            lastDot != -1 -> {
                val afterDot = input.substring(lastDot + 1)
                if (afterDot.isEmpty() || (afterDot.length in 1..2 && afterDot.all { it.isDigit() })) {
                    lastDot
                } else {
                    -1
                }
            }
            else -> -1
        }

        val intPartRaw = if (decimalIndex != -1) input.substring(0, decimalIndex) else input
        val decPartRaw = if (decimalIndex != -1) input.substring(decimalIndex + 1) else null

        val intDigits = intPartRaw.filter { it.isDigit() }.take(12)
        val decDigits = decPartRaw?.filter { it.isDigit() }?.take(2)

        return when {
            decimalIndex != -1 && !decDigits.isNullOrEmpty() -> {
                val prefix = if (intDigits.isEmpty()) "0" else intDigits
                "$prefix,$decDigits"
            }
            decimalIndex != -1 -> {
                val prefix = if (intDigits.isEmpty()) "0" else intDigits
                "$prefix,"
            }
            else -> intDigits
        }
    }

    /**
     * Girilen metnin ilk harfini büyük harfe dönüştürür.
     */
    fun capitalizeFirstChar(text: String): String {
        return if (text.isNotEmpty()) {
            text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(turkishLocale) else it.toString() }
        } else text
    }
}

/**
 * Jetpack Compose TextField için Türk Lirası görsel dönüştürücüsü (VisualTransformation).
 * Dahili metinde binlik noktası tutulmaz (böylece Android IME ikinci noktayı engellemez).
 * Ekranda otomatik olarak binlik noktaları (".") ve kuruş ayracı (",") gösterilir.
 */
class TurkishCurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val (formatted, rawToTrans, transToRaw) = formatWithMapping(raw)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return rawToTrans[offset.coerceIn(0, rawToTrans.size - 1)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transToRaw[offset.coerceIn(0, transToRaw.size - 1)]
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }

    companion object {
        fun formatWithMapping(raw: String): Triple<String, IntArray, IntArray> {
            val rawToTrans = IntArray(raw.length + 1)
            val formattedBuilder = StringBuilder()

            val decimalIdx = raw.indexOfAny(charArrayOf('.', ','))
            val intPart = if (decimalIdx != -1) raw.substring(0, decimalIdx) else raw
            val decPart = if (decimalIdx != -1) raw.substring(decimalIdx + 1).take(2) else null
            val hasDecimal = decimalIdx != -1

            val L = intPart.length
            val firstGroupLen = if (L == 0) 0 else if (L % 3 == 0) 3 else L % 3

            var r = 0
            // Tam sayı basamakları
            for (i in 0 until L) {
                if (i > 0 && (i - firstGroupLen) % 3 == 0) {
                    formattedBuilder.append('.')
                }
                rawToTrans[r] = formattedBuilder.length
                formattedBuilder.append(intPart[i])
                r++
            }

            if (hasDecimal) {
                rawToTrans[r] = formattedBuilder.length
                formattedBuilder.append(',')
                r++

                if (decPart != null) {
                    for (j in decPart.indices) {
                        rawToTrans[r] = formattedBuilder.length
                        formattedBuilder.append(decPart[j])
                        r++
                    }
                }
            }
            rawToTrans[r] = formattedBuilder.length

            val formatted = formattedBuilder.toString()
            val transToRaw = IntArray(formatted.length + 1)

            var lastR = 0
            for (t in 0..formatted.length) {
                while (lastR + 1 <= raw.length && rawToTrans[lastR + 1] <= t) {
                    lastR++
                }
                transToRaw[t] = lastR
            }

            return Triple(formatted, rawToTrans, transToRaw)
        }
    }
}
