package com.intellisoft.pssnationalinstance

import com.fasterxml.jackson.annotation.JsonProperty

data class Results(val code: Int, val details: Any?)

data class DbResults(
    val count: Int,
    val details: Any?
)

data class DbDetails(val details: Any?)

data class DbMetadataJsonNational(
    @JsonProperty("metadata")
    var metadata: DbProgramsDataDetails?
)
data class DbProgramsDataDetails(
    @JsonProperty("indicatorDescriptions")
    val indicatorDescriptions: List<DbIndicatorDescriptionData>
)


data class DbMetadataJson(
    @JsonProperty("version")
    var version: String?,
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
    val indicatorDescriptions: Any?,
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
    var indicators: List<DbIndicatorValues?>,
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
data class DbIndicatorDescriptionInt(
    val Description: String?,
    val Indicator_Code: String?
)
data class DbIndicatorDescriptionData(
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("indicator_Code")
    val indicator_Code: String?
)

data class DbIndicatorDataResponses(
    var id: Any?,
    var code: Any?,
    var name: Any?,
    var valueType: Any?,
    var response: DbDataEntryResponses?
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
    val isLatest:Boolean,
    val id:String
)
enum class DhisStatus {
    ACTIVE,
    COMPLETED,
    OVERDUE
}
enum class PublishStatus {
    DRAFT,
    PUBLISHED,
    COMPLETED
}
enum class SurveySubmissionStatus {
    DRAFT, //Respondent has not sent responses
    PENDING, // Respondent has sent responses
    VERIFIED, // Admin has confirmed
    CANCELLED, // Admin has cancelled respondents survey
    EXPIRED, // Respondent's survey has expired
}
enum class SurveyStatus {
    SAVED,
    SENT,
    COMPLETED
}
enum class SurveyRespondentStatus {
    VERIFIED,
    PENDING,
    RESEND_REQUEST
}
enum class MailStatus {
    SEND,
    RESEND,
    REMIND,
    EXPIRED
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
    val id: Long?,
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
data class DbDataEntryResponse(
    val id: Long?,
    val selectedPeriod: String?,
    val status: String?,
    val dataEntryPersonId: String?,
    val dataEntryDate: String?,
    val createdAt: Any?,
    val responses: Any?,
    var indicators: Any? = null,
)
data class DbSurvey(
    val surveyName: String,
    val surveyDescription: String,
    val surveyLandingPage:String?,
    val isSaved: Boolean,
    val creatorId: String,
    val indicators : List<String>
)
data class DbSurveyRespondent(
    val emailAddressList: List<String>,
    val expiryDateTime: String,
    val surveyId: String,
    val customAppUrl: String
)

data class DbSurveyDetails(
    val surveyId:String,
    val surveyName: String,
    val surveyStatus: String,
    val surveyDescription: String?,
    val landingPage: String?,
    val respondentList:List<DbRespondent>
)
data class DbRespondent(
    val respondentId:String,
    val emailAddress: String,
    val createdAt: String,

    var dateExpired: String?,
    var newLinkRequested: Boolean?
)
data class DbVerifySurvey(
    val respondentId: String,
    val password: String
)
data class DbRespondentsDetails(
    val id: Long,
    val emailAddress: String,
    val expiresAt:String,
    val status:String?,
    val surveyName:String?,
    val surveyDescription: String?,
    val landingPage: String?,
    val referenceSheet:String?
)
data class DbResponse(
    val isSubmit: Boolean?,
    val respondentId: String,
    val responses: List<DbRespondentSurvey>
)
data class DbRespondentSurvey(
    val indicatorId: String,
    val answer: String,
    val comments: String?,
    val attachment: String?
)
data class DbResponseDetails(
    var questions: Any? = null,
    var responses: Any? = null,
    var respondentDetails: Any? = null
)
data class DbResendSurvey(
    val indicators : List<String>,
    val expiryDateTime: String
)
data class DbConfirmSurvey(
    val orgUnit: String,
    val selectedPeriod: String?,
    val dataEntryPersonId: String,
    val indicators : List<String>,
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
data class DbRequestLink(
    val comment: String
)
data class DbRespondents(
    val respondents: List<DbSurveyRespondentData>
)
data class DbSurveyRespondentData(
    val emailAddress:String,
    val expiryDate:String,
    val customUrl: String,
    val password:String
)
data class DbProgramsData(
    @JsonProperty("pager")
    val pager: Any?,
    @JsonProperty("programs")
    val programs: List<DbProgramsValue>
)
data class DbProgramsValue(
    @JsonProperty("id")
    val id:Any?,
    @JsonProperty("displayName")
    val displayName:Any?,

)
data class DbOrganisationUnit(
    @JsonProperty("pager")
    val pager: Any?,
    @JsonProperty("organisationUnits")
    val organisationUnits: List<DbProgramsValue>
)
data class DbDocuments(
    val name:String,
    val type:String,
    val attachment: Boolean,
    val external:Boolean,
    val url:String
)
data class DbDocumentFile(
    @JsonProperty("httpStatusCode")
    val httpStatusCode:Any?,
    @JsonProperty("response")
    val response:DbFileResponse?
)
data class DbFileResponse(
    @JsonProperty("uid")
    val uid:String?
)
data class DbVersionDataDetails(
    val id:Long?,
    val versionName: String?,
    val versionDescription: String?,
    val status: String?,
    val createdBy: Any?,
    val publishedBy: String?,
    var indicators: Any?
)