package thestockapp.features.feed

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    fun subscribeForFakePosts(): LiveData<List<CompanyDatabaseEntity>>? {
        val fakeCompany01 = CompanyDatabaseEntity("XYZ1", 20000.0, 10000.0, 25000.0, 15000.0, 150.0, 10000, 25.0, "20/04/2022")
        val fakeCompany02 = CompanyDatabaseEntity("XYZ2", 30000.0, 30000.0, 25000.0, 12000.0, 50.0, 8000, 225.0, "20/12/2022")
        val fakeCompaniesList = listOf(fakeCompany01, fakeCompany02)
        val liveData = MutableLiveData<List<CompanyDatabaseEntity>>()
        liveData.postValue(fakeCompaniesList)
        return liveData
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

    fun calculateAverageValue_positiveOnly(
        companies: List<CompanyDatabaseEntity>,
        sortingOption: SortingOption
    ): Float {
        val positiveValues = companies.filter {
            val value = when (sortingOption) {
                SortingOption.GROSS_PROFIT_LAST_PERIOD -> it.getGrossProfitInRecentQuarterInCentPer1DollarSpentOnThemToday()
                SortingOption.NET_INCOME_LAST_PERIOD -> it.getNetIncomeInRecentQuarterInCentPer1DollarSpentOnThemToday()
                SortingOption.EPS_PER_1_DOLLAR_SPENT -> it.getEarningsPerSharePer1DollarSpentOnThemToday()
            }
            value != null && value > 0.0
        }
        return calculateAverageValue(positiveValues, sortingOption)
    }

    fun calculateAverageValue(
        companies: List<CompanyDatabaseEntity>,
        sortingOption: SortingOption
    ): Float {
        var valuesAmount = 0
        var sumPerDollar = 0.0
        companies.forEach {
            val value = when (sortingOption) {
                SortingOption.GROSS_PROFIT_LAST_PERIOD -> it.getGrossProfitInRecentQuarterInCentPer1DollarSpentOnThemToday()
                SortingOption.NET_INCOME_LAST_PERIOD -> it.getNetIncomeInRecentQuarterInCentPer1DollarSpentOnThemToday()
                SortingOption.EPS_PER_1_DOLLAR_SPENT -> it.getEarningsPerSharePer1DollarSpentOnThemToday()
            }
            value?.let {
                valuesAmount++
                sumPerDollar += it
            }
        }
        return (sumPerDollar / valuesAmount).toFloat()
    }
}