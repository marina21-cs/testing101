package com.codestudio.ide.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codestudio.ide.R
import com.codestudio.ide.model.SearchOptions
import com.codestudio.ide.model.SearchResult
import com.codestudio.ide.ui.adapter.SearchResultAdapter
import com.codestudio.ide.utils.SearchManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchDialog(
    context: Context,
    private val projectPath: String,
    private val onResultClick: (SearchResult) -> Unit
) : Dialog(context) {

    private val searchManager = SearchManager()
    private lateinit var resultAdapter: SearchResultAdapter
    private var searchJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_search)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val caseSensitiveCheck = findViewById<CheckBox>(R.id.caseSensitiveCheck)
        val wholeWordCheck = findViewById<CheckBox>(R.id.wholeWordCheck)
        val regexCheck = findViewById<CheckBox>(R.id.regexCheck)
        val resultsRecyclerView = findViewById<RecyclerView>(R.id.searchResults)
        val closeButton = findViewById<Button>(R.id.closeButton)

        resultAdapter = SearchResultAdapter { result ->
            onResultClick(result)
            dismiss()
        }

        resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = resultAdapter
        }

        // Debounced search
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = coroutineScope.launch {
                    delay(300) // Debounce
                    performSearch(
                        query = s?.toString() ?: "",
                        caseSensitive = caseSensitiveCheck.isChecked,
                        wholeWord = wholeWordCheck.isChecked,
                        useRegex = regexCheck.isChecked
                    )
                }
            }
        })

        // Update search on option change
        val optionListener = { _: android.widget.CompoundButton, _: Boolean ->
            val query = searchInput.text.toString()
            if (query.isNotBlank()) {
                searchJob?.cancel()
                searchJob = coroutineScope.launch {
                    performSearch(
                        query = query,
                        caseSensitive = caseSensitiveCheck.isChecked,
                        wholeWord = wholeWordCheck.isChecked,
                        useRegex = regexCheck.isChecked
                    )
                }
            }
        }

        caseSensitiveCheck.setOnCheckedChangeListener(optionListener)
        wholeWordCheck.setOnCheckedChangeListener(optionListener)
        regexCheck.setOnCheckedChangeListener(optionListener)

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private suspend fun performSearch(
        query: String,
        caseSensitive: Boolean,
        wholeWord: Boolean,
        useRegex: Boolean
    ) {
        if (query.isBlank()) {
            resultAdapter.submitList(emptyList())
            return
        }

        val results = withContext(Dispatchers.IO) {
            searchManager.searchInFiles(
                projectPath,
                SearchOptions(
                    query = query,
                    caseSensitive = caseSensitive,
                    wholeWord = wholeWord,
                    useRegex = useRegex
                )
            )
        }

        resultAdapter.submitList(results.take(100)) // Limit results
    }

    override fun dismiss() {
        searchJob?.cancel()
        super.dismiss()
    }
}
