package br.com.dgimenes.nasapiccontentfetcher

import br.com.dgimenes.nasapicserver.model.SpacePic
import br.com.dgimenes.nasapicserver.model.SpacePicSource
import br.com.dgimenes.nasapicserver.model.SpacePicStatus
import java.util.*
import javax.persistence.Persistence

fun main(args: Array<String>) {
    DownloadLatestPics().start()
    System.exit(0)
}

class DownloadLatestPics {
    fun start() {
        val unpublishedSpacePic = SpacePic(
                originalApiUrl = "https://api.nasa.gov/planetary/apod?concept_tags=True&api_key=DEMO_KEY",
                originalApiImageUrl = "http://apod.nasa.gov/apod/image/1512/Refsdal_Hubble_1080.jpg",
                title = "SN Refsdal: The First Predicted Supernova Image",
                createdAt = Calendar.getInstance().time,
                status = SpacePicStatus.CREATED,
                source = SpacePicSource.NASA_APOD
        )

        val spacePic = SpacePic(
                originalApiUrl = "https://api.nasa.gov/planetary/apod?concept_tags=True&api_key=DEMO_KEY",
                originalApiImageUrl = "http://apod.nasa.gov/apod/image/1512/Refsdal_Hubble_1080.jpg",
                title = "SN Refsdal: The First Predicted Supernova Image",
                createdAt = Calendar.getInstance().time,
                status = SpacePicStatus.PUBLISHED,
                source = SpacePicSource.NASA_APOD,
                description = "It's back.  Never before has an observed supernova been predicted. " +
                        "The unique astronomical event occurred in the field of galaxy cluster MACS J1149.5+2223. " +
                        "Most bright spots in the featured image are galaxies in this cluster.  The actual " +
                        "supernova, dubbed Supernova Refsdal, occurred just once far across the universe and well " +
                        "behind this massive galaxy cluster.  Gravity caused the cluster to act as a massive " +
                        "gravitational lens, splitting the image of Supernova Refsdal into multiple bright images. " +
                        "One of these images arrived at Earth about ten years ago, likely in the upper red circle, " +
                        "and was missed.  Four more bright images peaked in April in the lowest red circle, spread " +
                        "around a massive galaxy in the cluster as the first Einstein Cross supernova. But there " +
                        "was more.  Analyses revealed that a sixth bright supernova image was likely still on its " +
                        "way to Earth and likely to arrive within the next year.  Earlier this month -- right on " +
                        "schedule -- this sixth bright image was recovered, in the middle red circle, as predicted. " +
                        " Studying image sequences like this help humanity to understand how matter is distributed " +
                        "in galaxies and clusters, how fast the universe expands, and how massive stars explode.  " +
                        " Follow APOD on: Facebook,  Google Plus, or Twitter",
                hdImageUrl = "",
                previewImageUrl = "",
                publishedAt = Calendar.getInstance().time,
                deletedAt = null,
                updatedAt = Calendar.getInstance().time
        )
        persistNewSpacePic(unpublishedSpacePic)
        persistNewSpacePic(spacePic)
    }

    private fun persistNewSpacePic(spacePic: SpacePic) {
       val em = Persistence.createEntityManagerFactory("primary_pu_dev").createEntityManager()
        em.transaction.begin()
        em.persist(spacePic)
        em.transaction.commit()
        em.close()
    }
}
