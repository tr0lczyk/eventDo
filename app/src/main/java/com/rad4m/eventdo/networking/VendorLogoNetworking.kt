package com.rad4m.eventdo.networking

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Base64
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.R
import com.rad4m.eventdo.di.ApiModule.Companion.BASE_URL
import com.rad4m.eventdo.models.Result
import com.rad4m.eventdo.models.VendorLogoResponse
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import timber.log.Timber

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private const val GET_VENDOR_LOGO = "img/{vendor_id}"

interface VendorLogoService {

    @GET(GET_VENDOR_LOGO)
    suspend fun getVendorLogo(
        @Header("Authorization") token: String,
        @Path("vendor_id") phoneNumber: String
    ): Response<VendorLogoResponse>
}

object VendorLogoApi {
    val retrofitService: VendorLogoService by lazy {
        Timber.d("Creating retrofit client")

        val logging = HttpLoggingInterceptor()
        logging.apply { logging.level = HttpLoggingInterceptor.Level.BODY }
        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor(logging)

        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .client(httpClient.build())
            .build()

        return@lazy retrofit.create(VendorLogoService::class.java)
    }
}

class VendorLogoRepository(application: Application) : BaseRepository() {

    val sharedPrefs = SharedPreferences(application, moshi)
    private val token = sharedPrefs.getValueString(Utilities.USER_TOKEN)
    private val userToken = "bearer $token"

    suspend fun getVendorLogo(
        vendorId: String
    ): Result<VendorLogoResponse?> {
        return baseApiCall(
            block = {
                VendorLogoApi.retrofitService.getVendorLogo(
                    userToken,
                    vendorId
                )
            }
        )
    }
}

class VendorLogoNetworking {

    companion object {

        val application = EventDoApplication.instance
        private val repository = VendorLogoRepository(application)
        private val vendorLogoJob = Job()
        private val vendorLogoScope = CoroutineScope(Dispatchers.Main + vendorLogoJob)

        fun downloadLogo(vendorId: String, view: CircleImageView) {
            vendorLogoScope.launch {
                when (val response = repository.getVendorLogo(vendorId)) {
                    is Result.Success -> view.loadNewImage(response.data?.result)
                    is Result.Failure -> view.setImageResource(R.drawable.icon_logo)
                    is Result.Error -> view.setImageResource(R.drawable.icon_logo)
                }
            }
        }

        private fun CircleImageView.loadNewImage(link: String?) {
            if (link != null && link.isNotEmpty()) {
                val newLink = link.removePrefix("data:image/gif;base64,")
                val decodedString = Base64.decode(newLink, Base64.DEFAULT)
                val bitmap2 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                this.setImageBitmap(bitmap2)
            } else {
                this.setImageResource(R.drawable.icon_logo)
                Timber.i("beenherenow")
            }
        }
    }
}