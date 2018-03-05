package dbxprts.terminaltrak;

import android.app.Application;

/**
 * Created by Diego on 03/03/2017.
 */

public class LastRecordedLocation extends Application {

    private String latitude;
    private String longitude;
    private String user;
    private String id_area;

    public String getLastRecordedLatitude() {
        return latitude;
    }

    public String getLastRecordedLongitude() {
        return longitude;
    }

    public String getActiveUser(){
        return user;
    }

    public String getIdArea(){ return id_area; }

    public void setIdArea(String id_area){
        this.id_area = id_area;
    }

    public void setLastRecordedLocation(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setActiveUser(String user){
        this.user = user;
    }
}
