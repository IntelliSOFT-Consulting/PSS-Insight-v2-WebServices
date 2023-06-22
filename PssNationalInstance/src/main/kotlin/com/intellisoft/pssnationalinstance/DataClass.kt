package com.intellisoft.pssnationalinstance

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

data class Results(val code: Int, val details: Any?)
data class DbResultsApi(
    @JsonProperty("code")
    val code: Int,
    @JsonProperty("details")
    val details: Any?
)data class NotificationSubscription(
        @JsonProperty("id")
        val id: Long?,
        @JsonProperty("firstName")
        val firstName: String?,
        @JsonProperty("lastName")
        val lastName: String?,
        @JsonProperty("email")
        val email: String,
        @JsonProperty("phone")
        val phone: String?,
        @JsonProperty("isActive")
        val isActive: Boolean = true,
        @JsonProperty("userId")
        val userId: String?
)


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
    @JsonProperty("referenceSheet")
    var referenceSheet: Any?,
    @JsonProperty("aboutUs")
    var aboutUs: Any?,
    @JsonProperty("contactUs")
    var contactUs: Any?,
    @JsonProperty("indicatorDetails")
    var indicatorDetails: List<DbIndicatorDetails>?,


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
data class DbMobileData(
    val publishedIndicators: Any?,
    val nationalInformation:Any?
)

data class DbPublishedVersionApp(
    var count: Any?,
    var details: List<DbIndicatorsApp>,
)
data class DbIndicatorsApp(
    var categoryName: Any?,
    var indicators: List<DbIndicatorValuesApp?>,
)
data class DbIndicatorValuesApp(
    var categoryName: Any?,
    var description: Any?,
    var categoryId: Any?,
    var categoryCode: Any?,
    var indicatorName: Any?,
    var indicatorDataValue: List<DbIndicatorDataValues>,
)
data class DbPublishedVersionDetails(
        var aboutUs: String?,
        var contactUs: String?,
        var referenceSheet: String?,
        var count: Any?,
        var details: List<DbIndicators>,
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
    @JsonProperty("description")
    var description: Any?,
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
    COMPLETED,
    REJECTED,
    RESENT,
    REVISED
}
enum class SurveySubmissionStatus {
    DRAFT, //Respondent has not sent responses
    PENDING, // Respondent has sent responses
    VERIFIED, // Admin has confirmed
    CANCELLED, // Admin has cancelled respondents survey
    EXPIRED, // Respondent's survey has expired
    REJECTED
}
enum class SurveyStatus {
    DRAFT, //Survey has just been created
    SAVED, //
    SENT, // Survey has been sent
    COMPLETED
}
enum class SurveyRespondentStatus {
    VERIFIED,
    PENDING,
    RESEND_REQUEST,
    REJECTED
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
data class DbAboutUs(
    val aboutUs: String?,
    val contactUs:String?,
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

data class DbSubmissionsResponse(
        val id: Long,
        val selectedPeriod: String?,
        val status: String?,
        val dataEntryPersonId: String?,
        val dataEntryDate: String?,
        val createdAt: Any?,
        var dataEntryPerson: MutableList<DataEntryPerson>,
)
data class DataEntryPerson(
        var username: String?,
        var id: String?,
        var surname: String?,
        var firstName: String?,
        var email: String?
)
data class DbSurvey(
    val surveyName: String?,
    val surveyDescription: String?,
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

    var expiryDate: String?,
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
        val referenceSheet:String?,
        val dateFilled: String?
)
data class DbResponse(
    val isSubmit: Boolean,
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
    var resentQuestions:Any? = null,
    var questions: Any? = null,
    var responses: Any? = null,
    var respondentDetails: Any? = null
)
data class DbResendSurvey(
    val comments: String?,
    val indicators : List<String>,
    val expiryDateTime: String
)

data class DbResendDataEntry(
        val details: String?,
        val comments: String?,
        val indicators : List<String>
)
data class DbConfirmSurvey(
    val orgUnit: String,
    val selectedPeriod: String?,
    val dataEntryPersonId: String,
    val indicators : List<String>,
)
data class DbDataEntryData(
        val surveyId:String? = null,
        val orgUnit: String,
        val selectedPeriod: String?,
        val isPublished: Boolean,
        val dataEntryPersonId: String,
        val dataEntryDate: String?,
        val responses: List<DbDataEntryResponses>,
        val dataEntryPerson: DataEntryPerson?
)

data class DbDataEntryResponses(
    val indicator: String,
    val response: String?,
    val comment: String?,
    val attachment: String?
)
data class DbRequestLink(
    @JsonProperty("comment")
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
data class DbEvents(
    @JsonProperty("httpStatusCode")
    val httpStatusCode:Int
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
    val isLatest:Boolean,
    var indicators: Any?,

)
data class DbSurveyData(
    val id:Long?,
    val name: String?,
    val description: String?,
    val landingPage: String?,
    val status: String?,
    val createdBy: Any?,
    val createdAt: Any?,
    var indicators: Any?,
    var respondents: Any?
)
data class DbSurveyRespondentDataDerails(
    val id:Long,
    val emailAddress: String,
    val expiryDateTime: String,
    val surveyId: String,
    val customUrl: String,
    val status: String?

)
data class DbNotificationSub(
    val id:String?,
    val firstName: String?,
    val lastName: String?,
    val email:String,
    val phoneNumber:String?
)
data class DbEmailConfiguration(
    val serverType: String,
    val serverName: String,
    val ports: String,
    val username:String,
    val from:String,
    val password:String
)

data class DbIndicatorDetails(
        @JsonProperty("Description")
        var Description: String?,
        @JsonProperty("Indicator_Code")
        var Indicator_Code: String?,

        @JsonProperty("uuid")
        var uuid: Any?,
        @JsonProperty("date")
        var date: Any?,
        @JsonProperty("indicatorName")
        var indicatorName: Any?,
        @JsonProperty("indicatorCode")
        var indicatorCode: Any?,
        @JsonProperty("dataType")
        var dataType: Any?,
        @JsonProperty("topic")
        var topic: Any?,
        @JsonProperty("definition")
        var definition: Any?,
        @JsonProperty("systemComponent") // new field
        var systemComponent: Any?,
        @JsonProperty("systemElement") //new field
        var systemElement: Any?,
        @JsonProperty("assessmentQuestions")
        var assessmentQuestions: List<DbAssessmentQuestion>?,
        @JsonProperty("formula")
        var formula: DbFormula?, // Added new formula object
        @JsonProperty("purposeAndIssues")
        var purposeAndIssues: Any?,
        @JsonProperty("preferredDataSources")
        var preferredDataSources: Any?,
        @JsonProperty("methodOfEstimation")
        var methodOfEstimation: Any?,
        @JsonProperty("proposedScoring")
        var proposedScoring: Any?,
        @JsonProperty("expectedFrequencyDataDissemination")
        var expectedFrequencyDataDissemination: Any?,
        @JsonProperty("indicatorReference")
        var indicatorReference: Any?,
        @JsonProperty("indicatorSource")
        var indicatorSource: Any?,
        @JsonProperty("createdBy")
        var createdBy: DbCreatedBy?,
)

data class DbAssessmentQuestion(
        @JsonProperty("valueType")
        val valueType: Any?,
        @JsonProperty("name")
        val name: Any?
)

data class DbFormula(
        @JsonProperty("numerator")
        var numerator: String?,
        @JsonProperty("denominator")
        var denominator: String?,
        @JsonProperty("format")
        var format: String?
)
data class DbCreatedBy(
        @JsonProperty("id")
        val id:Any?,
        @JsonProperty("code")
        val code:Any?,
        @JsonProperty("name")
        val name:Any?,
        @JsonProperty("username")
        val username:Any?,
        @JsonProperty("displayName")
        val displayName:Any?,
)

data class DbApplicationValues(
        val internationalUrl :String?,
        val username :String?,
        val password :String?,
        val program :String?,
        val masterTemplate :String?,
)

enum class IndicatorDropDowns {
    SELECTION,
    TEXT,
    NUMBER,
}

data class DbIndicatorTypes(
        val topics:Any?,
        val valueType: Any?
)

data class DbDataImport(
    val program: String,
    val orgUnit: String,
    val eventDate: String,
    val status: String,
    val storedBy: String,
    val dataValues: List<DataValue>
)

data class DataValue(
    val dataElement: String,
    val value: String
)