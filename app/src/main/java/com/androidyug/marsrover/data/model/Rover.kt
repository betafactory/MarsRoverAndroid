package com.androidyug.marsrover.data.model

import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap


class Rover : Serializable {

    private var id: Int? = null
    private var name: String? = null
    private val landing_date: String? = null
    private val max_sol: Int? = null
    private val max_date: String? = null
    private val total_photos: Int? = null
    private var cameras: List<Camera_> = ArrayList()
    private val additionalProperties = HashMap<String, Any>()

    /**
     *
     * @return
     * The id
     */
    fun getId(): Int? {
        return id
    }

    /**
     *
     * @param id
     * The id
     */
    fun setId(id: Int?) {
        this.id = id
    }

    /**
     *
     * @return
     * The name
     */
    fun getName(): String? {
        return name
    }

    /**
     *
     * @param name
     * The name
     */
    fun setName(name: String) {
        this.name = name
    }

    /**
     *
     * @return
     * The landingDate
     */
    fun getLandingDate(): String? {
        return landing_date
    }


    /**
     *
     * @return
     * The maxSol
     */
    fun getMaxSol(): Int? {
        return max_sol
    }


    /**
     *
     * @return
     * The maxDate
     */
    fun getMaxDate(): String? {
        return max_date
    }


    /**
     *
     * @return
     * The totalPhotos
     */
    fun getTotalPhotos(): Int? {
        return total_photos
    }


    /**
     *
     * @return
     * The cameras
     */
    fun getCameras(): List<Camera_> {
        return cameras
    }

    /**
     *
     * @param cameras
     * The cameras
     */
    fun setCameras(cameras: List<Camera_>) {
        this.cameras = cameras
    }

    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}
