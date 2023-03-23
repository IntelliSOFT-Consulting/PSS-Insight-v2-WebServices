package com.intellisoft.pssnationalinstance

import com.fasterxml.jackson.annotation.JsonProperty

data class Results(val code: Int, val details: Any?)

data class DbResults(
    val count: Int,
    val details: Any?
)

data class DbDetails(val details: Any?)


data class DbMetadataJson(
    @JsonProperty("version")
    val version: String?,
    @JsonProperty("versionDescription")
    val versionDescription: String?,
    @JsonProperty("metadata")
    var metadata: DbPrograms?
)
data class DbPrograms(
    @JsonProperty("date")
    val date: Any?,
    @JsonProperty("dataElements")
    var dataElements: Any?,
    @JsonProperty("categoryOptionCombos")
    val categoryOptionCombos: Any?,
    @JsonProperty("categoryOptions")
    val categoryOptions: Any?,
    @JsonProperty("categoryCombos")
    val categoryCombos: Any?,
    @JsonProperty("programStageDataElements")
    val programStageDataElements: Any?,
    @JsonProperty("programStages")
    val programStages: Any?,
    @JsonProperty("categories")
    val categories: Any?,
    @JsonProperty("programs")
    val programs: Any?,
    @JsonProperty("groups")
    val groups: DbGroups?,
    @JsonProperty("indicatorDescriptions")
    val indicatorDescriptions: List<DbIndicatorDescription>,
    @JsonProperty("publishedVersion")
    var publishedVersion: DbPublishedVersion?,
)
data class DbGroups(
    @JsonProperty("pager")
    val pager:Any?,
    @JsonProperty("dataElementGroups")
    val dataElementGroups:List<DbDataElementGroups>
)
data class DbDataElementGroups(
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("id")
    val id:String?,
    @JsonProperty("dataElements")
    val dataElements:List<DbDataElements>
)
data class DbDataElements(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("id")
    val id: String?
)
data class DbPublishedVersion(
    @JsonProperty("count")
    var count: Any?,
    @JsonProperty("details")
    var details: List<DbIndicators>,
)
data class DbIndicators(
    @JsonProperty("categoryName")
    var categoryName: Any?,
    @JsonProperty("indicators")
    var indicators: List<DbIndicatorValues>,
)
data class DbIndicatorValues(
    @JsonProperty("categoryId")
    var categoryId: Any?,
    @JsonProperty("categoryName")
    var categoryName: Any?,
    @JsonProperty("indicatorName")
    var indicatorName: Any?,
    @JsonProperty("indicatorDataValue")
    var indicatorDataValue: List<DbIndicatorDataValues>,
)
data class DbIndicatorDataValues(
    @JsonProperty("id")
    var id: Any?,
    @JsonProperty("code")
    var code: Any?,
    @JsonProperty("name")
    var name: Any?,
    @JsonProperty("valueType")
    var valueType: Any?,
)
data class DbIndicatorDescription(
    @JsonProperty("Description")
    val Description: String?,
    @JsonProperty("Indicator_Code")
    val Indicator_Code: String?
)
data class DbIndicatorEdit(
    val categoryId: String,
    val indicatorId:String,
    val editValue:String,
    val creatorId:String
)
data class DbVersions(
    val versionDescription: String,
    val isPublished: Boolean,
    val createdBy: String,
    val publishedBy: String?,
    val indicators: List<DbVersionDate>
)
data class DbVersionDate(
    val isInternational:Boolean?,
    val id:String
)
enum class PublishStatus {
    DRAFT,
    PUBLISHED,
    COMPLETED
}
data class DbPublishVersionResponse(
    @JsonProperty("httpStatus")
    val httpStatus:String,
    @JsonProperty("httpStatusCode")
    val httpStatusCode:Int,
    @JsonProperty("status")
    val status:String,
    @JsonProperty("message")
    val message:String,
)
data class DbVersionDetails(
    val versionName: String?,
    val versionDescription: String,
    val createdBy: String,
    val status: String,
    val publishedBy: String?,
    val createdAt: String,
    val indicators: Any?
)
data class DbResultsData(
    val count: Int,
    val details: Any?
)
data class DbTemplates(
    var nationalTemplate: DbTemplateDetails? = null,
    var interNationalTemplate: DbTemplateDetails? = null
)
data class DbTemplateDetails(
    val version:Int?,
    val indicators: Any?
)
data class DbPeriodConfiguration(
    val period: String,
    val isCompleted:Boolean,
    val closedBy: String
)
data class DbDataEntryData(
    val orgUnit: String,
    val selectedPeriod: String?,
    val isPublished: Boolean,
    val dataEntryPersonId: String,
    val dataEntryDate: String?,
    val responses: List<DbDataEntryResponses>,
)
data class DbDataEntryResponses(
    val indicator: String,
    val response: String?,
    val comment: String?,
    val attachment: String?

)
data class DbDataEntry(
    val program: String,
    val orgUnit: String,
    val eventDate: String,
    val status: String,
    val storedBy: String,
    val dataValues: List<DbDataValues>
)
data class DbDataValues(
    val dataElement: String,
    val value: String
)
data class DbFileResources(
    @JsonProperty("httpStatus")
    val httpStatus:String,
    @JsonProperty("httpStatusCode")
    val httpStatusCode:String,
    @JsonProperty("response")
    val response:DbResponseFileResource?,
)

data class DbResponseFileResource(
    @JsonProperty("fileResource")
    val fileResource:DbResFileRes?
)
data class DbResFileRes(
    @JsonProperty("id")
    val id:String?
)