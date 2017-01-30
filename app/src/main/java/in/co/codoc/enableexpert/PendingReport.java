package in.co.codoc.enableexpert;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ashik619 on 20-01-2017.
 */
public class PendingReport extends RealmObject {
    public String docter_id;
    public String expert_id;
    public String expertName;
    public String thubUrl;
    public String imageUrl;
    public String qualification;
    public String location;
    public String about_me;
    @PrimaryKey
    private String report_id;

    //setter

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAbout_me(String about_me) {
        this.about_me = about_me;
    }

    public void setDocter_id(String docter_id) {
        this.docter_id = docter_id;
    }

    public void setExpert_id(String expert_id) {
        this.expert_id = expert_id;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public void setReport_id(String report_id) {
        this.report_id = report_id;
    }

    public void setThubUrl(String thubUrl) {
        this.thubUrl = thubUrl;
    }
    //getter

    public String getDocter_id() {
        return docter_id;
    }

    public String getAbout_me() {
        return about_me;
    }

    public String getExpert_id() {
        return expert_id;
    }

    public String getExpertName() {
        return expertName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public String getThubUrl() {
        return thubUrl;
    }

    public String getQualification() {
        return qualification;
    }

    public String getReport_id() {
        return report_id;
    }
}
