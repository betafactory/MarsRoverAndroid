package com.androidyug.marsrover.data.model

import java.io.Serializable
import java.util.HashMap


class Camera : Serializable {

    private var id: Int? = null
    private var name: String? = null
    private val roverId: Int? = null
    private val full_name: String? = null
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
     * The roverId
     */
    fun getRoverId(): Int? {
        return roverId
    }


    /**
     *
     * @return
     * The fullName
     */
    fun getFullName(): String? {
        return full_name
    }




    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}
