import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import com.venus_customer.R
import java.text.DateFormatSymbols
import java.util.*

class CustomDatePickerDialog(
    val selectedDay: Int,
    val selectedMonth: Int,
    val selectedYear: Int
) : DialogFragment() {

    interface OnDateSelectedListener {
        fun onDateSelected(date: Calendar)
    }

    var listener: OnDateSelectedListener? = null

    private lateinit var dayPicker: NumberPicker
    private lateinit var years: Array<String>
    var minAllowedYear = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.custom_date_picker, container, false)
        dayPicker = view.findViewById<NumberPicker>(R.id.dayPicker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Set up day picker
        dayPicker.minValue = 1
        dayPicker.maxValue = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        dayPicker.value = if (selectedDay != 0) selectedDay else currentDay


        // Set up month picker
        val months = DateFormatSymbols().months
        monthPicker.minValue = 0
        monthPicker.maxValue =
            months.size - 1  // -1 because DateFormatSymbols().months includes an empty string at the end
        monthPicker.displayedValues =
            months.sliceArray(months.indices) // Remove the empty string
        monthPicker.value = if (selectedMonth != 0) selectedMonth - 1 else currentMonth

        // Set up year picker
        minAllowedYear = currentYear - 18
        years = (1900..currentYear).map { it.toString() }.toTypedArray()
        yearPicker.minValue = 0
        yearPicker.maxValue = years.size - 1
        yearPicker.displayedValues = years
        yearPicker.value =
            years.indexOf((if (selectedYear != 0) selectedYear else minAllowedYear).toString())

        view.findViewById<TextView>(R.id.tvOk).setOnClickListener {
            val selectedDay = dayPicker.value
            val selectedMonth = monthPicker.value + 1 // Adjust month index (0-based to 1-based)
            val selectedYear = years[yearPicker.value].toInt()
//            listener?.onDateSelected(
//                selectedDay, selectedMonth, selectedYear
//            )
            dismiss()
        }

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dayPicker = view.findViewById<NumberPicker>(R.id.dayPicker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)

        // Example listener for day picker
        dayPicker.setOnValueChangedListener { _, _, newVal ->
            // Handle day selection
        }

        // Example listener for month picker
        monthPicker.setOnValueChangedListener { _, _, newVal ->
            // Handle month selection
            val selectedMonth = newVal + 1 // Adjust month index (0-based to 1-based)
            val selectedYear = years[yearPicker.value].toInt()
            updateDaysInDayPicker(selectedMonth, selectedYear)
        }

        // Example listener for year picker
        yearPicker.setOnValueChangedListener { _, _, newVal ->
            // Handle year selection
            val selectedYear = years[newVal].toInt()
            if (selectedYear > minAllowedYear) {
                // If selected year is less than 18 years in the past, reset to the minimum allowed year
                yearPicker.value = years.indexOf((minAllowedYear).toString())
            }
            val selectedMonth = monthPicker.value + 1 // Adjust month index (0-based to 1-based)
//            val selectedYear = years[newVal].toInt()
            updateDaysInDayPicker(selectedMonth, selectedYear)

        }
        yearPicker.setOnScrollListener { view, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                // Get the currently selected year
                val selectedYear = years[yearPicker.value].toInt()
                // Check if the selected year is greater than the minimum allowed year
                if (selectedYear > minAllowedYear) {
                    // Set the year picker value to the minimum allowed year
                    Log.d("MINYEAR", "$minAllowedYear")
                    yearPicker.value = years.indexOf(minAllowedYear.toString())

                } else
                    yearPicker.value = years.indexOf(selectedYear.toString())

            }
        }
    }
    private fun updateDaysInDayPicker(month: Int, year: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        dayPicker.maxValue = maxDay
    }
}
