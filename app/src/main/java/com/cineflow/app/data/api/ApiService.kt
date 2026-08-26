package com.cineflow.app.data.api

import com.cineflow.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

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
}
