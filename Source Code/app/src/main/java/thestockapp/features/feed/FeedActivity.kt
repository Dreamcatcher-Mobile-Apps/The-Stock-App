package thestockapp.features.feed

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.loading_badge.*
import thestockapp.R
import thestockapp.data.database.CompanyDatabaseEntity
import thestockapp.data.utils.DataFetchingCallback


@AndroidEntryPoint
class FeedActivity : AppCompatActivity(), DataFetchingCallback {

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var companiesListAdapter: CompaniesListAdapter
    private var isLoadingMoreItems: Boolean = false

    private val STATE_LOADING_ERROR = "STATE_LOADING_ERROR"
    private val STATE_CONTENT_LOADED = "STATE_CONTENT_LOADED"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView (feed items)
        setupRecyclerView()

        // Initialize sorting spinner
        setupSortingOptions()

        // Initialize adding company button
        setupAddCompanyButton()

        // Fetch feed items from the back-end and load them into the view
        subscribeForFeedItems()

        // Catch and handle potential update (e.g. network) issues
        subscribeForUpdateError()
    }

    private fun setupSortingOptions() {
        val optionsList = SortingOption.values().map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, optionsList)
        sorting_spinner.adapter = adapter
        sorting_spinner.onItemSelectedListener = (object : OnItemSelectedListener {

            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {

                // Todo: Refactor into using arguments, not seletedItem
                val sortingOption = SortingOption.valueOf(sorting_spinner.selectedItem as String)
                companiesListAdapter.sortItems(sortingOption)

                // Todo: Not good solution when we fetch data back from the adapter - refactor in the future.
                updateAverageValues(companiesListAdapter.getCurrentlyDisplayedItems(), sortingOption)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        })
    }

    private fun setupAddCompanyButton() {
        val activity = this
        add_company_button.setOnClickListener {
            val ticker = add_company_input.text.toString()
            viewModel.addCompany(ticker, activity)
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(add_company_input.windowToken, 0)
    }

    private fun updateAverageValues(
        values: List<CompanyDatabaseEntity>,
        sortingOption: SortingOption
    ) {
        val averageValue = viewModel.calculateAverageValue(values, sortingOption)
        val averageValuePositiveOnly = viewModel.calculateAverageValue_positiveOnly(values, sortingOption)
        val formatter = "%.4f"
        val averagePrefix: Int
        val averagePositiveOnlyPrefix: Int
        when (sortingOption) {
            SortingOption.GROSS_PROFIT_LAST_PERIOD -> {
                averagePrefix = R.string.average_gross_profit_per_dollar
                averagePositiveOnlyPrefix = R.string.average_positive_gross_profit_per_dollar
            }
            SortingOption.NET_INCOME_LAST_PERIOD -> {
                averagePrefix = R.string.average_net_income_per_dollar
                averagePositiveOnlyPrefix = R.string.average_positive_net_income_per_dollar
            }
            SortingOption.EPS_PER_1_DOLLAR_SPENT -> {
                averagePrefix = R.string.average_eps_per_dollar
                averagePositiveOnlyPrefix = R.string.average_positive_eps_per_dollar
            }
        }
        average.text = getString(averagePrefix, String.format(formatter, averageValue))
        average_positive.text = getString(averagePositiveOnlyPrefix, String.format(formatter, averageValuePositiveOnly))
    }

    private fun setupRecyclerView() {
        companiesListAdapter = CompaniesListAdapter(this)
        main_feed_recyclerview.layoutManager = LinearLayoutManager(this)
        main_feed_recyclerview.adapter = companiesListAdapter

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                    val ticker = companiesListAdapter.getCompanyIdByPosition(viewHolder.adapterPosition)
                    viewModel.removeCompany(ticker)
                    companiesListAdapter.itemRemoved(viewHolder.adapterPosition)
                }
            }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(main_feed_recyclerview)
    }

    private fun subscribeForFeedItems() {
//        viewModel.subscribeForPosts()?.observe(this) {
        viewModel.subscribeForFakePosts()?.observe(this) {
            setViewState(STATE_CONTENT_LOADED, it)
        }
    }

    private fun subscribeForUpdateError() {
        viewModel.subscribeForUpdateErrors()?.observe(this) {

            // Case of Network Error if no items have been cached
            if (companiesListAdapter.itemCount == 0) {
                setViewState(STATE_LOADING_ERROR, null)
            }

            // Display error message to the user
            Toast.makeText(this, R.string.network_problem_check_internet_connection, Toast.LENGTH_LONG).show()

            isLoadingMoreItems = false
        }
    }

    private fun setViewState(state: String, items: List<CompanyDatabaseEntity>?) {
        when (state) {
            STATE_LOADING_ERROR -> setupLoadingErrorView()
            STATE_CONTENT_LOADED -> setupContentLoadedView(items!!)
        }
    }

    private fun setupLoadingErrorView() {
        // Stop the loading progress bar (circle)
        progressBar.visibility = View.INVISIBLE

        // Display "Try Again" button
        tryagain_button.visibility = View.VISIBLE

        // Setup onClick listener that resets the feed data subscription
        tryagain_button.setOnClickListener {
            // Todo. Fix.
            //refreshPostsSubscription()

            // Re-display the loading progress bar (circle)
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun setupContentLoadedView(items: List<CompanyDatabaseEntity>) {
        // Hide the loading view
        loading_container.visibility = View.GONE
        appbar_container.visibility = View.VISIBLE

        // If there are no items in the DB, then upload the default set.
        // But not when we use fake dataset - therefore commented out.
        //if (items.isEmpty()) viewModel.loadDefaultCompaniesSet(this)

        // Display fetched items
        val sortingOption = SortingOption.valueOf(sorting_spinner.selectedItem as String)
        companiesListAdapter.setItems(items, sortingOption)

        updateAverageValues(items, sortingOption)
    }

    override fun fetchingError(ticker: String, errorMessage: String?) {
        displayErrorDialog(ticker, errorMessage)
    }

    private fun displayErrorDialog(ticker: String, errorMessage: String?) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.dialogTitle, ticker))
            builder.setMessage(errorMessage ?: getString(R.string.dialogMessage))
            val alertDialog = builder.create()
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
    }
}