package ar.com.ordia.collaborativesamples.dto;

import java.util.ArrayList;

/**
 * Created by hordia on 6/15/17.
 */

/*
 Remote sound resource Data Transfer Object
 Freesound Sound Resource DTO

 JSON reference: http://www.freesound.org/docs/api/resources_apiv2.html#sound-resources
 */
public class FreesoundResourceDTO extends SoundResourceDTO {

    //private ArrayList<String> tags; //FIXME: Expected a string but was BEGIN_ARRAY

    private String type;
    private String channels;
    private String filesize;
    private String bitrate;
    private String bitdepth;
    private String samplerate;
    private String pack;
    private String pack_name;
    private String download; //Download link //IMPORTANTE
    private String bookmark;
    //private ArrayList<String> previews;
    //private String ArrayList<images>; //FIXME: importante? agregar a la interfaz alguna ¿?

    //menos importantes?
    /*
    private String num_downloads;
    private String avg_rating;
    private String num_ratings;
    private String rate;
    private String comments;
    private String num_comments;
    private String comment;
    private String similar_sounds;
    private String analysis;
    private String analysis_frames;
    private String analysis_stats;
    */

//generated

/*
    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
    }
*/

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getBitdepth() {
        return bitdepth;
    }

    public void setBitdepth(String bitdepth) {
        this.bitdepth = bitdepth;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSamplerate() {
        return samplerate;
    }

    public void setSamplerate(String samplerate) {
        this.samplerate = samplerate;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getPack_name() {
        return pack_name;
    }

    public void setPack_name(String pack_name) {
        this.pack_name = pack_name;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }
}
