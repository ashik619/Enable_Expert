package in.co.codoc.enableexpert;

import io.realm.RealmObject;

/**
 * Created by ashik619 on 19-12-2016.
 */
public class Diagnosis extends RealmObject {
    String diagnosisText;
    public String getDiagnosisText() {
        return diagnosisText;
    }

    public void setDiagnosisText(String diagnosisText) {
        this.diagnosisText = diagnosisText;
    }
}
