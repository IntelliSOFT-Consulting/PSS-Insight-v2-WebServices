package com.intellisoft.internationalinstance

import com.intellisoft.internationalinstance.db.VersionEntity
import com.intellisoft.internationalinstance.db.repso.VersionRepos
import com.intellisoft.internationalinstance.service_impl.impl.InternationalServiceImpl
import com.intellisoft.internationalinstance.util.GenericWebclient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class BackgroundAsyncTask(private val versionRepos: VersionRepos) {

    fun startBackGroundTask(url: String, versionDescription: String, savedVersionEntity: VersionEntity, internationalService: InternationalServiceImpl, indicatorList: List<String>) {
        GlobalScope.launch {
            doBackGroundTask(url, versionDescription, savedVersionEntity, internationalService, indicatorList)
        }
    }


    suspend fun doBackGroundTask(url: String, versionDescription: String, savedVersionEntity: VersionEntity, internationalService: InternationalServiceImpl, indicatorList: List<String>) {
        withContext(Dispatchers.IO) {
            // Run some background task here
            val dbIndicatorsValueListNew = ArrayList<DbIndicatorsValue>()
            val dbIndicatorsValueList: List<DbIndicatorsValue> = internationalService.indicatorsValues

            for (dbIndicatorsValue in dbIndicatorsValueList) {

                val dbIndicatorDataValuesList = ArrayList<DbIndicatorDataValues?>()
                val categoryName = dbIndicatorsValue.categoryName as String?

                val indicatorDataValuesList = dbIndicatorsValue.indicators
                for (dbIndicatorDataValues in indicatorDataValuesList) {
                    val categoryId = dbIndicatorDataValues?.categoryId as String?
                    if (indicatorList.contains(categoryId)) {
                        dbIndicatorDataValuesList.add(dbIndicatorDataValues)
                    }
                }

                if (!dbIndicatorDataValuesList.isEmpty()) {
                    val dbIndicatorsValueNew = DbIndicatorsValue(categoryName, dbIndicatorDataValuesList)
                    dbIndicatorsValueListNew.add(dbIndicatorsValueNew)
                }
            }
            val dbResults = DbResults(dbIndicatorsValueListNew.size, dbIndicatorsValueListNew)

            val dbMetadataJsonData = GenericWebclient.getForSingleObjResponse<DbMetadataJsonData, Exception>(url, DbMetadataJsonData::class.java)
            dbMetadataJsonData.publishedVersion = dbResults
            val versionNo = (internationalService.internationalVersions + 1).toString()

            //append versionNo to the savedVersionEntity object
            savedVersionEntity.versionName = versionNo

            //persist to DB:
            val updatedEntity = versionRepos.save(savedVersionEntity)

            //send email notification
            internationalService.sendNotification(updatedEntity)

            val dbMetadataJson = DbMetadataValue(versionNo, versionDescription, dbMetadataJsonData)
            internationalService.pushMetadata(dbMetadataJson, savedVersionEntity)


        }
    }
}