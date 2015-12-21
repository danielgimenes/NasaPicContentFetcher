package br.com.dgimenes.nasapiccontentfetcher

import br.com.dgimenes.nasapicserver.model.SpacePic
import br.com.dgimenes.nasapicserver.model.SpacePicSource
import br.com.dgimenes.nasapicserver.model.SpacePicStatus
import java.util.*
import javax.persistence.Persistence

fun main(args: Array<String>) {
    DownloadLatestPics().start()
}

class DownloadLatestPics {
    fun start() {
        val spacePic = SpacePic(
                originalApiUrl = "https://api.nasa.gov/planetary/apod?concept_tags=True&api_key=DEMO_KEY",
                originalApiImageUrl = "http://apod.nasa.gov/apod/image/1512/Refsdal_Hubble_1080.jpg",
                title = "SN Refsdal: The First Predicted Supernova Image",
                createdAt = Calendar.getInstance().time,
                status = SpacePicStatus.CREATED,
                source = SpacePicSource.NASA_APOD
        )
        persistNewSpacePic(spacePic)
    }

    private fun persistNewSpacePic(spacePic: SpacePic) {
       val em = Persistence.createEntityManagerFactory("primary_pu_dev").createEntityManager()
        em.transaction.begin()
        em.persist(spacePic)
        em.transaction.commit()
    }
}
