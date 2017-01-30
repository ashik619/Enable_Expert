package in.co.codoc.enableexpert;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by ashik619 on 24-11-2016.
 */
public interface FileUploadService {
    @Multipart
    @POST("report")
    Call<ResponseBody> upload(@Part("report_id") RequestBody description,
                              @Part MultipartBody.Part file);

    @Multipart
    @POST("file_upload1")
    Call<ResponseBody> updateProfile(@Part("user_id") RequestBody user_id,
                                     @Part("name") RequestBody name,
                                     @Part("about_me") RequestBody about_me,
                                     @Part("medical_education") RequestBody medical_education,
                                     @Part("location") RequestBody location,
                                     @Part("is_expert") RequestBody is_expert,
                                     @Part MultipartBody.Part file);
    @Multipart
    @POST("initreport")
    Call<ResponseBody> reportUpload(@Part("user_id") RequestBody user_id,
                                    @Part("p_name") RequestBody p_name,
                                    @Part("p_age") RequestBody p_age,
                                    @Part("p_gender") RequestBody p_gender,
                                    @Part("p_history") RequestBody p_history,
                                    @Part("diff_diagnosis") RequestBody diff_diagnosis,
                                    @Part("note_to_expert") RequestBody note_to_expert,
                                    @Part("expert_id") RequestBody expert_id,
                                    @Part MultipartBody.Part file);

}
