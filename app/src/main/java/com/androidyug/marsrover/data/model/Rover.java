
package com.androidyug.marsrover.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Rover implements Serializable {

    private Integer id;
    private String name;
    private String landing_date;
    private Integer max_sol;
    private String max_date;
    private Integer total_photos;
    private List<Camera_> cameras = new ArrayList<Camera_>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The landingDate
     */
    public String getLandingDate() {
        return landing_date;
    }



    /**
     * 
     * @return
     *     The maxSol
     */
    public Integer getMaxSol() {
        return max_sol;
    }


    /**
     * 
     * @return
     *     The maxDate
     */
    public String getMaxDate() {
        return max_date;
    }


    /**
     * 
     * @return
     *     The totalPhotos
     */
    public Integer getTotalPhotos() {
        return total_photos;
    }



    /**
     * 
     * @return
     *     The cameras
     */
    public List<Camera_> getCameras() {
        return cameras;
    }

    /**
     * 
     * @param cameras
     *     The cameras
     */
    public void setCameras(List<Camera_> cameras) {
        this.cameras = cameras;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
