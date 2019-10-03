package com.androidyug.marsrover.data.model

import java.io.Serializable
import java.util.HashMap


class Photo : Serializable {
    private var id: Int? = null
    private var sol: Int? = null
    private var camera: Camera? = null
    private var img_src: String? = null
    private var earth_date: String? = null
    private var rover: Rover? = null
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
     * The sol
     */
    fun getSol(): Int? {
        return sol
    }

    /**
     *
     * @param sol
     * The sol
     */
    fun setSol(sol: Int?) {
        this.sol = sol
    }

    /**
     *
     * @return
     * The camera
     */
    fun getCamera(): Camera? {
        return camera
    }

    /**
     *
     * @param camera
     * The camera
     */
    fun setCamera(camera: Camera) {
        this.camera = camera
    }

    /**
     *
     * @return
     * The imgSrc
     */
    fun getImgSrc(): String? {
        return img_src
    }

    /**
     *
     * @param imgSrc
     * The img_src
     */
    fun setImgSrc(imgSrc: String) {
        this.img_src = imgSrc
    }

    /**
     *
     * @return
     * The earthDate
     */
    fun getEarthDate(): String? {
        return earth_date
    }

    /**
     *
     * @param earthDate
     * The earth_date
     */
    fun setEarthDate(earthDate: String) {
        this.earth_date = earthDate
    }

    /**
     *
     * @return
     * The rover
     */
    fun getRover(): Rover? {
        return rover
    }

    /**
     *
     * @param rover
     * The rover
     */
    fun setRover(rover: Rover) {
        this.rover = rover
    }

    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }
}
