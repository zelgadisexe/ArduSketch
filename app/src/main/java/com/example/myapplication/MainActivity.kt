package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private val boards = listOf(
        Device("Arduino Uno R3", listOf(
            Pin("NC"), Pin("SCL"), Pin("IOREF"), Pin("SDA"), Pin("RESET"), Pin("AREF"), Pin("3.3V"), Pin("GND"),
            Pin("5V"), Pin("D13"), Pin("GND"), Pin("D12"), Pin("GND"), Pin("D11"), Pin("Vin"), Pin("D10"),
            Pin("A0"), Pin("D9"), Pin("A1"), Pin("D8"), Pin("A2"), Pin("D7"), Pin("A3"), Pin("D6"),
            Pin("A4"), Pin("D5"), Pin("A5"), Pin("D4"), Pin("SDA*"), Pin("D3"), Pin("SCL*"), Pin("D2"),
            Pin("RX"), Pin("D0"), Pin("TX"), Pin("D1")
        ), DeviceType.BOARD, 320f, 600f),
        Device("Arduino Nano", listOf(
            Pin("D1/TX"), Pin("VIN"), Pin("D0/RX"), Pin("GND"), Pin("RESET"), Pin("RESET"), Pin("GND"), Pin("5V"),
            Pin("D2"), Pin("A7"), Pin("D3"), Pin("A6"), Pin("D4"), Pin("A5"), Pin("D5"), Pin("A4"),
            Pin("D6"), Pin("A3"), Pin("D7"), Pin("A2"), Pin("D8"), Pin("A1"), Pin("D9"), Pin("A0"),
            Pin("D10"), Pin("REF"), Pin("D11"), Pin("3V3"), Pin("D12"), Pin("D13")
        ), DeviceType.BOARD, 260f, 500f),
        Device("Arduino Leonardo", listOf(
            Pin("NC"), Pin("SCL"), Pin("IOREF"), Pin("SDA"), Pin("RESET"), Pin("AREF"), Pin("3.3V"), Pin("GND"),
            Pin("5V"), Pin("D13"), Pin("GND"), Pin("D12"), Pin("GND"), Pin("D11"), Pin("Vin"), Pin("D10"),
            Pin("A0"), Pin("D9"), Pin("A1"), Pin("D8"), Pin("A2"), Pin("D7"), Pin("A3"), Pin("D6"),
            Pin("A4"), Pin("D5"), Pin("A5"), Pin("D4"), Pin("SDA*"), Pin("D3"), Pin("SCL*"), Pin("D2"),
            Pin("RX"), Pin("D0"), Pin("TX"), Pin("D1")
        ), DeviceType.BOARD, 320f, 600f),
        Device("Orange Pi 40-Pin", listOf(
            Pin("3.3V"), Pin("5V"), Pin("I2C_SDA"), Pin("5V"), Pin("I2C_SCL"), Pin("GND"), Pin("GPIO7"), Pin("UART_TX"),
            Pin("GND"), Pin("UART_RX"), Pin("GPIO0"), Pin("GPIO1"), Pin("GPIO2"), Pin("GND"), Pin("GPIO3"), Pin("GPIO4"),
            Pin("3.3V"), Pin("GPIO5"), Pin("SPI_MOSI"), Pin("GND"), Pin("SPI_MISO"), Pin("GPIO6"), Pin("SPI_CLK"), Pin("SPI_CS0"),
            Pin("GND"), Pin("SPI_CS1"), Pin("I2C_SDA"), Pin("I2C_SCL"), Pin("GPIO21"), Pin("GND"), Pin("GPIO22"), Pin("GPIO26"),
            Pin("GPIO23"), Pin("GND"), Pin("GPIO24"), Pin("GPIO27"), Pin("GPIO25"), Pin("GPIO28"), Pin("GND"), Pin("GPIO29")
        ), DeviceType.BOARD, 350f, 650f),
        Device("ESP32-S3 DevKit", listOf(
            Pin("3V3"), Pin("5V"), Pin("EN"), Pin("GND"), Pin("IO4"), Pin("IO43"), Pin("IO5"), Pin("IO44"),
            Pin("IO6"), Pin("IO1"), Pin("IO7"), Pin("IO2"), Pin("IO15"), Pin("IO42"), Pin("IO16"), Pin("IO41"),
            Pin("IO17"), Pin("IO40"), Pin("IO18"), Pin("IO39"), Pin("IO8"), Pin("IO38"), Pin("IO19"), Pin("IO37"),
            Pin("IO20"), Pin("IO36"), Pin("IO21"), Pin("IO35"), Pin("IO47"), Pin("IO0"), Pin("IO48"), Pin("IO45"),
            Pin("IO33"), Pin("IO46"), Pin("IO34"), Pin("GND"), Pin("GND"), Pin("TX"), Pin("GND"), Pin("RX")
        ), DeviceType.BOARD, 300f, 650f),
        Device("Pi Pico", listOf(
            Pin("GP0"), Pin("VBUS"), Pin("GP1"), Pin("VSYS"), Pin("GND"), Pin("GND"), Pin("GP2"), Pin("3V3_EN"),
            Pin("GP3"), Pin("3V3_OUT"), Pin("GP4"), Pin("ADC_VREF"), Pin("GP5"), Pin("GP28"), Pin("GND"), Pin("AGND"),
            Pin("GP6"), Pin("GP27"), Pin("GP7"), Pin("GP26"), Pin("GP8"), Pin("RUN"), Pin("GP9"), Pin("GP22"),
            Pin("GND"), Pin("GND"), Pin("GP10"), Pin("GP21"), Pin("GP11"), Pin("GP20"), Pin("GP12"), Pin("GP19"),
            Pin("GP13"), Pin("GP18"), Pin("GND"), Pin("GND"), Pin("GP14"), Pin("GP17"), Pin("GP15"), Pin("GP16")
        ), DeviceType.BOARD, 320f, 650f),
        Device("NodeMCU V3", listOf(
            Pin("A0"), Pin("D0"), Pin("RSV"), Pin("D1"), Pin("RSV"), Pin("D2"), Pin("SD3"), Pin("D3"),
            Pin("SD2"), Pin("D4"), Pin("SD1"), Pin("3V3"), Pin("CMD"), Pin("GND"), Pin("SD0"), Pin("D5"),
            Pin("CLK"), Pin("D6"), Pin("GND"), Pin("D7"), Pin("3V3"), Pin("D8"), Pin("EN"), Pin("RX"),
            Pin("RST"), Pin("TX"), Pin("GND"), Pin("GND"), Pin("VIN"), Pin("3V3")
        ), DeviceType.BOARD, 280f, 550f)
    )

    private val modules = listOf(
        Device("Accelerometer MPU6050", listOf(Pin("VCC"), Pin("GND"), Pin("SCL"), Pin("SDA"), Pin("AD0"), Pin("INT")), DeviceType.MODULE, 220f, 250f),
        Device("Bluetooth HC-05", listOf(Pin("VCC"), Pin("GND"), Pin("TX"), Pin("RX"), Pin("STATE"), Pin("EN")), DeviceType.MODULE, 200f, 250f),
        Device("Button", listOf(Pin("S1"), Pin("S2")), DeviceType.MODULE, 100f, 100f),
        Device("Buzzer", listOf(Pin("+"), Pin("-")), DeviceType.MODULE, 100f, 100f),
        Device("Common GND", (1..8).map { Pin("GND") }, DeviceType.POWER, 400f, 100f),
        Device("DHT11 Temp/Hum", listOf(Pin("VCC"), Pin("DATA"), Pin("NC"), Pin("GND")), DeviceType.MODULE, 180f, 200f),
        Device("Joystick", listOf(Pin("GND"), Pin("+5V"), Pin("VRX"), Pin("VRY"), Pin("SW")), DeviceType.MODULE, 200f, 220f),
        Device("LCD 1602 (I2C)", listOf(Pin("GND"), Pin("VCC"), Pin("SDA"), Pin("SCL")), DeviceType.MODULE, 350f, 150f),
        Device("LDR Sensor", listOf(Pin("VCC"), Pin("GND"), Pin("DO"), Pin("AO")), DeviceType.MODULE, 180f, 180f),
        Device("LED", listOf(Pin("+"), Pin("-")), DeviceType.MODULE, 100f, 100f),
        Device("Motor DC", listOf(Pin("M1"), Pin("M2")), DeviceType.ACTUATOR, 180f, 180f),
        Device("OLED 0.96", listOf(Pin("VCC"), Pin("GND"), Pin("SCL"), Pin("SDA")), DeviceType.MODULE, 220f, 180f),
        Device("PIR Motion", listOf(Pin("VCC"), Pin("OUT"), Pin("GND")), DeviceType.MODULE, 180f, 150f),
        Device("Potentiometer", listOf(Pin("1"), Pin("2"), Pin("3")), DeviceType.MODULE, 150f, 120f),
        Device("Power 12V", listOf(Pin("AC_L"), Pin("AC_N"), Pin("+12V"), Pin("GND")), DeviceType.POWER, 300f, 200f),
        Device("Relay 1ch", listOf(Pin("VCC"), Pin("GND"), Pin("IN"), Pin("NO"), Pin("COM"), Pin("NC")), DeviceType.ACTUATOR, 200f, 280f),
        Device("RFID RC522", listOf(Pin("3.3V"), Pin("RST"), Pin("GND"), Pin("IRQ"), Pin("MISO"), Pin("MOSI"), Pin("SCK"), Pin("SDA")), DeviceType.MODULE, 250f, 320f),
        Device("Servo", listOf(Pin("PWM"), Pin("VCC"), Pin("GND")), DeviceType.ACTUATOR, 150f, 220f),
        Device("Step-Down LM2596", listOf(Pin("IN+"), Pin("IN-"), Pin("OUT+"), Pin("OUT-")), DeviceType.POWER, 180f, 150f),
        Device("Ultrasonic HC-SR04", listOf(Pin("VCC"), Pin("TRIG"), Pin("ECHO"), Pin("GND")), DeviceType.MODULE, 250f, 150f)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val schematicView = findViewById<SchematicView>(R.id.schematicView)
        val addBoardButton = findViewById<Button>(R.id.addBoardButton)
        val addModuleButton = findViewById<Button>(R.id.addModuleButton)
        val clearButton = findViewById<Button>(R.id.clearButton)
        val exportButton = findViewById<Button>(R.id.exportButton)
        val snapToGridButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.snapToGridButton)
        val languageButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.languageButton)
        val customButton = findViewById<Button>(R.id.customModuleButton)

        var isRussian = true
        languageButton.setOnClickListener {
            isRussian = !isRussian
            if (isRussian) {
                languageButton.text = "RU"
                addBoardButton.text = "Плата"
                addModuleButton.text = "Модуль"
                clearButton.text = "Сброс"
                customButton.text = "Свой модуль"
            } else {
                languageButton.text = "EN"
                addBoardButton.text = "Board"
                addModuleButton.text = "Module"
                clearButton.text = "Clear"
                customButton.text = "Custom"
            }
        }

        snapToGridButton.setOnClickListener {
            schematicView.snapToGrid = !schematicView.snapToGrid
            snapToGridButton.alpha = if (schematicView.snapToGrid) 1.0f else 0.5f
            val status = if (isRussian) {
                if (schematicView.snapToGrid) "Включена" else "Выключена"
            } else {
                if (schematicView.snapToGrid) "Enabled" else "Disabled"
            }
            android.widget.Toast.makeText(this, if(isRussian) "Привязка к сетке: $status" else "Snap to grid: $status", android.widget.Toast.LENGTH_SHORT).show()
        }

        exportButton.setOnClickListener {
            val bitmap = schematicView.exportToBitmap()
            if (bitmap != null) {
                val filename = "sketch_${System.currentTimeMillis()}.png"
                try {
                    val resolver = contentResolver
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/SchematicApp")
                        }
                    }

                    val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (imageUri != null) {
                        resolver.openOutputStream(imageUri).use { out ->
                            if (out != null) {
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                                android.widget.Toast.makeText(this, if(isRussian) "Сохранено в Галерею (Pictures/SchematicApp)" else "Saved to Gallery", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(this, if(isRussian) "Схема пуста" else "Schematic is empty", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        schematicView.onDeviceMenuListener = { device, _ ->
            MaterialAlertDialogBuilder(this)
                .setTitle(if(isRussian) "Модуль: ${device.device.name}" else "Module: ${device.device.name}")
                .setItems(if(isRussian) arrayOf(
                    if (device.isLocked) "Разблокировать" else "Заблокировать",
                    "Удалить модуль"
                ) else arrayOf(
                    if (device.isLocked) "Unlock" else "Lock",
                    "Delete module"
                )) { _, which ->
                    when (which) {
                        0 -> {
                            device.isLocked = !device.isLocked
                            schematicView.invalidate()
                        }
                        1 -> {
                            MaterialAlertDialogBuilder(this)
                                .setTitle(if(isRussian) "Удаление" else "Delete")
                                .setMessage(if(isRussian) "Удалить ${device.device.name}?" else "Delete ${device.device.name}?")
                                .setPositiveButton(if(isRussian) "Да" else "Yes") { _, _ ->
                                    schematicView.removeDevice(device)
                                }
                                .setNegativeButton(if(isRussian) "Отмена" else "Cancel", null)
                                .show()
                        }
                    }
                }
                .show()
        }

        schematicView.onConnectionMenuListener = { connection, _ ->
            val colorsRU = arrayOf("Красный", "Черный", "Синий", "Зеленый", "Желтый")
            val colorsEN = arrayOf("Red", "Black", "Blue", "Green", "Yellow")
            val colorValues = intArrayOf(android.graphics.Color.RED, android.graphics.Color.BLACK, android.graphics.Color.BLUE, android.graphics.Color.GREEN, android.graphics.Color.YELLOW)
            
            MaterialAlertDialogBuilder(this)
                .setTitle(if(isRussian) "Настройка провода" else "Wire Settings")
                .setItems(if(isRussian) arrayOf("Изменить цвет", "Удалить связь") else arrayOf("Change color", "Delete connection")) { _, which ->
                    when (which) {
                        0 -> {
                            MaterialAlertDialogBuilder(this)
                                .setTitle(if(isRussian) "Выберите цвет" else "Select color")
                                .setItems(if(isRussian) colorsRU else colorsEN) { _, colorIdx ->
                                    connection.color = colorValues[colorIdx]
                                    schematicView.invalidate()
                                }
                                .show()
                        }
                        1 -> {
                            schematicView.removeConnection(connection)
                        }
                    }
                }
                .show()
        }

        val saveButton = findViewById<Button>(R.id.saveButton)
        val loadButton = findViewById<Button>(R.id.loadButton)

        saveButton.setOnClickListener {
            val editText = android.widget.EditText(this).apply {
                hint = if(isRussian) "Название проекта" else "Project name"
                setPadding(50, 40, 50, 40)
            }
            
            MaterialAlertDialogBuilder(this)
                .setTitle(if(isRussian) "Сохранить проект" else "Save project")
                .setView(editText)
                .setPositiveButton(if(isRussian) "Сохранить" else "Save") { _, _ ->
                    val projectName = editText.text.toString().ifBlank { if(isRussian) "Без названия" else "Untitled" }
                    val data = schematicView.getProjectData()
                    val json = com.google.gson.Gson().toJson(data)
                    val fileName = "project_$projectName.json"
                    
                    openFileOutput(fileName, android.content.Context.MODE_PRIVATE).use {
                        it.write(json.toByteArray())
                    }
                    android.widget.Toast.makeText(this, if(isRussian) "Проект '$projectName' сохранен" else "Project '$projectName' saved", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(if(isRussian) "Отмена" else "Cancel", null)
                .show()
        }

        loadButton.setOnClickListener {
            val files = fileList().filter { it.startsWith("project_") && it.endsWith(".json") }.toMutableList()
            if (files.isEmpty()) {
                android.widget.Toast.makeText(this, if(isRussian) "Нет сохраненных проектов" else "No saved projects", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val projectNames = files.map { it.removePrefix("project_").removeSuffix(".json") }.toTypedArray()
            
            MaterialAlertDialogBuilder(this)
                .setTitle(if(isRussian) "Управление проектами" else "Manage projects")
                .setItems(projectNames) { _, which ->
                    val fileName = files[which]
                    val projectName = projectNames[which]
                    
                    MaterialAlertDialogBuilder(this)
                        .setTitle(if(isRussian) "Проект: $projectName" else "Project: $projectName")
                        .setItems(if(isRussian) arrayOf("Загрузить", "Удалить") else arrayOf("Load", "Delete")) { _, actionIdx ->
                            when (actionIdx) {
                                0 -> { // Загрузить
                                    try {
                                        val json = openFileInput(fileName).bufferedReader().readText()
                                        val data = com.google.gson.Gson().fromJson(json, SchematicView.ProjectData::class.java)
                                        schematicView.loadProjectData(data)
                                        android.widget.Toast.makeText(this, if(isRussian) "Проект '$projectName' загружен" else "Project '$projectName' loaded", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(this, "Error loading", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                                1 -> { // Удалить
                                    MaterialAlertDialogBuilder(this)
                                        .setTitle(if(isRussian) "Удаление" else "Delete")
                                        .setMessage(if(isRussian) "Удалить проект '$projectName'?" else "Delete project '$projectName'?")
                                        .setPositiveButton(if(isRussian) "Да" else "Yes") { _, _ ->
                                            if (deleteFile(fileName)) {
                                                android.widget.Toast.makeText(this, if(isRussian) "Проект удален" else "Project deleted", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .setNegativeButton(if(isRussian) "Отмена" else "Cancel", null)
                                        .show()
                                }
                            }
                        }
                        .show()
                }
                .show()
        }

        customButton.setOnClickListener {
            val layout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(50, 40, 50, 40)
            }

            val nameInput = android.widget.EditText(this).apply { hint = if(isRussian) "Название (Sensor)" else "Name (Sensor)" }
            val pinsInput = android.widget.EditText(this).apply { hint = if(isRussian) "Пины (VCC,GND,OUT)" else "Pins (VCC,GND,OUT)" }

            layout.addView(nameInput)
            layout.addView(pinsInput)

            MaterialAlertDialogBuilder(this)
                .setTitle(if(isRussian) "Свой модуль" else "Custom Module")
                .setView(layout)
                .setPositiveButton(if(isRussian) "Создать" else "Create") { _, _ ->
                    val name = nameInput.text.toString().ifBlank { "Custom" }
                    val pinNames = pinsInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    
                    if (pinNames.isEmpty()) {
                        android.widget.Toast.makeText(this, "No pins", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        val customDevice = Device(
                            name = name,
                            pins = pinNames.map { Pin(it) },
                            type = DeviceType.MODULE,
                            preferredWidth = 200f,
                            preferredHeight = (pinNames.size / 2 + 1) * 60f + 40f
                        )
                        schematicView.addDevice(customDevice)
                    }
                }
                .setNegativeButton(if(isRussian) "Отмена" else "Cancel", null)
                .show()
        }

        addBoardButton.setOnClickListener {
            val names = boards.map { it.name }.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setTitle(if(isRussian) "Выберите контроллер" else "Select Controller")
                .setItems(names) { _, which ->
                    schematicView.addDevice(boards[which])
                }
                .show()
        }

        addModuleButton.setOnClickListener {
            val names = modules.map { it.name }.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setTitle(if(isRussian) "Добавить компонент" else "Add Component")
                .setItems(names) { _, which ->
                    schematicView.addDevice(modules[which])
                }
                .show()
        }

        clearButton.setOnClickListener {
            schematicView.clear()
        }
    }
}
