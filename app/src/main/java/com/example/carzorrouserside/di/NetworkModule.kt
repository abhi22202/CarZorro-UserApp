package com.example.carzorrouserside.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.carzorrouserside.data.api.BookingApiService
import com.example.carzorrouserside.data.api.homescreen.AddressApiService
import com.example.carzorrouserside.data.api.myprofile.OrderApiInterface
import com.example.carzorrouserside.data.api.homescreen.BannerApiService
import com.example.carzorrouserside.data.api.homescreen.BrandModelApiService
import com.example.carzorrouserside.data.api.homescreen.CarApiService
import com.example.carzorrouserside.data.api.homescreen.HomepageServiceApi
import com.example.carzorrouserside.data.api.homescreen.OrderApiService
import com.example.carzorrouserside.data.api.homescreen.PackageApiService
import com.example.carzorrouserside.data.api.homescreen.ProductApiService
import com.example.carzorrouserside.data.api.homescreen.TestimonialApiService
import com.example.carzorrouserside.data.api.loginscreen.LogoutApiService
import com.example.carzorrouserside.data.api.loginscreen.UserAuthApiService
import com.example.carzorrouserside.data.api.register.UserAuthRegisterApiService
import com.example.carzorrouserside.data.api.vendor.VendorApiService
import com.example.carzorrouserside.data.repository.BookingRepositoryImpl
import com.example.carzorrouserside.data.repository.car.CarRepository
import com.example.carzorrouserside.data.repository.homescreen.*
import com.example.carzorrouserside.data.repository.login.LogoutRepository
import com.example.carzorrouserside.data.repository.register.UserAuthRegisterRepository
import com.example.carzorrouserside.data.repository.vendor.FavouriteVendorRepository
import com.example.carzorrouserside.data.repository.vendor.VendorDetailsRepository
import com.example.carzorrouserside.domain.repository.BookingRepository
import com.example.carzorrouserside.data.token.BookingSessionManager
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.di.SharedPreferencesManager
import com.example.carzorrouserside.util.LocationManager
import com.example.carzorrouserside.utils.token.UserAutomatedTokenExpirationHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

// Removed simple AuthInterceptor - using UserAuthInterceptor instead

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserRetrofit

@Module
@InstallIn(SingletonComponent::class)
object UserNetworkModule {

    private const val USER_BASE_URL = "https://carzoro-user-api.alphaprotocall.com/public/api/"

    @Provides
    @Singleton
    fun provideUserGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .serializeNulls() // Include null values explicitly
            .create()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("carzorro_user_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        userPreferencesManager: UserPreferencesManager,
        tokenHandler: UserAutomatedTokenExpirationHandler
    ): UserAuthInterceptor {
        return UserAuthInterceptor(userPreferencesManager, tokenHandler)
    }
//    @Provides
//    @Singleton
//    fun provideFcmApiService(retrofit: Retrofit): FcmApiService {
//        return retrofit.create(FcmApiService::class.java)
//    }

    @Provides
    @Singleton
    fun provideUserOkHttpClient(authInterceptor: UserAuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS // Changed to HEADERS to see Authorization header
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Add auth interceptor FIRST so headers are added before logging
            .addInterceptor(loggingInterceptor) // Then log the request with all headers
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    @UserRetrofit
    fun provideUserRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideUserAutomatedTokenExpirationHandler(
        userPreferencesManager: UserPreferencesManager,
        application: Application
    ): UserAutomatedTokenExpirationHandler {
        return UserAutomatedTokenExpirationHandler(userPreferencesManager, application)
    }


    @Provides
    @Singleton
    fun provideHomepageServiceApi(@UserRetrofit retrofit: Retrofit): HomepageServiceApi {
        return retrofit.create(HomepageServiceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserAuthApiService(@UserRetrofit retrofit: Retrofit): UserAuthApiService {
        return retrofit.create(UserAuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideVendorApiService(@UserRetrofit retrofit: Retrofit): VendorApiService {
        return retrofit.create(VendorApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserAuthRegisterApiService(@UserRetrofit retrofit: Retrofit): UserAuthRegisterApiService {
        return retrofit.create(UserAuthRegisterApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLogoutApiService(@UserRetrofit retrofit: Retrofit): LogoutApiService {
        return retrofit.create(LogoutApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBannerApiService(@UserRetrofit retrofit: Retrofit): BannerApiService {
        return retrofit.create(BannerApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAddressApiService(@UserRetrofit retrofit: Retrofit): AddressApiService {
        return retrofit.create(AddressApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBrandModelApiService(@UserRetrofit retrofit: Retrofit): BrandModelApiService {
        return retrofit.create(BrandModelApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCarApiService(@UserRetrofit retrofit: Retrofit): CarApiService {
        return retrofit.create(CarApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProductApiService(@UserRetrofit retrofit: Retrofit): ProductApiService {
        return retrofit.create(ProductApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOrderApiService(@UserRetrofit retrofit: Retrofit): OrderApiService {
        return retrofit.create(OrderApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBookingApiService(@UserRetrofit retrofit: Retrofit): BookingApiService {
        return retrofit.create(BookingApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOrderApiInterface(@UserRetrofit retrofit: Retrofit): OrderApiInterface {
        return retrofit.create(OrderApiInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideTestimonialApiService(@UserRetrofit retrofit: Retrofit): TestimonialApiService {
        return retrofit.create(TestimonialApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePackageApiService(@UserRetrofit retrofit: Retrofit): PackageApiService {
        return retrofit.create(PackageApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideCarRepository(apiService: CarApiService): CarRepository {
        return CarRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideFavouriteVendorRepository(apiService: VendorApiService): FavouriteVendorRepository {
        return FavouriteVendorRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideVendorDetailsRepository(
        apiService: VendorApiService,
        gson: Gson
    ): VendorDetailsRepository {
        return VendorDetailsRepository(apiService, gson)
    }

    @Provides
    @Singleton
    fun providePopularAreaRepository(
        api: HomepageServiceApi,
        userPreferencesManager: UserPreferencesManager,
        locationManager: LocationManager,
        sharedPreferencesManager: SharedPreferencesManager
    ): PopularAreaRepository {
        return PopularAreaRepository(api, userPreferencesManager, locationManager, sharedPreferencesManager)
    }

    @Provides
    @Singleton
    fun provideTestimonialRepository(
        apiService: TestimonialApiService,
        userPreferencesManager: UserPreferencesManager
    ): TestimonialRepository {
        return TestimonialRepository(apiService, userPreferencesManager)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        apiService: OrderApiService,
        userPreferencesManager: UserPreferencesManager
    ): OrderRepository {
        return OrderRepository(
            orderApiService = apiService,
            userPreferencesManager = userPreferencesManager
        )
    }

    /*
    @Provides
    @Singleton
    fun provideAddressRepository(
        apiService: AddressApiService,
        userPreferencesManager: UserPreferencesManager,
        sharedPreferencesManager: SharedPreferencesManager,
        locationManager: LocationManager
    ): AddressRepository {
        return AddressRepository(
            apiService = apiService,
            userPreferencesManager = userPreferencesManager,
            sharedPreferencesManager = sharedPreferencesManager,
            locationManager = locationManager
        )
    }
    */

    @Provides
    @Singleton
    fun provideHomepageServiceRepository(
        api: HomepageServiceApi,
        userPreferencesManager: UserPreferencesManager
    ): HomepageServiceRepository {
        return HomepageServiceRepository(api, userPreferencesManager)
    }

    @Provides
    @Singleton
    fun provideLogoutRepository(
        logoutApiService: LogoutApiService,
        userPreferencesManager: UserPreferencesManager,
        bookingSessionManager: BookingSessionManager,
        gson: Gson
    ): LogoutRepository {
        return LogoutRepository(logoutApiService, userPreferencesManager, bookingSessionManager, gson)
    }

    @Provides
    @Singleton
    fun provideUserAuthRepository(
        apiService: UserAuthRegisterApiService,
        gson: Gson
    ): UserAuthRegisterRepository {
        return UserAuthRegisterRepository(apiService, gson)
    }

    @Provides
    @Singleton
    fun provideBannerRepository(
        bannerApiService: BannerApiService,
        userPreferencesManager: UserPreferencesManager,
        locationManager: LocationManager,
        gson: Gson
    ): BannerRepository {
        return BannerRepository(bannerApiService, userPreferencesManager, locationManager, gson)
    }

    @Provides
    @Singleton
    fun providePackageRepository(
        apiService: PackageApiService,
        locationManager: LocationManager
    ): PackageRepository {
        return PackageRepository(apiService, locationManager)
    }

    @Provides
    @Singleton
    fun provideBookingRepository(
        apiService: BookingApiService,
        gson: Gson,
        userPreferencesManager: UserPreferencesManager
    ): BookingRepository {
        return BookingRepositoryImpl(apiService, gson, userPreferencesManager)
    }


    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context
    ): LocationManager {
        return LocationManager(context)
    }
}

