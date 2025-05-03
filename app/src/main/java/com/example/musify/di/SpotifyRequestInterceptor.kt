package com.example.musify.di
//
//import com.example.musify.data.repositories.tokenrepository.TokenRepository
//import kotlinx.coroutines.runBlocking
//import okhttp3.Interceptor
//import okhttp3.Response
//import javax.inject.Inject
//import javax.inject.Named
//
//class SpotifyRequestInterceptor @Inject constructor(
//    private val tokenRepo: TokenRepository,
//    @Named("defaultMarket") private val defaultMarket: String,
//    @Named("defaultLocale") private val defaultLocale: String
//) : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val original = chain.request()
//        val token = runBlocking { tokenRepo.getValidBearerToken() }
//        val url = original.url.newBuilder()
//            .apply {
//                if (original.url.queryParameter("country") == null) {
//                    addQueryParameter("country", defaultMarket)
//                }
//                if (original.url.queryParameter("locale") == null) {
//                    addQueryParameter("locale", defaultLocale)
//                }
//            }
//            .build()
//
//        val request = original.newBuilder()
//            .url(url)
//            .addHeader("Authorization", token.toString())
//            .build()
//
//        return chain.proceed(request)
//    }
//}