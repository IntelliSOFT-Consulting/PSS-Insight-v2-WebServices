package com.intellisoft.internationalinstance

import com.fasterxml.jackson.annotation.JsonProperty
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

data class Results(val code: Int, val details: Any?)

data class DbResults(
    val count: Int,
    val details: Any?
)

data class DbDetails(val details: Any?)

data class DbGroupsData(
    @JsonProperty("pager")
    val pager: Any?,
    @JsonProperty("dataElementGroups")
    val dataElementGroups: List<DbGroupings>,
)
data class DbGroupings(
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("dataElements")
    val dataElements: List<DbDataElementsData>,
)
data class DbDataElementsData(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("id")
    val id: String?,
)
data class DbIndicatorsValue(
    var categoryName: Any?,
    var indicators: List<DbIndicatorDataValues?>,
)
data class DbIndicatorDataValues(
    var description: Any?,
    var categoryId: Any?,
    var categoryName: Any?,
    var indicatorName: Any?,
    var indicatorDataValue: List<DbDataGrouping>,
)
data class DbDataGrouping(
    val code: String?,
    val name: String?,
    val id: String?,
    val valueType: String?,
)
data class DbMetadataValue(
    @JsonProperty("version")
    val version: String?,
    @JsonProperty("versionDescription")
    val versionDescription: String?,
    @JsonProperty("metadata")
    val metadata: DbMetadataJsonData
)

data class DbMetadataJsonData(
    @JsonProperty("date")
    val date: Any?,
    @JsonProperty("dataElements")
    var dataElements: List<DbDataElements>,
    @JsonProperty("categoryOptionCombos")
    val categoryOptionCombos: Any?,
    @JsonProperty("categoryOptions")
    val categoryOptions: Any?,
    @JsonProperty("categoryCombos")
    val categoryCombos: Any?,
    @JsonProperty("programIndicators")
    val programIndicators: Any?,
    @JsonProperty("programRuleVariables")
    val programRuleVariables: Any?,
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
    @JsonProperty("publishedVersion")
    var publishedVersion: Any?
)

data class DbPublishedVersion(
    @JsonProperty("count")
    var count: Any?,
    @JsonProperty("details")
    var details: List<DbIndicatorsData>,
)
data class DbIndicatorsData(
    @JsonProperty("categoryName")
    var categoryName: Any?,
    @JsonProperty("indicators")
    var indicators: List<DbIndicatorValuesData>,
)
data class DbIndicatorValuesData(
    @JsonProperty("categoryId")
    var categoryId: Any?,
    @JsonProperty("categoryName")
    var categoryName: Any?,
    @JsonProperty("indicatorName")
    var indicatorName: Any?,
    @JsonProperty("indicatorDataValue")
    var indicatorDataValue: List<DbIndicatorDataValuesData>,
)
data class DbIndicatorDataValuesData(
    @JsonProperty("id")
    var id: Any?,
    @JsonProperty("code")
    var code: Any?,
    @JsonProperty("name")
    var name: Any?,
    @JsonProperty("valueType")
    var valueType: Any?,
)
data class DbDataElements(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("valueType")
    val valueType: String?,
    @JsonProperty("id")
    val id: Any?,
    @JsonProperty("lastUpdated")
    val lastUpdated: Any?,
    @JsonProperty("created")
    val created: Any?,
    @JsonProperty("name")
    val name: Any?,
    @JsonProperty("shortName")
    val shortName: Any?,
    @JsonProperty("aggregationType")
    val aggregationType: Any?,
    @JsonProperty("domainType")
    val domainType: Any?,
    @JsonProperty("formName")
    val formName: Any?,
    @JsonProperty("zeroIsSignificant")
    val zeroIsSignificant: Any?,
    @JsonProperty("categoryCombo")
    val categoryCombo: Any?,
    @JsonProperty("lastUpdatedBy")
    val lastUpdatedBy: Any?,
    @JsonProperty("sharing")
    val sharing: Any?,
    @JsonProperty("createdBy")
    val createdBy: Any?,
    @JsonProperty("translations")
    val translations: Any?,
    @JsonProperty("attributeValues")
    val attributeValues: Any?,
    @JsonProperty("legendSets")
    val legendSets: Any?,
    @JsonProperty("aggregationLevels")
    val aggregationLevels: Any?,

)


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
data class DbIndicatorDescription(
    @JsonProperty("Description")
    val Description: String?,
    @JsonProperty("Indicator_Code")
    val Indicator_Code: String?
)
data class DbIndicatorDescriptionData(
    val Description: String?,
    val Indicator_Code: String?
)
data class DbVersionDetails(
    val id: Long,
    val versionName: String?,
    val versionDescription: String?,
    val status: String,
    val createdBy: String?,
    val publishedBy: String?,
    val createdAt: Date,
    var indicators: Any?

    )

data class DbNotificationData(
    val emailAddress:List<String>,
    val createdAt:String,
    val title: String,
    val description:String
)
data class DbPdfData(
    val title:String,
    val version: String?,
    val versionDescription: String?,
    val subTitleList:List<DbPdfSubTitle>
)
data class DbPdfSubTitle(
    val subTitle:String,
    val valueList:List<DbPdfValue>
)
data class DbPdfValue(
    val key:String,
    val value:String
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
data class DbNotificationSub(
    val firstName: String,
    val lastName: String?,
    val email:String,
    val phoneNumber:String?
)
data class DbSendNotification(
    val sendAll:Boolean?,
    val emailList:List<String>,
    val title: String,
    val message:String,
    val sender: String
)
data class DbNotification(
    val title: String,
    val message:String,
    val sender: String,
    val createdAt:String
)