package br.com.dgimenes.nasapicserver.model

import java.util.*
import javax.persistence.*


@Table(name = "space_pic"
        // TODO create index in an automated way
        //, indexes = { @Index(name = "my_index_name2", columnList = "name", unique = false) }
)
@Entity(name = "SpacePic")
class SpacePic {

    constructor()

    constructor(originalApiUrl: String, originalApiImageUrl: String, originallyPublishedAt: Date,
                title: String, createdAt: Date, status: SpacePicStatus, source: SpacePicSource) {
        this.originalApiUrl = originalApiUrl
        this.originalApiImageUrl = originalApiImageUrl
        this.originallyPublishedAt = originallyPublishedAt
        this.title = title
        this.createdAt = createdAt
        this.status = status
        this.source = source
    }

    constructor(description: String?, hdImageUrl: String?, previewImageUrl: String?,
                originalApiUrl: String, originalApiImageUrl: String, originallyPublishedAt: Date,
                title: String, createdAt: Date, publishedAt: Date?, updatedAt: Date?,
                deletedAt: Date?, status: SpacePicStatus, source: SpacePicSource) {
        this.description = description
        this.hdImageUrl = hdImageUrl
        this.previewImageUrl = previewImageUrl
        this.originalApiUrl = originalApiUrl
        this.originalApiImageUrl = originalApiImageUrl
        this.originallyPublishedAt = originallyPublishedAt
        this.title = title
        this.createdAt = createdAt
        this.publishedAt = publishedAt
        this.updatedAt = updatedAt
        this.deletedAt = deletedAt
        this.status = status
        this.source = source
    }

    @Id
    @SequenceGenerator(name = "space_pic_id_seq",
            sequenceName = "space_pic_id_seq",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "space_pic_id_seq")
    @Column(updatable = false)
    var id: Int? = null

    @Column(nullable = true, length = 768)
    var hdImageUrl: String? = null

    @Column(nullable = true, length = 768)
    var previewImageUrl: String? = null

    @Column(nullable = false, length = 768)
    var originalApiUrl: String? = null

    @Column(nullable = false, length = 768)
    var originalApiImageUrl: String? = null

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    var originallyPublishedAt: Date? = null

    @Column(nullable = false, length = 256)
    var title: String? = null

    @Column(nullable = true, length = 2560)
    var description: String? = null

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var createdAt: Date? = null

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    var publishedAt: Date? = null

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    var updatedAt: Date? = null

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    var deletedAt: Date? = null

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    var status: SpacePicStatus? = null

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    var source: SpacePicSource? = null

    @Column(nullable = true)
    var best: Boolean? = false
        get
        set
}

enum class SpacePicSource {
    NASA_APOD,
    NASA_IOTD
}

enum class SpacePicStatus {
    CREATED,
    PUBLISHED,
    DELETED
}
