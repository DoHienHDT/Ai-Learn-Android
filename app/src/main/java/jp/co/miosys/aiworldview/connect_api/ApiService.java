package jp.co.miosys.aiworldview.connect_api;

import java.util.List;

import jp.co.miosys.aiworldview.data_post_response.BaseDataResponse;
import jp.co.miosys.aiworldview.data_post_response.Company;
import jp.co.miosys.aiworldview.data_post_response.DataResponseLogin;
import jp.co.miosys.aiworldview.data_post_response.ResponseMemo;
import jp.co.miosys.aiworldview.static_value.ValueStatic;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import jp.co.miosys.aiworldview.data_post_response.Category;

public interface ApiService {

    @GET("api/v1/memo/list")
    Call<ResponseMemo> apiGetListMemo(
            @Header("Authorization") String authorHeader,
            @Query("username") String userName,
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("square") int square);

    @GET("https://ai-con-stg2.aimap.jp/api/v1/category/list")
    Call<BaseDataResponse<List<Category>>> apiGetListCategory(
            @Header("Authorization") String authorHeader
    );

    @FormUrlEncoded
    @POST("api/v1/user/company-code")
    Call<BaseDataResponse<Company>> apiLoginFirst(
            @Field("app_token") String appToken,
            @Field("company_code") String companyCode
    );

    @FormUrlEncoded
    @POST("/api/v1/user/login")
    Call<DataResponseLogin> apiLogin(
            @Field("app_token") String appToken,
            @Field("username") String userName,
            @Field("password") String password,
            @Field("uuid") String uuid,
            @Field("kind") String kind
    );
}
