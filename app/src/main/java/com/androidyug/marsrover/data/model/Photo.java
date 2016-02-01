
package com.androidyug.marsrover.data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Photo implements Serializable {

    private Integer id;
    private Integer sol;
    private Camera camera;
    private String img_src;
    private String earth_date;
    private Rover rover;
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
     *     The sol
     */
    public Integer getSol() {
        return sol;
    }

    /**
     * 
     * @param sol
     *     The sol
     */
    public void setSol(Integer sol) {
        this.sol = sol;
    }

    /**
     * 
     * @return
     *     The camera
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * 
     * @param camera
     *     The camera
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * 
     * @return
     *     The imgSrc
     */
    public String getImgSrc() {
        return img_src;
    }

    /**
     * 
     * @param imgSrc
     *     The img_src
     */
    public void setImgSrc(String imgSrc) {
        this.img_src = imgSrc;
    }

    /**
     * 
     * @return
     *     The earthDate
     */
    public String getEarthDate() {
        return earth_date;
    }

    /**
     * 
     * @param earthDate
     *     The earth_date
     */
    public void setEarthDate(String earthDate) {
        this.earth_date = earthDate;
    }

    /**
     * 
     * @return
     *     The rover
     */
    public Rover getRover() {
        return rover;
    }

    /**
     * 
     * @param rover
     *     The rover
     */
    public void setRover(Rover rover) {
        this.rover = rover;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
