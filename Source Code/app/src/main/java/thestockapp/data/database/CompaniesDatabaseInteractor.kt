package thestockapp.data.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Interactor used for communication with the internal database
class CompaniesDatabaseInteractor(private val companiesDatabase: CompaniesDatabase) {

    fun getAllCompanies_liveData(): LiveData<List<CompanyDatabaseEntity>>? {
        return companiesDatabase.getCompaniesDao().getAllSavedCompanies_liveData()
    }

    fun getAllCompanies(): List<CompanyDatabaseEntity>? {
        return companiesDatabase.getCompaniesDao().getAllSavedCompanies()
    }

    fun addNewCompany(company: CompanyDatabaseEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            companiesDatabase.getCompaniesDao().insertNewCompany(company)
        }
    }

    fun removeCompany(ticker: String) {
        GlobalScope.launch(Dispatchers.IO) {
            companiesDatabase.getCompaniesDao().removeCompany(ticker)
        }
    }
}



