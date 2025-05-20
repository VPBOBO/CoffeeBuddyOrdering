package com.example.coffeebuddyordering

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var coffeeTypeGroup: RadioGroup
    private lateinit var sizeGroup: RadioGroup
    private lateinit var milkSpinner: Spinner
    private lateinit var sugarSpinner: Spinner
    private lateinit var cbWhippedCream: CheckBox
    private lateinit var cbChocolate: CheckBox
    private lateinit var cbCaramel: CheckBox
    private lateinit var summaryText: TextView
    private lateinit var orderButton: Button
    private lateinit var btnIncrease: Button
    private lateinit var btnDecrease: Button
    private lateinit var tvQuantity: TextView
    private lateinit var tvTotalPrice: TextView

    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        coffeeTypeGroup = findViewById(R.id.coffeeTypeGroup)
        sizeGroup = findViewById(R.id.sizeGroup)
        milkSpinner = findViewById(R.id.milkSpinner)
        sugarSpinner = findViewById(R.id.sugarSpinner)
        cbWhippedCream = findViewById(R.id.cbWhippedCream)
        cbChocolate = findViewById(R.id.cbChocolate)
        cbCaramel = findViewById(R.id.cbCaramel)
        summaryText = findViewById(R.id.summary)
        orderButton = findViewById(R.id.btnOrder)
        btnIncrease = findViewById(R.id.btnIncrease)
        btnDecrease = findViewById(R.id.btnDecrease)
        tvQuantity = findViewById(R.id.tvQuantity)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)

        val milkOptions = listOf("None", "Whole Milk", "Skim Milk", "Soy Milk", "Almond Milk")
        val sugarOptions = listOf("None", "Less Sugar", "Regular Sugar", "Extra Sugar")

        milkSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, milkOptions)
        sugarSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sugarOptions)

        btnIncrease.setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
            updatePriceAndSummary()
        }

        btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
                updatePriceAndSummary()
            }
        }

        coffeeTypeGroup.setOnCheckedChangeListener { _, _ -> updatePriceAndSummary() }
        sizeGroup.setOnCheckedChangeListener { _, _ -> updatePriceAndSummary() }

        milkSpinner.onItemSelectedListener = spinnerListener
        sugarSpinner.onItemSelectedListener = spinnerListener

        cbWhippedCream.setOnCheckedChangeListener { _, _ -> updatePriceAndSummary() }
        cbChocolate.setOnCheckedChangeListener { _, _ -> updatePriceAndSummary() }
        cbCaramel.setOnCheckedChangeListener { _, _ -> updatePriceAndSummary() }

        orderButton.setOnClickListener { placeOrder() }

        tvQuantity.text = quantity.toString()
        updatePriceAndSummary()
    }

    private val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            updatePriceAndSummary()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    private fun getPriceDetails(): Triple<Int, String, String> {
        val selectedCoffeeId = coffeeTypeGroup.checkedRadioButtonId
        val selectedSizeId = sizeGroup.checkedRadioButtonId

        if (selectedCoffeeId == -1 || selectedSizeId == -1) {
            return Triple(0, "", "")
        }

        val coffeeType = findViewById<RadioButton>(selectedCoffeeId).text.toString()
        val size = findViewById<RadioButton>(selectedSizeId).text.toString()
        val milk = milkSpinner.selectedItem.toString()
        val sugar = sugarSpinner.selectedItem.toString()

        var basePrice = 100
        var coffeeExtra = 0
        var sizeExtra = 0
        var addOnTotal = 0

        coffeeExtra = when (coffeeType) {
            "Latte" -> 20
            "Mocha" -> 30
            else -> 0
        }

        sizeExtra = when (size) {
            "Medium" -> 10
            "Large" -> 20
            else -> 0
        }

        val addOns = mutableListOf<String>()
        if (cbWhippedCream.isChecked) {
            addOnTotal += 10
            addOns.add("Whipped Cream")
        }
        if (cbChocolate.isChecked) {
            addOnTotal += 15
            addOns.add("Chocolate")
        }
        if (cbCaramel.isChecked) {
            addOnTotal += 20
            addOns.add("Caramel")
        }

        val addOnText = if (addOns.isEmpty()) "None" else addOns.joinToString(", ")
        val totalPerCup = basePrice + coffeeExtra + sizeExtra + addOnTotal

        val breakdown = """
            Order Summary:
            -----------------------------
            Coffee: $coffeeType
            └ Base Price: ₱$basePrice
            Coffee Extra: ₱$coffeeExtra
            Size: $size
            └ Size Extra: ₱$sizeExtra
            Milk: $milk
            Sugar: $sugar
            Add-ons: $addOnText
            └ Add-ons Total: ₱$addOnTotal
            -----------------------------
            Price per Cup: ₱$totalPerCup
            Quantity: $quantity
            Total: ₱${totalPerCup * quantity}
        """.trimIndent()

        return Triple(totalPerCup * quantity, breakdown, addOnText)
    }

    private fun updatePriceAndSummary() {
        val (totalPrice, breakdown, _) = getPriceDetails()
        tvTotalPrice.text = "Total: ₱$totalPrice"
        summaryText.text = breakdown
    }

    private fun placeOrder() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter name and email.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
            return
        }

        val (totalPrice, breakdown, _) = getPriceDetails()

        if (totalPrice == 0) {
            Toast.makeText(this, "Please select coffee type and size.", Toast.LENGTH_SHORT).show()
            return
        }

        val emailSummary = """
            ☕ Coffee Buddy Order
            ------------------------------
            Name: $name
            Email: $email
            
            $breakdown
        """.trimIndent()

        summaryText.text = emailSummary

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Coffee Order for $name")
            putExtra(Intent.EXTRA_TEXT, emailSummary)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send order via email..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show()
        }
    }
}
