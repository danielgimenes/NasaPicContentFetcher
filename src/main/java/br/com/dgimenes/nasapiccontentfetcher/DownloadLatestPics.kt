package br.com.dgimenes.nasapiccontentfetcher

import br.com.dgimenes.nasapiccontentfetcher.service.api.NasaAPODWebservice
import br.com.dgimenes.nasapiccontentfetcher.service.api.RetrofitFactory
import br.com.dgimenes.nasapicserver.model.SpacePic
import br.com.dgimenes.nasapicserver.model.SpacePicSource
import br.com.dgimenes.nasapicserver.model.SpacePicStatus
import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.cloudinary.utils.ObjectUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.persistence.Persistence

fun main(args: Array<String>) {
    try {
        val program = DownloadLatestPics()
        if (args.size > 0 && args[0] == "test-data")
            program.insertTestData()
        else if (args.size > 1 && args[0] == "check-interval")
            program.start(args[1].toInt())
        else {
            program.start()
        }
        program.close()
    } catch(e: Exception) {
        e.printStackTrace()
    }
}

class DownloadLatestPics {

    var em = Persistence.createEntityManagerFactory("primary_pu_dev").createEntityManager()
    val DATE_FORMAT = "yyyy-MM-dd"
    val APOD_BASE_URL = "https://api.nasa.gov"
    val CONFIG_FILE_NAME = "nasapiccontentfetcher.config"
    var APOD_API_KEY : String? = null
    var CLOUDINARY_CLOUD_NAME : String? = null
    var CLOUDINARY_API_KEY : String? = null
    var CLOUDINARY_API_SECRET : String? = null
    var cloudinary : Cloudinary? = null

    fun start(checkInterval : Int? = null) {
        loadConfigurations()
        println("Fetching pictures metadata...")
        val spacePics =
                if (checkInterval != null) downloadLatestAPODsMetadata(checkInterval) else downloadLatestAPODsMetadata()
        println("SpacePics to persist = ${spacePics.size}")
        spacePics.forEach { persistNewSpacePic(it) }
        println("Persisted!")

        println("Checking SpacePics not published yet...")
        val spacePicsToPublish = getSpacePicsToPublish()
        println("Pics to publish = ${spacePicsToPublish.size}")

        if (spacePicsToPublish.size > 0) {
            setupCloudinary()
            println("Preparing and publishing SpacePics...")
            spacePicsToPublish.map { prepareSpacePicForPublishing(it) }
                    .filterNotNull()
                    .forEach { persistNewSpacePic(it) }
        }
        println("All done! Bye")
    }

    private fun loadConfigurations() {
        val inputStream = this.javaClass.classLoader.getResourceAsStream(CONFIG_FILE_NAME)
        inputStream ?: throw RuntimeException("Configurations file $CONFIG_FILE_NAME not found!")
        val properties = Properties()
        properties.load(inputStream)
        APOD_API_KEY = properties.get("apod-api-key") as String?
        CLOUDINARY_CLOUD_NAME = properties.get("cloudinary-cloud-name") as String?
        CLOUDINARY_API_KEY = properties.get("cloudinary-api-key") as String?
        CLOUDINARY_API_SECRET = properties.get("cloudinary-api-secret") as String?
        if (APOD_API_KEY == null || CLOUDINARY_CLOUD_NAME == null || CLOUDINARY_API_KEY == null
                || CLOUDINARY_API_SECRET == null) {
            throw RuntimeException("Invalid configurations!")
        }
    }

    private fun setupCloudinary() {
        val config = HashMap<String, String>()
        config.put("cloud_name", CLOUDINARY_CLOUD_NAME!!)
        config.put("api_key", CLOUDINARY_API_KEY!!)
        config.put("api_secret", CLOUDINARY_API_SECRET!!)
        cloudinary = Cloudinary(config);
    }

    private fun prepareSpacePicForPublishing(spacePic: SpacePic) : SpacePic? {
        println("preparing SpacePic ${DateTime(spacePic.originallyPublishedAt).toString(DATE_FORMAT)}...")
        spacePic.status = SpacePicStatus.PUBLISHED
        spacePic.publishedAt = DateTime().toDate()

        try {
            val uploadResult = cloudinary?.uploader()?.upload(
                    spacePic.originalApiImageUrl, ObjectUtils.emptyMap()) ?: return null
            spacePic.hdImageUrl = uploadResult.get("url") as String
            val resizedUrl = cloudinary?.url()?.transformation(
                    Transformation().width(320).height(320).crop("fill")
                )?.generate(uploadResult.get("public_id") as String) ?: return null
            spacePic.previewImageUrl = resizedUrl
            return spacePic
        } catch (e : Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getSpacePicsToPublish(): List<SpacePic> {
        val toPublishQuery = em.createQuery(
                "FROM SpacePic WHERE status = :status ORDER BY createdAt DESC")
        toPublishQuery.setParameter("status", SpacePicStatus.CREATED)
        return toPublishQuery.resultList as List<SpacePic>
    }

    // TODO refator all APOD-related logic to a different class as soon as there are other SpacePic sources
    private fun downloadLatestAPODsMetadata(checkInterval : Int = 3): List<SpacePic> {
        println("=== APOD ===")
        println("checkInterval = $checkInterval")
        println("Checking for already downloaded APOD pictures...")

        val daysToFetch = getDaysThatNeedToFetchAPOD(checkInterval)
        val spacePics = daysToFetch.map { downloadAPODMetadata(it) }.filterNotNull()
        return spacePics
    }

    private fun downloadAPODMetadata(dayString: String): SpacePic? {
        println("Downloading APOD metadata of $dayString")
        val apodWebService = RetrofitFactory.get(APOD_BASE_URL).create(NasaAPODWebservice::class.java)
        val response = apodWebService.getAPOD(APOD_API_KEY, false, dayString).execute()
        if (!response.isSuccess) {
            println("url ${response.raw().request().urlString()}")
            println(response.errorBody().string())
            return null
        }
        val apod = response.body()
        val spacePic = SpacePic(
                originalApiUrl = response.raw().request().urlString(),
                originalApiImageUrl = apod.url,
                originallyPublishedAt = DateTimeFormat.forPattern(DATE_FORMAT).parseDateTime(dayString).toDate(),
                title = apod.title,
                createdAt = DateTime().toDate(),
                status = SpacePicStatus.CREATED,
                source = SpacePicSource.NASA_APOD,
                description = apod.explanation,
                hdImageUrl = "",
                previewImageUrl = "",
                publishedAt = null,
                deletedAt = null,
                updatedAt = null
        )
        if (apod.mediaType != "image") {
            spacePic.status = SpacePicStatus.DELETED
            spacePic.deletedAt = DateTime().toDate()
        }
        return spacePic
    }

    private fun getDaysThatNeedToFetchAPOD(checkInterval: Int): Set<String> {
        val startDate = DateTime().minusDays(checkInterval - 1)
                .withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        val latestQuery = em.createQuery(
                "FROM SpacePic WHERE source = :source AND originallyPublishedAt >= :originallyPublishedAt " +
                        "ORDER BY originallyPublishedAt DESC")
        latestQuery.setParameter("source", SpacePicSource.NASA_APOD)
        latestQuery.setParameter("originallyPublishedAt", startDate.toDate())
        val latestPersistedAPODs = latestQuery.resultList as List<SpacePic>
        val daysAlreadyFetchedAsDateStrings =
                latestPersistedAPODs.map { DateTime(it.originallyPublishedAt).toString(DATE_FORMAT) }.toSet()
        println("daysAlreadyFetched = $daysAlreadyFetchedAsDateStrings")

        val daysThatShouldBeFetchedAsDateStrings = intervalToListOfDateStrings(startDate, DateTime())
        println("daysThatShouldBeFetched = $daysThatShouldBeFetchedAsDateStrings")

        val daysToFetch = daysThatShouldBeFetchedAsDateStrings.minus(daysAlreadyFetchedAsDateStrings)
        println("daysToFetch = $daysToFetch")

        return daysToFetch
    }

    // TODO refactor
    private fun intervalToListOfDateStrings(startDate: DateTime, endDate: DateTime) : Set<String> {
        var dateStrings = linkedSetOf<String>()
        var currDate = startDate
        while (currDate <= endDate) {
            dateStrings.add(currDate.toString(DATE_FORMAT))
            currDate = currDate.plusDays(1)
        }
        return dateStrings
    }

    private fun persistNewSpacePic(spacePic: SpacePic) {
        em.transaction.begin()
        em.persist(spacePic)
        em.transaction.commit()
    }

    fun insertTestData() {
        val spacePics = getTestSpacePics()
        spacePics.forEach { persistNewSpacePic(it) }
    }

    private fun getTestSpacePics(): List<SpacePic> {
        val unpublishedSpacePic = SpacePic(
                originalApiUrl = "https://api.nasa.gov/planetary/apod?concept_tags=True&api_key=DEMO_KEY",
                originalApiImageUrl = "http://apod.nasa.gov/apod/image/1512/Refsdal_Hubble_1080.jpg",
                originallyPublishedAt = DateTime().toDate(),
                title = "SN Refsdal: The First Predicted Supernova Image",
                createdAt = DateTime().toDate(),
                status = SpacePicStatus.CREATED,
                source = SpacePicSource.NASA_APOD
        )

        val spacePic = SpacePic(
                originalApiUrl = "https://api.nasa.gov/planetary/apod?concept_tags=True&api_key=DEMO_KEY",
                originalApiImageUrl = "http://apod.nasa.gov/apod/image/1512/Refsdal_Hubble_1080.jpg",
                originallyPublishedAt = DateTime().toDate(),
                title = "SN Refsdal: The First Predicted Supernova Image",
                createdAt = DateTime().toDate(),
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
                publishedAt = DateTime().toDate(),
                deletedAt = null,
                updatedAt = DateTime().toDate()
        )

        return listOf(spacePic, unpublishedSpacePic)
    }

    fun close() {
        if (em.isOpen()) {
            em.entityManagerFactory.close()
        }
    }
}
