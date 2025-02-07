package thestockapp.injection

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import thestockapp.data.database.CompaniesDatabase
import thestockapp.data.database.CompaniesDatabaseInteractor
import thestockapp.data.network.ApiClient
import thestockapp.data.network.CompaniesNetworkInteractor
import thestockapp.data.network.NetworkAdapter
import thestockapp.data.repositories.CompaniesRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FeedModule {

    @Provides
    @Singleton
    fun providesApiClient(): ApiClient {
        return NetworkAdapter.apiClient()
    }

    @Provides
    fun providesCompaniesRepository(companiesNetworkInteractor: CompaniesNetworkInteractor,
                                    companiesDatabaseInteractor: CompaniesDatabaseInteractor
    ): CompaniesRepository {
        return CompaniesRepository(companiesNetworkInteractor, companiesDatabaseInteractor)
    }

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext application: Context): CompaniesDatabase {
        return Room.databaseBuilder(application, CompaniesDatabase::class.java, "main_database").build()
    }

    @Provides
    fun providesCompaniesDatabaseInteractor(companiesDatabase: CompaniesDatabase): CompaniesDatabaseInteractor {
        return CompaniesDatabaseInteractor(companiesDatabase)
    }

    @Provides
    fun providesCompaniesNetworkInteractor(apiClient: ApiClient): CompaniesNetworkInteractor {
        return CompaniesNetworkInteractor(apiClient)
    }
}