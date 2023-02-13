package thestockapp.features.feed

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import thestockapp.data.database.CompanyDatabaseEntity
import thestockapp.data.repositories.CompaniesRepository
import thestockapp.data.utils.DataFetchingCallback
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(private val companiesRepository: CompaniesRepository)
    : ViewModel(), LifecycleObserver {

    fun subscribeForPosts(): LiveData<List<CompanyDatabaseEntity>>? {
        return companiesRepository.getAllCompanies()
    }

    fun addCompany(ticker: String, callback: DataFetchingCallback) {
        companiesRepository.addNewCompany(ticker, callback)
    }

    fun removeCompany(ticker: String) {
        companiesRepository.removeCompany(ticker)
    }

    fun subscribeForUpdateErrors(): LiveData<Boolean>? {
        return companiesRepository.subscribeForUpdateErrors()
    }

    fun loadDefaultCompaniesSet(callback: DataFetchingCallback) {
        val defaultCompanies = arrayListOf<String>()

        // Error: incomeStatementResponse size less than 2 or floatSharesResponse is empty
        //defaultCompanies.add("IMPX")

        defaultCompanies.add("AAPL")
        defaultCompanies.add("AMZN")
        defaultCompanies.add("FB")
        defaultCompanies.add("GOOGL")
        defaultCompanies.add("IBM")
        defaultCompanies.add("MSFT")
        defaultCompanies.add("NFLX")
        defaultCompanies.add("NVDA")
        defaultCompanies.add("PYPL")
        defaultCompanies.add("VZ")

//        defaultCompanies.add("ABBV")
//        defaultCompanies.add("ALPP")
//        defaultCompanies.add("AZN")
//        defaultCompanies.add("BABA")
//        defaultCompanies.add("BP")
//        defaultCompanies.add("COIN")
//        defaultCompanies.add("CXW")
//        defaultCompanies.add("FDX")
//        defaultCompanies.add("FNF")
//        defaultCompanies.add("GD")
//        defaultCompanies.add("HMC")
//        defaultCompanies.add("MRNA")
//        defaultCompanies.add("NIO")
//        defaultCompanies.add("PLTR")
//        defaultCompanies.add("PLUG")
//        defaultCompanies.add("PRU")
//        defaultCompanies.add("PTON")
//        defaultCompanies.add("RIVN")
//        defaultCompanies.add("SQ")
//        defaultCompanies.add("TM")
//        defaultCompanies.add("TSLA")
//        defaultCompanies.add("XPEV")

        defaultCompanies.forEach {
            addCompany(it, callback)
        }
    }
}