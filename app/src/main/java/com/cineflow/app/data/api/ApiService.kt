package com.cineflow.app.data.api

import com.cineflow.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/app/auth/nonce")
    suspend fun getNonce(@Body request: AuthNonceRequest): Response<BaseResponse<AuthNonceResponseData>>

    @POST("api/app/auth/login/google-account")
    suspend fun loginWithGoogle(@Body request: GoogleAccountLoginRequest): Response<BaseResponse<AuthLoginResponseData>>

    @GET("api/modelles/models")
    suspend fun getModels(): Response<BaseResponse<List<StreamingModel>>>

    @GET("api/modelles/categories")
    suspend fun getCategories(@Query("model_id") modelId: String): Response<BaseResponse<CategoryResponseData>>

    @GET("api/modelles/videos")
    suspend fun getVideos(
        @Query("model_id") modelId: String,
        @Query("category_id") categoryId: String? = null,
        @Query("page") page: Int = 1
    ): Response<BaseResponse<VideoListResponseData>>

    @GET("api/modelles/detail")
    suspend fun getDetail(
        @Query("model_id") modelId: String,
        @Query("id") id: String
    ): Response<BaseResponse<UnifiedDetailResponse>>

    @GET("api/modelles/source")
    suspend fun getSource(
        @Query("model_id") modelId: String,
        @Query("episode_id") episodeId: String,
        @Query("id") id: String
    ): Response<BaseResponse<UnifiedVideoSourceResponse>>

    @POST("api/modelles/search")
    suspend fun search(@Body request: SearchRequest): Response<BaseResponse<SearchResponseData>>

    @POST("api/app/auth/refresh-token")
    suspend fun refreshToken(@Body request: AuthRefreshTokenRequest): Response<BaseResponse<AuthRefreshTokenResponseData>>

    @GET("api/app/auth/me")
    suspend fun getCurrentUser(): Response<BaseResponse<AuthCurrentUserData>>

    @POST("api/app/auth/device/pairing")
    suspend fun startDeviceLink(@Body request: DeviceLinkStartRequest): Response<BaseResponse<DeviceLinkStartResponseData>>

    @GET("api/app/auth/device/status")
    suspend fun getDeviceLinkStatus(
        @Query("device_code") deviceCode: String,
        @Query("app_instance_id") appInstanceId: String
    ): Response<BaseResponse<DeviceLinkStatusData>>

    @POST("api/app/auth/device/exchange")
    suspend fun exchangeDeviceLink(@Body request: DeviceLinkExchangeRequest): Response<BaseResponse<DeviceLinkExchangeResponseData>>
}
