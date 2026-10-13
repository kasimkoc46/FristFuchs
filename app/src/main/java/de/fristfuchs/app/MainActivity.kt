
package de.fristfuchs.app

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Deadline(
    val id: Long,
    val title: String,
    val category: String,
    val date: Long
)

class MainActivity : Activity() {
    private val deadlines = mutableListOf<Deadline>()
    private val prefs by lazy { getSharedPreferences("fristfuchs", MODE_PRIVATE) }
    private val format = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
    private var chosenDate = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 30)
    }
    private lateinit var list: LinearLayout
    private lateinit var count: TextView
    private lateinit var dateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(15, 23, 42)
        window.navigationBarColor = Color.rgb(15, 23, 42)
        loadDeadlines()
        createScreen()
    }

    private fun createScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 12)
            setBackgroundColor(Color.rgb(245, 247, 251))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(label("🦊 FristFuchs", 28, true))
        content.addView(label("Verträge und wichtige Fristen im Blick.", 15))

        val name = EditText(this).apply {
            hint = "z. B. Handyvertrag"
            textSize = 16f
        }
        content.addView(name, params())

        val category = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Vertrag", "Kündigung", "Garantie",
                    "Versicherung", "Sonstiges")
            )
        }
        content.addView(category, params())

        dateButton = Button(this).apply {
            text = "Frist: ${format.format(chosenDate.time)}"
            setOnClickListener { chooseDate() }
        }
        content.addView(dateButton, params())

        val add = Button(this).apply {
            text = "Frist speichern"
            setOnClickListener {
                val title = name.text.toString().trim()
                if (title.isBlank()) {
                    name.error = "Bitte einen Namen eingeben"
                    return@setOnClickListener
                }
                deadlines.add(
                    Deadline(
                        System.currentTimeMillis(),
                        title,
                        category.selectedItem.toString(),
                        chosenDate.timeInMillis
                    )
                )
                saveDeadlines()
                name.text.clear()
                render()
                Toast.makeText(
                    this@MainActivity,
                    "Frist gespeichert",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        content.addView(add, params())

        count = label("Deine Fristen", 20, true)
        content.addView(count)

        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(list)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        render()
    }

    private fun chooseDate() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                chosenDate.set(year, month, day, 12, 0, 0)
                dateButton.text = "Frist: ${format.format(chosenDate.time)}"
            },
            chosenDate.get(Calendar.YEAR),
            chosenDate.get(Calendar.MONTH),
            chosenDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun render() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val sorted = deadlines.sortedBy { it.date }
        count.text = "Deine Fristen (${sorted.size})"

        sorted.forEach { item ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 12, 16, 12)
                setBackgroundColor(Color.WHITE)
            }
            card.addView(label(item.title, 18, true))
            card.addView(label(
                "${item.category} • ${format.format(Date(item.date))}", 14
            ))
            val delete = Button(this).apply {
                text = "Löschen"
                setOnClickListener {
                    deadlines.removeAll { it.id == item.id }
                    saveDeadlines()
                    render()
                }
            }
            card.addView(delete)
            val p = params()
            p.bottomMargin = 12
            list.addView(card, p)
        }
    }

    private fun loadDeadlines() {
        try {
            val array = JSONArray(prefs.getString("deadlines", "[]"))
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                deadlines.add(
                    Deadline(
                        obj.getLong("id"),
                        obj.getString("title"),
                        obj.getString("category"),
                        obj.getLong("date")
                    )
                )
            }
        } catch (_: Exception) {
            deadlines.clear()
        }
    }

    private fun saveDeadlines() {
        val array = JSONArray()
        deadlines.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("category", it.category)
                    .put("date", it.date)
            )
        }
        prefs.edit().putString("deadlines", array.toString()).apply()
    }

    private fun label(
        value: String,
        size: Int,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(Color.rgb(15, 23, 42))
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
        setPadding(0, 8, 0, 8)
    }

    private fun params() =
        LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = 6
        }
}
