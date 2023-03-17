package com.intellisoft.nationalinstance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.http.*
import org.springframework.util.Base64Utils
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

class FormatterClass {

    fun getNextVersion(list:List<Any>):Int {

        val intList = list.map { it.toString().toIntOrNull() }
        val filteredList = intList.filterIsInstance<Int>()

        return filteredList.maxOrNull() ?: 1
    }

    fun sendMail(emailAddress: String,
                 customUrl: String){

        CoroutineScope(Dispatchers.IO).launch {
            sendSurveyLink(emailAddress, customUrl)
        }
    }
    private suspend fun sendSurveyLink(
        emailAddress: String,
        customUrl: String){
        coroutineScope {
            launch(Dispatchers.IO){


            }
        }
    }
    fun getIndicatorName(indicatorName: String): String{

        var name = ""
        when (indicatorName) {
            "PS01" -> {
                name = "Existence of a national essential medicines list published within the past five years"
            }
            "PS02" -> {
                name = "Existence of a reimbursement list published within the past two years"
            }
            "PS03" -> {
                name = "% of median international price paid for a set of tracer medicines that was part of the last regular MOH procurement"
            }
            "PS04" -> {
                name = "Mean % availability across a basket of medicines"
            }
            "PS05" -> {
                name = "Product losses by value due to expired medicines or damage or theft per value received (%)"
            }
            "PS06" -> {
                name = "% Generic medicines out of total market volume"
            }
            "PS07" -> {
                name = "Defined daily dose (DDD) for antimicrobials (per 1000 population)"
            }
            "PS08" -> {
                name = "% Medicines prescribed from an EML or reimbursement list"
            }
            "PS09" -> {
                name = "% Medicines prescribed as generics"
            }
            "PS10" -> {
                name = "% Antibiotics prescribed in outpatient settings"
            }
            "PS11" -> {
                name = "% Population with unmet medicine needs"
            }

            "PLG01" -> {
                name = "An institutional development plan of the national medicines regulatory authority based on the results of the GBT exists"
            }
            "PLG02" -> {
                name = "A progress report on the institutional development of the national medicines regulatory authority published"
            }
            "PLG03" -> {
                name = "Submission of national data to the Global Antimicrobial Resistance Surveillance System (GLASS)"
            }
            "PLG04" -> {
                name = "Updated National Action Plan on the containment of antimicrobial resistance"
            }
            "PLG05" -> {
                name = "# of annual reports submitted to the INCB in last five years"
            }
            "PLG06" -> {
                name = "Pharmaceutical System Transparency and Accountability (PSTA) assessment score"
            }
            "PLG07" -> {
                name = "Number of PSTA assessments within the last five years"
            }

            "RS01" -> {
                name = "% of manufacturing facilities inspected each year"
            }
            "RS02" -> {
                name = "% of distribution facilities inspected each year"
            }
            "RS03" -> {
                name = "% of dispensing facilities inspected each year"
            }
            "RS04" -> {
                name = "Average number of days for decision making on a medicine application for registration"
            }
            "RS05" -> {
                name = "% of medicines on the EML that have at least one registered product available."
            }
            "RS06" -> {
                name = "% of recorded adverse event reports that are assessed for causality"
            }
            "RS07" -> {
                name = "% of samples tested that failed quality control testing"
            }

            "IRDMT01" -> {
                name = "Pharmaceutical innovation goals identified and documented to address unmet or inadequately met public health needs"
            }
            "IRDMT02" -> {
                name = "Are medicines subject to import tariffs? If so, what are the tariff amounts applied?"
            }
            "IRDMT03" ->{
                name = "Have any of the following TRIPS flexibilities been utilized to date: compulsory licensing provisions, government use, parallel importation provisions, the Bolar exception (10 year time frame)?"
            }

            "F01" ->{
                name = "Per capita expenditure on pharmaceuticals"
            }
            "F02" ->{
                name = "Population with household expenditures on health greater than 10% of total household expenditure or income"
            }
            "F03" ->{
                name = "Total expenditure on pharmaceuticals (% total expenditure on health)"
            }
            "F04" ->{
                name = "Median (consumer) drug price ratio for tracer medicines in the public, private, and mission sectors"
            }
            "F05" ->{
                name = "Out-of-pocket expenditure out of total pharmaceutical expenditure"
            }
            "F06" ->{
                name = "At least one national health accounts exercise including pharmaceuticals completed in the past five years. "
            }

            "HR01" ->{
                name = "Existence of governing bodies tasked with accreditation of pre- and in-service pharmacy training programs "
            }
            "HR02" ->{
                name = "Population per licensed pharmacist, pharmacy technician, or pharmacy assistant"
            }

            "IM01" ->{
                name = "Existence of a policy or strategy that sets standards for collection and management of pharmaceutical information"
            }
            "IM02" ->{
                name = "Data on safety, efficacy, and cost effectiveness of medicines available and used to inform essential medicines selection"
            }

            "OA01" ->{
                name = "GBT Maturity Level(s)"
            }
            "OA02" ->{
                name = "MedMon outputs on Affordability and Availablity of pharmaceutical products"
            }
            "OA03" ->{
                name = "Proportion of health facilities that have a core set of relevant essential medicines available and affordable on a sustainable basis. (SDG indicator 3.b.3)"
            }
            "OA04" ->{
                name = "Proportion of population with large household expenditure on health as a share of total household expenditure or income. (SDG indicator 3.8.2)"
            }
            "OA05" ->{
                name = "Coverage of essential health services. (SDG indicator 3.8.1)"
            }

        }
        return name

    }

    fun mapIndicatorNameToCategory(indicatorName: String): String {
        val categoryMap = mapOf(
            "PS" to "Pharmaceutical Products and Services",
            "PLG" to "Policy Laws and Governance",
            "RS" to "Regulatory Systems",
            "IRDMT" to "Innovation, Research and Development, Manufacturing, and Trade",
            "F" to "Financing",
            "HR" to "Human Resources",
            "IM" to "Information",
            "OA" to "Outcomes and Attributes"
        )
        val prefix = indicatorName.takeWhile { it.isLetter() }
        return categoryMap[prefix] ?: "Others"
    }



    fun getHeaders(): HttpEntity<String?>? {
        val headers = HttpHeaders()
        headers["Authorization"] = "Basic " +
                Base64Utils.encodeToString("admin:district".toByteArray())
        return HttpEntity(headers)
    }
     fun saveSelectedMetadataToNationalInstance(
         internationalUrl: String,
         dataStore: String,
         masterTemplate: String,
         nationalUrl: String,
         version: String,
         restTemplate: RestTemplate
     ) {

         CoroutineScope(Dispatchers.IO).launch {

             // Get the metadata json
             val templateUrl: String = "$internationalUrl$dataStore$masterTemplate/$version"
             val dbAllTemplateResponseEntity: ResponseEntity<DbAllTemplate> = restTemplate.exchange<DbAllTemplate>(
                 templateUrl,
                 HttpMethod.GET, getHeaders(), DbAllTemplate::class.java
             )
             if (dbAllTemplateResponseEntity.statusCode == HttpStatus.OK) {
                 val dbAllTemplate = dbAllTemplateResponseEntity.body

                 // Create the request headers
                 val headers = HttpHeaders()
                 val auth = "admin:district"
                 val encodedAuth = Base64.getEncoder().encode(auth.toByteArray(StandardCharsets.UTF_8))
                 val authHeader = "Basic $encodedAuth"
                 headers.add("Authorization", authHeader)
                 val nationalTemplateUrl: String = "$nationalUrl$dataStore$masterTemplate/$version"

                 // Create the request body as a Map
                 val request = HttpEntity(dbAllTemplate, headers)
                 val response: ResponseEntity<DbSaveTemplate> = restTemplate.postForEntity<DbSaveTemplate>(
                     nationalTemplateUrl, request, DbSaveTemplate::class.java
                 )
                 if (response.statusCodeValue == 201) {
                     println(" $response")
                 } else {
                     println(" fail")
                 }
             }

         }


    }

    //Check date format
    fun isValidDate(date: String): Boolean {
        val dateRegex = """\d{4}-\d{2}-\d{2}"""
        return date.matches(Regex(dateRegex))
    }

    //Convert date to string
    fun convertDateToString(date: Date?): String {

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return if (date != null){
            val dateStr = inputFormat.parse(date.toString())
            outputFormat.format(dateStr)
        }else{
            ""
        }


    }

    fun isEmailValid(emailAddress: String):Boolean{

        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat = Pattern.compile(emailRegex)
        return pat.matcher(emailAddress).matches()
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

    fun getOtp():String{

        // Using numeric values
        val rnd = Random()
        val number = rnd.nextInt(999999)

        return String.format("%06d", number);

    }

    fun getRemainingTime(targetDateStr: String): Triple<Long, Long, Long>{
        val targetDate = LocalDateTime.parse(targetDateStr,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val now = LocalDateTime.now()
        val duration = Duration.between(now, targetDate)

        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        return Triple(days, hours, minutes)
    }
}