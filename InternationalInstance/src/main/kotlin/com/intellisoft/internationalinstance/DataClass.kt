package com.intellisoft.internationalinstance

import com.fasterxml.jackson.annotation.JsonProperty
import org.json.JSONArray
import org.json.JSONObject

data class Results(val code: Int, val details: Any?)

data class DbResults(
    val count: Int,
    val details: Any?
)

data class DbDetails(val details: Any?)



data class DbProgramsList(
    @JsonProperty("programs")
    val programs: List<DbPrograms>
)

data class DbPrograms(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("created")
    val created: String,
    @JsonProperty("name")
    val name: String,
)
data class DbTemplate(
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("program")
    val program: String?,
    @JsonProperty("metadata")
    val metadata: Any?
)

data class DbTemplateData(
    val versionNumber: String?,
    val description: String?,
    val program: String?
)
data class DbSaveTemplate(
    @JsonProperty("httpStatusCode")
    val httpStatusCode: Int?,
)
data class DbVersionData(
    val versionDescription: String?,
    val isPublished: Boolean,
    val indicators: List<String>,

    val createdBy: String?,
    val publishedBy: String?,

    var versionId: Long?)
enum class PublishStatus {
    DRAFT,
    PUBLISHED
}
data class DbIndicatorValues(
    val versionName:String,
    val versionDescription:String,
    val versionId: Long,
    val status: String,
    val indicators: Any,


    )

data class DbFrontendIndicators(
    val indicatorId: String,
    val categoryName: String,
    val indicatorName: String,
    val indicators: List<DbIndicators>
)
data class DbIndicators(
    val code: String,
    val name: String,
    val id: String
)
data class DbFrontendCategoryIndicators(
    val categoryName: String,
    val indicators: List<DbFrontendIndicators>
)


data class DbMetadataJson(
    @JsonProperty("version")
    val version: String?,
    @JsonProperty("versionDescription")
    val versionDescription: String?,
    @JsonProperty("metadata")
    val metadata: DbProgramsData
)
data class DbProgramsData(
    @JsonProperty("date")
    val date: Any?,
    @JsonProperty("dataElements")
    var dataElements: Any?,
    @JsonProperty("categoryOptionCombos")
    val categoryOptionCombos: Any?,
    @JsonProperty("categoryOptions")
    val categoryOptions: Any?,
    @JsonProperty("programIndicators")
    val programIndicators: Any?,
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
    var groups: Any?,
    @JsonProperty("indicatorDescriptions")
    var indicatorDescriptions: Any?,
    @JsonProperty("publishedGroups")
    var publishedGroups: Any?,
)
data class DbDataValuesData(
    val code: String,
    val lastUpdated: String,
    val id: String,
    val created: String,
    val name: String,
    val shortName: String,
    val aggregationType: String,
    val domainType: String,
    val valueType: String,
    val formName: String,
    val zeroIsSignificant: Any,
    val categoryCombo: Any,
    val lastUpdatedBy: Any,
    val sharing: Any,
    val createdBy: Any,
    val translations: Any,
    val attributeValues: Any,
    val legendSets: Any,
    val aggregationLevels: Any
)