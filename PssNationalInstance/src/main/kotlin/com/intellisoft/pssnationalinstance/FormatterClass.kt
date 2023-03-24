package com.intellisoft.pssnationalinstance

import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class FormatterClass {

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