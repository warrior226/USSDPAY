package com.example.mobilemhealthpay.di

import android.content.Context
import androidx.room.Room
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.Dao.BeneficiaireDao
import com.example.mobilemhealthpay.data.Dao.PaiementBeneficiaireDao
import com.example.mobilemhealthpay.data.Dao.PaiementDao
import com.example.mobilemhealthpay.data.Dao.RefundDao
import com.example.mobilemhealthpay.data.Dao.TransactionInfoDao
import com.example.mobilemhealthpay.data.Dao.VirementDao
import com.example.mobilemhealthpay.data.remote.Api_Service.PaiementApiService
import com.example.mobilemhealthpay.data.remote.WSApiInterceptor
import com.example.mobilemhealthpay.data.repository_implementation.PaiementRepositoryImpl
import com.example.mobilemhealthpay.data.repository_implementation.TransactionRepositoryImpl
import com.example.mobilemhealthpay.domain.PaiementRepository
import com.example.mobilemhealthpay.domain.usecases.FetchPaiementsUseCase
import com.example.mobilemhealthpay.domain.usecases.GetBeneficiairesUseCase
import com.example.mobilemhealthpay.domain.usecases.GetPaiementWithBeneficiairesUsecase
import com.example.mobilemhealthpay.domain.usecases.GetPaiementWithBeneficiairesUsecaseById
import com.example.mobilemhealthpay.domain.usecases.GetPaiementsUseCase
import com.example.mobilemhealthpay.domain.usecases.TransactionUseCase
import com.example.mobilemhealthpay.repository.TransactionRepository
import com.example.mobilemhealthpay.utils.Constant
import com.example.mobilemhealthpay.utils.NetworkConnectivityObserver
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    //private const val BASE_URL ="https://apipay.mhealth-africa.org/docs/"
    private const val BASE_URL ="https://dev.lagfo.com/wallet/"
    private const val WS_CALL_TIMEOUT_SECONDS = 60L

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(WSApiInterceptor())
            .callTimeout(60L, TimeUnit.SECONDS)
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun providePaymentApiService(retrofit: Retrofit): PaiementApiService {
        return retrofit.create(PaiementApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePaiementRepository(
        apiService: PaiementApiService,
        paiementDao: PaiementDao,
        beneficiaireDao: BeneficiaireDao, // Fixed typo: beficiaireDao -> beneficiaireDao
        paiementBeneficiaireDao: PaiementBeneficiaireDao
    ): PaiementRepository = PaiementRepositoryImpl(
        apiService = apiService,
        paiementDao = paiementDao,
        beneficiaireDao = beneficiaireDao,
        paiementBeneficiaireDao = paiementBeneficiaireDao
    )

    @Provides
    @Singleton
    fun provideTransactionRepository(
        apiService: PaiementApiService,
        appDataBase: AppDataBase
    ): TransactionRepository= TransactionRepositoryImpl(
        paiementApiService =apiService,
        appDataBase = appDataBase
    )

    @Provides
    @Singleton
    fun provideTransactionUseCase(
        repository: TransactionRepository
    ): TransactionUseCase{
        return TransactionUseCase(transactionRepository = repository)
    }
    @Provides
    @Singleton
    fun provideFetchPaiementUseCase(
        repository: PaiementRepository
    ): FetchPaiementsUseCase {
        return FetchPaiementsUseCase(repository = repository)
    }

    @Provides
    @Singleton
    fun provideGetBeneficiaireUseCase(
        repository: PaiementRepository
    ): GetBeneficiairesUseCase {
        return GetBeneficiairesUseCase(repository = repository)
    }

    @Provides
    @Singleton
    fun provideGetPaiementsUseCase(
        repository: PaiementRepository
    ): GetPaiementsUseCase {
        return GetPaiementsUseCase(repository = repository)
    }

    @Provides
    @Singleton
    fun provideGetPaiementWithBeneficiairesUseCase(
        repository: PaiementRepository
    ): GetPaiementWithBeneficiairesUsecase {
        return GetPaiementWithBeneficiairesUsecase(repository = repository)
    }

    @Provides
    @Singleton
    fun provideGetPaiementWithBeneficiairesUseCaseById(
        repository: PaiementRepository
    ): GetPaiementWithBeneficiairesUsecaseById {
        return GetPaiementWithBeneficiairesUsecaseById(repository = repository)
    }

    @Provides
    @Singleton
    fun provideCheckNetwork(
        @ApplicationContext context: Context
    ): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    @Provides
    @Singleton
    fun provideAppDataBase(@ApplicationContext context: Context): AppDataBase {
        return Room.databaseBuilder(
            context,
            AppDataBase::class.java,
            Constant.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun providePaiementDao(appDataBase: AppDataBase): PaiementDao {
        return appDataBase.paiementDao()
    }

    @Provides
    @Singleton
    fun provideBeneficiaireDao(appDataBase: AppDataBase): BeneficiaireDao {
        return appDataBase.beneficiaireDao()
    }

    @Provides
    @Singleton
    fun providePaiementBeneficiaireDao(appDataBase: AppDataBase): PaiementBeneficiaireDao {
        return appDataBase.paiementBeneficiaireDao()
    }

    @Provides
    @Singleton
    fun provideTransactionInfoDao(appDataBase: AppDataBase): TransactionInfoDao {
        return appDataBase.transactionDao()
    }

    @Provides
    @Singleton
    fun provideRefundDao(appDataBase: AppDataBase): RefundDao {
        return appDataBase.refundDao()
    }

    @Provides
    @Singleton
    fun provideVirementDao(appDataBase: AppDataBase): VirementDao {
        return appDataBase.virementDao()
    }

//    @Provides
//    @Singleton
//    fun provideUSSDApi(): USSDApi {
//        return USSDController
//    }
//
//    // Keep only the interactor provider - remove the repository provider
//    @Provides
//    @Singleton
//    fun providePaiementCoursInteractorInterface(
//        @ApplicationContext context: Context
//    ): PaiementEnCoursFragmentInteractorInterface {
//        return PaiementEnCoursFragmentInteractor(context)
//    }
//
//    @Provides
//    @Singleton
//    fun ProvideViewModel(): CallViewModel{
//        return CallViewModel()
//    }

    // REMOVED: provideUssdRepository - this was causing the scope issue
}