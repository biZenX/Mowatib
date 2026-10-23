package com.etrisad.zenith.data.remote

import com.etrisad.zenith.data.remote.model.GitHubRelease
import retrofit2.Response
import retrofit2.http.GET

interface GitHubService {
    @GET("repos/1372Slash/Zenith/releases/latest")
    suspend fun getLatestRelease(): Response<GitHubRelease>

    @GET("repos/1372Slash/Zenith/releases")
    suspend fun getAllReleases(): Response<List<GitHubRelease>>
}
