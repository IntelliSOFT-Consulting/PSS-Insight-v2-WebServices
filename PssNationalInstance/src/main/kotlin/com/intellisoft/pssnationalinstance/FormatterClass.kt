package com.intellisoft.pssnationalinstance

import org.springframework.http.ResponseEntity
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class FormatterClass {
    fun extractName(emailAddress: String): String{
        return emailAddress.substringBefore("@")
    }
    fun getRemainingTime(dateString: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateString, formatter)

        // Convert to Instant and calculate duration between current time and given date
        val instant = dateTime.toInstant(ZoneOffset.UTC)
        val duration = Duration.between(Instant.now(), instant)

        // Calculate remaining time in days, hours, minutes, and seconds
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60

        // Build remaining time string
        val sb = StringBuilder()
        if (days > 0) sb.append("$days days, ")
        sb.append("$hours hours, $minutes minutes, and $seconds seconds remaining")
        return sb.toString()
    }
    fun isDateFormatValid(dateString: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        try {
            formatter.parse(dateString)
        } catch (e: DateTimeParseException) {
            return false
        }
        return true
    }
    fun isEmailValid(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }
    fun isPastToday(dateString: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateString, formatter)
        val today = LocalDate.now()
        val date = dateTime.toLocalDate()
        return date.isBefore(today)
    }
    fun getOtp():String{

        // Using numeric values
        val rnd = Random()
        val number = rnd.nextInt(999999)

        return String.format("%06d", number);

    }
    fun getNextVersion(list:List<Any>):Int {

        val intList = list.map { it.toString().toIntOrNull() }
        val filteredList = intList.filterIsInstance<Int>()

        return filteredList.maxOrNull() ?: 1
    }
    fun getResponse(results: Results): ResponseEntity<*>? {
        return when (results.code) {
            200, 201 -> {
                ResponseEntity.ok(results.details)
            }
            500 -> {
                ResponseEntity.internalServerError().body(results)
            }
            else -> {
                ResponseEntity.badRequest().body(DbDetails(results.details.toString()))
            }
        }
    }
}