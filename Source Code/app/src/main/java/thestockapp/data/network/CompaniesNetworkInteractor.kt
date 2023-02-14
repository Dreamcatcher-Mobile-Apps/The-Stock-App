package thestockapp.data.network

import retrofit2.Call
import javax.inject.Inject

class CompaniesNetworkInteractor @Inject constructor(var apiClient: ApiClient) {

    private val PERIOD_QUARTER = "quarter";
    private val PERIOD_ANNUAL = "annual";

    fun getIncomeStatementData(ticker: String): Call<List<QuarterIncomeStatementGsonModel>> {
        return apiClient.getIncomeStatementData(ticker, PERIOD_QUARTER, 2, apiKey = NetworkConstants.API_KEY)
    }

    fun getFloatSharesData(ticker: String): Call<List<SharesFloatGsonModel>> {
        return apiClient.getFloatSharesData(ticker, apiKey = NetworkConstants.API_KEY)
    }

    fun getSharePriceData(ticker: String): Call<List<SharePriceGsonModel>> {
        return apiClient.getSharePriceData(ticker, apiKey = NetworkConstants.API_KEY)
    }
}