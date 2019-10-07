package com.androidyug.marsrover.data.model

import java.util.ArrayList
import java.util.HashMap


class Dataset {

    /**
     *
     * @return
     * The photos
     */
    /**
     *
     * @param photos
     * The photos
     */
    var photos: List<Photo> = ArrayList()
    private val additionalProperties = HashMap<String, Any>()

    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}
