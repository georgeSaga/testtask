package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.utils.data.Attendee
import com.example.myapplication.utils.data.Result
import com.example.myapplication.utils.getJsonDataFromAsset
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAttendees()
    }

    private fun checkAttendees() {
        val jsonFileString = this.getJsonDataFromAsset("testNew.json")

        val gson = GsonBuilder().setPrettyPrinting().create()
        val listAttendeeType = object : TypeToken<List<Attendee>>() {}.type

        val attendees: List<Attendee> = gson.fromJson(jsonFileString, listAttendeeType)

        try {
            attendees.forEach {
                it.availableDates = it.availableDates.distinct().sortedWith(StringDateComparator())
                it.availableDates = it.availableDates.filterIndexed { index, s ->
                    if (it.availableDates.size > index + 1) checkDatesContract(
                        s,
                        it.availableDates[index + 1]
                    ) else false
                }
            }

            val result: MutableList<Result> = mutableListOf()
            attendees.groupBy { it.country }.forEach { countryGroup ->
                val countedDate =
                    countryGroup.value.flatMap { it.availableDates }.groupingBy { it }.eachCount()
                        .toSortedMap(StringDateComparator())
                        .maxBy { it.value }?.key

                countedDate?.let {
                    val filtered =
                        countryGroup.value.filter { it.availableDates.contains(countedDate) }
                    result.add(Result(countryGroup.key, countedDate, filtered.map { it.email }))
                }
            }

            val resultJson = gson.toJson(result)

            println(resultJson)
            text.text = resultJson

            //In real project we have to ask permission here, but now I don't have time for it :)
            try {
                val myFile = File("/sdcard/resultjson.txt")
                myFile.createNewFile()
                val fOut = FileOutputStream(myFile)
                val myOutWriter = OutputStreamWriter(fOut)
                myOutWriter.append(resultJson)
                myOutWriter.close()
                fOut.close()
            } catch (e: IOException) {
            }

        } catch (e: ParseException) {
            println("oops!")
        }
    }

    @Throws(ParseException::class)
    fun checkDatesContract(firstDate: String, secondDate: String) : Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date1 = dateFormat.parse(firstDate)
        val date2 = dateFormat.parse(secondDate)
        val calendar1 = Calendar.getInstance()
        calendar1.time = date1
        val calendar2 = Calendar.getInstance()
        calendar2.time = date2
        return (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR)+1 == calendar2.get(Calendar.DAY_OF_YEAR))
    }

    internal class StringDateComparator : Comparator<String> {
        private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        override fun compare(lhs: String, rhs: String): Int {
            return dateFormat.parse(lhs)?.compareTo(dateFormat.parse(rhs)) ?: 0
        }
    }

}
