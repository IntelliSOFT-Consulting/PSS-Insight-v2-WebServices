package com.intellisoft.internationalinstance


import com.intellisoft.internationalinstance.db.VersionEntity
import com.intellisoft.internationalinstance.service_impl.impl.InternationalServiceImpl
import com.intellisoft.internationalinstance.util.GenericWebclient
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.*
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.http.ResponseEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class FormatterClass {

    fun getUUid():String{
        return RandomStringUtils.randomAlphanumeric(10)
    }

    fun startBackGroundTask(
        url: String,
        versionDescription: String,
        savedVersionEntity: VersionEntity,
        internationalService: InternationalServiceImpl,
        indicatorList:List<String>
        ){
        GlobalScope.launch {
            doBackGroundTask(
                url,
                versionDescription,
                savedVersionEntity,
                internationalService,
                indicatorList)
        }
    }


    suspend fun doBackGroundTask(
        url: String,
        versionDescription: String,
        savedVersionEntity: VersionEntity,
        internationalService: InternationalServiceImpl,
        indicatorList:List<String>
    ) {
        withContext(Dispatchers.IO) {
            // Run some background task here
            val dbIndicatorsValueListNew = ArrayList<DbIndicatorsValue>()
            val dbIndicatorsValueList: List<DbIndicatorsValue> = internationalService.indicatorsValues

            for (dbIndicatorsValue in dbIndicatorsValueList){

                val dbIndicatorDataValuesList = ArrayList<DbIndicatorDataValues?>()
                val categoryName = dbIndicatorsValue.categoryName as String?

                val indicatorDataValuesList = dbIndicatorsValue.indicators
                for (dbIndicatorDataValues in indicatorDataValuesList){
                    val categoryId = dbIndicatorDataValues?.categoryId as String?
                    if (indicatorList.contains(categoryId)) {
                        dbIndicatorDataValuesList.add(dbIndicatorDataValues)
                    }
                }

                if (!dbIndicatorDataValuesList.isEmpty()) {
                    val dbIndicatorsValueNew = DbIndicatorsValue(
                        categoryName,
                        dbIndicatorDataValuesList
                    )
                    dbIndicatorsValueListNew.add(dbIndicatorsValueNew)
                }
            }
            val dbResults = DbResults(
                dbIndicatorsValueListNew.size,
                dbIndicatorsValueListNew
            )

            val dbMetadataJsonData = GenericWebclient
                .getForSingleObjResponse<DbMetadataJsonData, Exception>(
                    url, DbMetadataJsonData::class.java
                )
            dbMetadataJsonData.publishedVersion = dbResults
            val versionNo = (internationalService.internationalVersions + 1).toString()

            val dbMetadataJson = DbMetadataValue(
                versionNo,
                versionDescription,
                dbMetadataJsonData
            )
            internationalService.pushMetadata(dbMetadataJson, savedVersionEntity)

        }
    }

    fun generatePdfFile(dbPdfData: DbPdfData):File {
        val pdfFile = File("file.pdf")
        val document = Document(PageSize.A4, 50f, 50f, 50f, 50f)
        PdfWriter.getInstance(document, FileOutputStream(pdfFile))
        document.open()

        // Add title
        val titleFont = Font(Font.FontFamily.HELVETICA, 36f, Font.BOLD, BaseColor.WHITE)
        val titleTable = PdfPTable(1)
        titleTable.widthPercentage = 100f
        val titleCell = PdfPCell(Phrase(dbPdfData.title, titleFont))
        titleCell.backgroundColor = BaseColor.RED
        titleCell.horizontalAlignment = Element.ALIGN_CENTER
        titleCell.verticalAlignment = Element.ALIGN_MIDDLE
        titleCell.fixedHeight = 50f
        titleTable.addCell(titleCell)
        document.add(titleTable)

        // Add version and description
        val versionFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.BLACK)
        val paragraph = Paragraph()
        paragraph.add(Chunk("\n"))
        paragraph.add(Chunk("Version: ", versionFont))
        paragraph.add(Chunk(dbPdfData.version, versionFont))
        paragraph.add(Chunk("\n\n"))
        paragraph.add(Chunk("Description: ", versionFont))
        paragraph.add(Chunk(dbPdfData.versionDescription, versionFont))
        paragraph.add(Chunk("\n"))
        document.add(paragraph)

        // Add subtitles and values
        val subTitleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor.BLACK)
        val valueFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.BLACK)
        val subTitleList = dbPdfData.subTitleList
        for (subTitle in subTitleList){

            // Add subtitle
            val subTitleTable = PdfPTable(1)
            subTitleTable.widthPercentage = 100f
            subTitleTable.spacingBefore = 10f
            subTitleTable.addCell(PdfPCell(Phrase(subTitle.subTitle, subTitleFont)))
            document.add(subTitleTable)

            // Add values
            val valueTable = PdfPTable(2)
            valueTable.widthPercentage = 100f
            valueTable.spacingBefore = 10f

            val valueList = subTitle.valueList
            for (valueKey in valueList){
                valueTable.addCell(createCell(valueKey.key, BaseColor.BLUE, valueFont))
                valueTable.addCell(createCell(valueKey.value, BaseColor.WHITE, valueFont))
            }

            document.add(valueTable)
        }

        document.close()
        return pdfFile
    }
    private fun createCell(text: String, backgroundColor: BaseColor, font: Font): PdfPCell? {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = backgroundColor
        cell.border = Rectangle.BOX
        cell.borderWidth = 1f
        cell.setPadding(5f)
        return cell
    }


    fun extractName(emailAddress: String): String{
        return emailAddress.substringBefore("@")
    }
    fun getNextVersion(list:List<Any>):Int{

        val intList = list.map { it.toString().toIntOrNull() }
        val filteredList = intList.filterIsInstance<Int>()
        val largestValue = filteredList.maxOrNull()

        return largestValue ?: 1
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
    fun getTodayDate():String{
        val currentDate = Date()
        return currentDate.toString()
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