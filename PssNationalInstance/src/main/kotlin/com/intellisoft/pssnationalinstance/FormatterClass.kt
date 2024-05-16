package com.intellisoft.pssnationalinstance

import org.springframework.http.ResponseEntity
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.*

class FormatterClass {

    fun getValue(): String {
        val props = Properties()
        val inputStream = javaClass.classLoader
            .getResourceAsStream("application.properties")
        props.load(inputStream);
        return props.getProperty("server-url")
    }
    fun getValueDetails(): Triple<String, String, String> {
        val props = Properties()
        val inputStream = javaClass.classLoader
            .getResourceAsStream("application.properties")
        props.load(inputStream)
        val username = props.getProperty("dhis.username")
        val password = props.getProperty("dhis.password")
        val internationalUrl = props.getProperty("dhis.international")

        return Triple(username, password, internationalUrl)
    }


    fun getNewDays(): String {
        val now = LocalDateTime.now()
        val later = now.plusDays(3)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return later.format(formatter)
    }
    fun isExpiredToday(dateString: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd[ HH:mm:ss]")
        val today = LocalDate.now()
        val dateStr = LocalDate.parse(dateString, formatter)
        val dayNo = (ChronoUnit.DAYS.between(today, dateStr)).toInt()
        if (dayNo == 0){
            return true
        }
        return false
    }
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
        if (days > 0)
        sb.append("$hours hours")
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
        val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";
        return EMAIL_REGEX.toRegex().matches(email);

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

    fun getMasterTemplate(): DbApplicationValues{
        val props = Properties()
        val inputStream = javaClass.classLoader
                .getResourceAsStream("application.properties")
        props.load(inputStream)
        val username = props.getProperty("dhis.username")
        val password = props.getProperty("dhis.password")
        val internationalUrl = props.getProperty("dhis.international")
        val program = props.getProperty("dhis.program")
        val template = props.getProperty("dhis.template")

        return DbApplicationValues(internationalUrl, username, password, program, template)
    }
}