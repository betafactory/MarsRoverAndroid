package com.androidyug.marsrover.data.model

import java.io.Serializable
import java.util.HashMap


class Camera_ : Serializable {

    /**
     *
     * @return
     * The name
     */
    /**
     *
     * @param name
     * The name
     */
    var name: String? = null
    /**
     *
     * @return
     * The fullName
     */
    /**
     *
     * @param fullName
     * The full_name
     */
    var fullName: String? = null
    private val additionalProperties = HashMap<String, Any>()

    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}
