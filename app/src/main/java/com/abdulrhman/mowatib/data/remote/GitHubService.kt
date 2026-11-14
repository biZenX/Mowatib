package com.abdulrhman.mowatib.data.remote

import com.abdulrhman.mowatib.data.remote.model.GitHubRelease
import retrofit2.Response
import retrofit2.http.GET

interface GitHubService {
    @GET("repos/biZenX/Mowatib/releases/latest")
    suspend fun getLatestRelease(): Response<GitHubRelease>

    @GET("repos/biZenX/Mowatib/releases")
    suspend fun getAllReleases(): Response<List<GitHubRelease>>
}
