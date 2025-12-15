package com.subwatch.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.subwatch.R
import com.subwatch.data.SubWatchDatabase
import com.subwatch.data.Subscription
import com.subwatch.notify.NotifyScheduler
import com.subwatch.util.Dates
import coil.load
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditSubscriptionActivity : AppCompatActivity() {

    private var editingId: Long? = null
    private var startDate: LocalDate = Dates.today()
    private var endDate: LocalDate = Dates.today().plusMonths(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_subscription)

        val dao = SubWatchDatabase.get().subscriptionDao()

        val nameInput = findViewById<TextInputEditText>(R.id.nameInput)
        val domainInput = findViewById<TextInputEditText>(R.id.domainInput)
        val startInput = findViewById<TextInputEditText>(R.id.startDateInput)
        val endInput = findViewById<TextInputEditText>(R.id.endDateInput)
        val iconPreview = findViewById<ImageView>(R.id.iconPreview)
        val deleteBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.deleteBtn)

        fun refreshDates() {
            startInput.setText(Dates.format(startDate))
            endInput.setText(Dates.format(endDate))
        }
        refreshDates()

        // If editing existing
        val id = intent.getLongExtra(EXTRA_ID, -1L).takeIf { it > 0 }
        if (id != null) {
            editingId = id
            deleteBtn.visibility = View.VISIBLE
            lifecycleScope.launch {
                dao.getById(id)?.let { sub ->
                    nameInput.setText(sub.name)
                    domainInput.setText(sub.domain ?: "")
                    startDate = Dates.epochDayToLocalDate(sub.startDateEpochDay)
                    endDate = Dates.epochDayToLocalDate(sub.endDateEpochDay)
                    refreshDates()
                    refreshIconPreview(domainInput.text?.toString(), iconPreview)
                }
            }
            findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
                .setTitle(R.string.edit_subscription)
        }

        startInput.setOnClickListener {
            pickDate("Select start date", startDate) { picked ->
                startDate = picked
                if (endDate.isBefore(startDate)) endDate = startDate
                refreshDates()
            }
        }
        endInput.setOnClickListener {
            pickDate("Select end date", endDate) { picked ->
                endDate = picked
                if (endDate.isBefore(startDate)) startDate = endDate
                refreshDates()
            }
        }

        domainInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) refreshIconPreview(domainInput.text?.toString(), iconPreview)
        }
        domainInput.setOnKeyListener { _, _, _ ->
            refreshIconPreview(domainInput.text?.toString(), iconPreview)
            false
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.saveBtn).setOnClickListener {
            val name = nameInput.text?.toString()?.trim().orEmpty()
            val domain = domainInput.text?.toString()?.trim().orEmpty().ifBlank { null }

            if (name.isBlank()) {
                nameInput.error = "Required"
                return@setOnClickListener
            }

            val sub = Subscription(
                id = editingId ?: 0,
                name = name,
                domain = domain,
                startDateEpochDay = Dates.localDateToEpochDay(startDate),
                endDateEpochDay = Dates.localDateToEpochDay(endDate)
            )

            lifecycleScope.launch {
                dao.upsert(sub)
                NotifyScheduler.scheduleDaily(this@EditSubscriptionActivity)
                finish()
            }
        }

        deleteBtn.setOnClickListener {
            val currentId = editingId ?: return@setOnClickListener
            lifecycleScope.launch {
                dao.getById(currentId)?.let { dao.delete(it) }
                NotifyScheduler.scheduleDaily(this@EditSubscriptionActivity)
                finish()
            }
        }
    }

    private fun refreshIconPreview(domainText: String?, preview: ImageView) {
        val domain = domainText?.trim()
            ?.removePrefix("http://")?.removePrefix("https://")
            ?.removePrefix("www.")
        if (!domain.isNullOrBlank()) {
            val url = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
            preview.load(url) {
                crossfade(true)
                placeholder(android.R.drawable.sym_def_app_icon)
                error(android.R.drawable.sym_def_app_icon)
            }
        } else {
            preview.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }

    private fun pickDate(title: String, initial: LocalDate, onPicked: (LocalDate) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(initial.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            onPicked(Dates.millisToLocalDate(millis))
        }
        picker.show(supportFragmentManager, "datePicker")
    }

    companion object {
        const val EXTRA_ID = "id"
    }
}
