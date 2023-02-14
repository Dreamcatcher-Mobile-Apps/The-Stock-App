package thestockapp.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import thestockapp.data.database.CompaniesDatabaseInteractor
import thestockapp.data.database.CompanyDatabaseEntity
import thestockapp.data.network.CompaniesNetworkInteractor
import thestockapp.data.network.QuarterIncomeStatementGsonModel
import thestockapp.data.network.SharePriceGsonModel
import thestockapp.data.network.SharesFloatGsonModel
import thestockapp.data.utils.CurrencyExchange
import thestockapp.data.utils.DataFetchingCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

// Data Repository - the main gate of the model (data) part of the application
class CompaniesRepository @Inject constructor(private val networkInteractor: CompaniesNetworkInteractor,
                                              private val databaseInteractor: CompaniesDatabaseInteractor
) {

    fun getAllCompanies(callback: DataFetchingCallback): LiveData<List<CompanyDatabaseEntity>>? {
        updateAllCurrentlyStoredCompanies(callback)
        return databaseInteractor.getAllCompanies_liveData()
    }

    private fun updateAllCurrentlyStoredCompanies(callback: DataFetchingCallback) {
        GlobalScope.launch(Dispatchers.IO) {
            databaseInteractor.getAllCompanies()?.forEach {
                addNewCompany(it.ticker, callback)
            }
        }
    }

    fun addNewCompany(ticker: String, callback: DataFetchingCallback) {
        val formattedTicker = ticker.uppercase()

        // First API call
        networkInteractor.getIncomeStatementData(formattedTicker).enqueue(object: Callback<List<QuarterIncomeStatementGsonModel>> {

            override fun onResponse(call: Call<List<QuarterIncomeStatementGsonModel>>?, response: Response<List<QuarterIncomeStatementGsonModel>>?) {

                val incomeStatementResponse = response?.body()


                // Second API call
                networkInteractor.getFloatSharesData(formattedTicker).enqueue(object: Callback<List<SharesFloatGsonModel>> {

                    override fun onResponse(call: Call<List<SharesFloatGsonModel>>?, response: Response<List<SharesFloatGsonModel>>?) {

                        val floatSharesResponse = response?.body()


                        // Third API call
                        networkInteractor.getSharePriceData(formattedTicker).enqueue(object: Callback<List<SharePriceGsonModel>> {

                            override fun onResponse(call: Call<List<SharePriceGsonModel>>?, response: Response<List<SharePriceGsonModel>>?) {

                                val sharePriceResponse = response?.body()

                                if (incomeStatementResponse != null && floatSharesResponse != null && sharePriceResponse != null) {
                                    if (incomeStatementResponse.size == 2 && floatSharesResponse.isNotEmpty()) {
                                        Log.d("DATA FETCHING", "Data fetched successfully")

                                        val currency = incomeStatementResponse[0].reportedCurrency
                                        val fillingDate = incomeStatementResponse[0].fillingDate
                                        val previousQuarter_GrossProfit = CurrencyExchange.applyCurrencyExchange(currency ,incomeStatementResponse[1].grossProfit)
                                        val previousQuarter_NetIncome = CurrencyExchange.applyCurrencyExchange(currency ,incomeStatementResponse[1].netIncome)
                                        val recentQuarter_GrossProfit = CurrencyExchange.applyCurrencyExchange(currency ,incomeStatementResponse[0].grossProfit)
                                        val recentQuarter_NetIncome = CurrencyExchange.applyCurrencyExchange(currency ,incomeStatementResponse[0].netIncome)
                                        val eps = CurrencyExchange.applyCurrencyExchange(currency ,incomeStatementResponse[0].eps)
                                        val today_OutstandingShares = floatSharesResponse[0].outstandingShares
                                        val today_SharePrice = sharePriceResponse[0].sharePrice

                                        Log.d("DATA CONTROL", "ticker: " + formattedTicker)
                                        Log.d("DATA CONTROL", "fillingDate: " + fillingDate)
                                        Log.d("DATA CONTROL", "previousQuarter_GrossProfit: " + previousQuarter_GrossProfit.toString())
                                        Log.d("DATA CONTROL", "previousQuarter_NetIncome: " + previousQuarter_NetIncome.toString())
                                        Log.d("DATA CONTROL", "recentQuarter_GrossProfit: " + recentQuarter_GrossProfit.toString())
                                        Log.d("DATA CONTROL", "recentQuarter_NetIncome: " + recentQuarter_NetIncome.toString())
                                        Log.d("DATA CONTROL", "today_OutstandingShares: " + today_OutstandingShares.toString())
                                        Log.d("DATA CONTROL", "today_SharePrice: " + today_SharePrice.toString())

                                        val newCompany = CompanyDatabaseEntity(
                                                ticker = ticker,
                                                incomeStatementDate = fillingDate,
                                                previousQuarter_GrossProfit = previousQuarter_GrossProfit,
                                                previousQuarter_NetIncome = previousQuarter_NetIncome,
                                                recentQuarter_GrossProfit = recentQuarter_GrossProfit,
                                                recentQuarter_NetIncome = recentQuarter_NetIncome,
                                                eps = eps,
                                                today_OutstandingShares = today_OutstandingShares,
                                                today_SharePrice = today_SharePrice
                                        )
                                        databaseInteractor.addNewCompany(newCompany)
                                    } else {
                                        val message = "incomeStatementResponse size less than 2 or floatSharesResponse is empty"
                                        callback.fetchingError(ticker, message)
                                        Log.e("DATA FETCHING", "Data fetching error - data incomplete")
                                    }
                                } else {
                                    val message = "lack of incomeStatementResponse or floatSharesResponse or sharePriceResponse"
                                    callback.fetchingError(ticker, message)
                                    Log.e("DATA FETCHING", "Data fetching error - data incomplete")
                                }

                            }

                            override fun onFailure(call: Call<List<SharePriceGsonModel>>?, t: Throwable?) {
                                callback.fetchingError(ticker, t?.message)
                                logError(3, t)
                            }
                        })
                    }

                    override fun onFailure(call: Call<List<SharesFloatGsonModel>>?, t: Throwable?) {
                        callback.fetchingError(ticker, t?.message)
                        logError(2, t)
                    }
                })
            }

            override fun onFailure(call: Call<List<QuarterIncomeStatementGsonModel>>?, t: Throwable?) {
                callback.fetchingError(ticker, t?.message)
                logError(1, t)
            }
        })
    }

    private fun logError(callNumber: Int, throwable: Throwable?) {
        Log.e("DATA FETCHING", "Data fetching error - call $callNumber")
        throwable?.message?.let {
            Log.e("DATA FETCHING", ("Data fetching error - call $callNumber error message: " + it))
        }
    }

    fun removeCompany(ticker: String) {
        databaseInteractor.removeCompany(ticker)
    }
}