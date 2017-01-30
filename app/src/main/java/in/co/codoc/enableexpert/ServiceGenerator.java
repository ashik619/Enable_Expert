package in.co.codoc.enableexpert;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ashik619 on 24-11-2016.
 */
public class ServiceGenerator {
    public static final String baseUrla = Constants.BASE_URL;
    public static final String API_BASE_URL = baseUrla+"/imageuploader/";

   /* private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();*/
   private static OkHttpClient httpClient = new OkHttpClient().newBuilder()
           .connectTimeout(120 , TimeUnit.SECONDS)
           .readTimeout(120 , TimeUnit.SECONDS)
           .writeTimeout(120 , TimeUnit.SECONDS)
           .build();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    /*public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }*/
    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(serviceClass);
    }
}