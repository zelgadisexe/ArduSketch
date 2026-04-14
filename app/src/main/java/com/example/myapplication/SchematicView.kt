package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import java.util.UUID
import kotlin.math.sqrt

class SchematicView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class DeviceInstance(
        val id: String = UUID.randomUUID().toString(),
        val device: Device,
        var x: Float,
        var y: Float,
        var isLocked: Boolean = false
    ) {
        val width = device.preferredWidth
        val height = device.preferredHeight

        fun getPinPos(pinName: String): PointF {
            val index = device.pins.indexOfFirst { it.name == pinName }
            if (index == -1) return PointF(x, y)
            val step = height / (device.pins.size / 2 + 1).coerceAtLeast(1)
            val isLeft = index % 2 == 0
            val sideIndex = index / 2
            val pinY = y + step * (sideIndex + 1)
            return if (isLeft) PointF(x, pinY) else PointF(x + width, pinY)
        }

        fun isHit(tx: Float, ty: Float): Boolean {
            return tx >= x && tx <= x + width && ty >= y && ty <= y + height
        }

        fun getPinAt(tx: Float, ty: Float): String? {
            device.pins.forEach {
                val pos = getPinPos(it.name)
                val dist = sqrt(((tx - pos.x) * (tx - pos.x) + (ty - pos.y) * (ty - pos.y)).toDouble())
                if (dist < 45) return it.name
            }
            return null
        }
    }

    private val devices = mutableListOf<DeviceInstance>()
    private val connections = mutableListOf<Connection>()

    private val boardPaint = Paint().apply { color = Color.parseColor("#1B5E20"); style = Paint.Style.FILL; isAntiAlias = true }
    private val modulePaint = Paint().apply { color = Color.parseColor("#1565C0"); style = Paint.Style.FILL; isAntiAlias = true }
    private val relayPaint = Paint().apply { color = Color.parseColor("#37474F"); style = Paint.Style.FILL; isAntiAlias = true }
    private val pwrPaint = Paint().apply { color = Color.parseColor("#B71C1C"); style = Paint.Style.FILL; isAntiAlias = true }
    private val borderPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 4f }
    private val pinPaint = Paint().apply { color = Color.YELLOW; style = Paint.Style.FILL }
    private val linePaint = Paint().apply { color = Color.RED; strokeWidth = 6f; style = Paint.Style.STROKE; isAntiAlias = true; strokeJoin = Paint.Join.ROUND }
    private val gndLinePaint = Paint().apply { color = Color.BLACK; strokeWidth = 6f; style = Paint.Style.STROKE; isAntiAlias = true; strokeJoin = Paint.Join.ROUND }
    private val textPaint = Paint().apply { color = Color.WHITE; textSize = 28f; textAlign = Paint.Align.CENTER }
    private val labelPaint = Paint().apply { color = Color.BLACK; textSize = 24f }
    private val lockPaint = Paint().apply { color = Color.WHITE; textSize = 40f; textAlign = Paint.Align.CENTER }
    private val highlightPinPaint = Paint().apply { color = Color.CYAN; style = Paint.Style.STROKE; strokeWidth = 8f; isAntiAlias = true }
    private val gridPaint = Paint().apply { color = Color.parseColor("#BDBDBD"); strokeWidth = 1f; style = Paint.Style.STROKE }
    var snapToGrid = false
    private val gridSize = 20f

    // Трансформации
    private var scaleFactor = 1.0f
    private val matrixTransform = Matrix()
    private val inverseMatrix = Matrix()
    private var offsetX = 0f
    private var offsetY = 0f

    private var draggedDevice: DeviceInstance? = null
    private var connectingSource: Pair<DeviceInstance, String>? = null
    private var activePath: MutableList<PointF>? = null
    private var touchX = 0f
    private var touchY = 0f
    private var snapTarget: Pair<DeviceInstance, String>? = null
    
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.2f, 5.0f)
            invalidate()
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            val worldPoints = floatArrayOf(e.x, e.y)
            inverseMatrix.mapPoints(worldPoints)
            val wx = worldPoints[0]
            val wy = worldPoints[1]

            // Сначала проверяем пины для меню соединений
            for (inst in devices) {
                val pin = inst.getPinAt(wx, wy)
                if (pin != null) {
                    val existingConn = connections.findLast {
                        (it.fromDeviceId == inst.id && it.fromPinName == pin) ||
                        (it.toDeviceId == inst.id && it.toPinName == pin)
                    }
                    if (existingConn != null) {
                        showConnectionMenu(existingConn)
                        return
                    }
                }
            }

            val device = devices.findLast { it.isHit(wx, wy) }
            if (device != null) {
                showDeviceMenu(device)
            }
        }
    })

    var onDeviceMenuListener: ((DeviceInstance, action: String) -> Unit)? = null
    var onConnectionMenuListener: ((Connection, action: String) -> Unit)? = null

    private fun showDeviceMenu(device: DeviceInstance) {
        onDeviceMenuListener?.invoke(device, "menu")
    }

    private fun showConnectionMenu(conn: Connection) {
        onConnectionMenuListener?.invoke(conn, "menu")
    }

    fun removeDevice(device: DeviceInstance) {
        devices.remove(device)
        connections.removeAll { it.fromDeviceId == device.id || it.toDeviceId == device.id }
        invalidate()
    }

    fun removeConnection(conn: Connection) {
        connections.remove(conn)
        invalidate()
    }

    fun addDevice(device: Device) {
        // Добавляем в центр текущего вида
        val center = floatArrayOf(width / 2f, height / 2f)
        inverseMatrix.mapPoints(center)
        devices.add(DeviceInstance(device = device, x = center[0] - device.preferredWidth/2, y = center[1] - device.preferredHeight/2))
        invalidate()
    }

    fun clear() {
        devices.clear()
        connections.clear()
        scaleFactor = 1.0f
        offsetX = 0f
        offsetY = 0f
        invalidate()
    }

    fun getProjectData(): ProjectData {
        return ProjectData(devices.toList(), connections.toList())
    }

    fun loadProjectData(data: ProjectData) {
        devices.clear()
        devices.addAll(data.devices)
        connections.clear()
        connections.addAll(data.connections)
        invalidate()
    }

    data class ProjectData(
        val devices: List<DeviceInstance>,
        val connections: List<Connection>
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        matrixTransform.reset()
        matrixTransform.postTranslate(offsetX, offsetY)
        matrixTransform.postScale(scaleFactor, scaleFactor, width / 2f, height / 2f)
        canvas.setMatrix(matrixTransform)
        matrixTransform.invert(inverseMatrix)

        drawGrid(canvas)
        drawAll(canvas)
        
        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas) {
        val gridSize = 50f
        
        // Получаем видимую область в мировых координатах
        val visibleRect = floatArrayOf(0f, 0f, width.toFloat(), height.toFloat())
        inverseMatrix.mapPoints(visibleRect)
        
        val left = visibleRect[0]
        val top = visibleRect[1]
        val right = visibleRect[2]
        val bottom = visibleRect[3]

        val startX = (left / gridSize).toInt() * gridSize
        val startY = (top / gridSize).toInt() * gridSize

        var x = startX
        while (x < right) {
            canvas.drawLine(x, top, x, bottom, gridPaint)
            x += gridSize
        }

        var y = startY
        while (y < bottom) {
            canvas.drawLine(left, y, right, y, gridPaint)
            y += gridSize
        }
    }

    private fun drawAll(canvas: Canvas) {
        // Рисуем соединения
        connections.forEach { conn ->
            val d1 = devices.find { it.id == conn.fromDeviceId }
            val d2 = devices.find { it.id == conn.toDeviceId }
            if (d1 != null && d2 != null) {
                drawConnection(canvas, conn, d1, d2)
            }
        }

        // Рисуем активную линию
        connectingSource?.let { (dev, pin) ->
            drawActiveLine(canvas, dev, pin)
        }

        // Рисуем девайсы
        devices.forEach { inst ->
            drawDevice(canvas, inst)
        }
    }

    private fun drawDevice(canvas: Canvas, inst: DeviceInstance) {
        val p = when(inst.device.type) {
            DeviceType.BOARD -> boardPaint
            DeviceType.POWER -> pwrPaint
            DeviceType.ACTUATOR -> relayPaint
            else -> modulePaint
        }
        val rect = RectF(inst.x, inst.y, inst.x + inst.width, inst.y + inst.height)
        canvas.drawRoundRect(rect, 15f, 15f, p)
        canvas.drawRoundRect(rect, 15f, 15f, borderPaint)

        if (inst.isLocked) {
            canvas.drawText("🔒", inst.x + inst.width - 30f, inst.y + 40f, lockPaint)
        }

        canvas.drawText(inst.device.name, inst.x + inst.width/2, inst.y + 40f, textPaint)

        inst.device.pins.forEach { pin ->
            val pos = inst.getPinPos(pin.name)
            canvas.drawCircle(pos.x, pos.y, 14f, pinPaint)
            canvas.drawCircle(pos.x, pos.y, 14f, borderPaint)
            val isLeft = pos.x == inst.x
            labelPaint.textAlign = if (isLeft) Paint.Align.LEFT else Paint.Align.RIGHT
            canvas.drawText(pin.name, pos.x + (if(isLeft) 20f else -20f), pos.y + 10f, labelPaint)
        }
    }

    private fun drawConnection(canvas: Canvas, conn: Connection, d1: DeviceInstance, d2: DeviceInstance) {
        val p1 = d1.getPinPos(conn.fromPinName)
        val p2 = d2.getPinPos(conn.toPinName)

        val isLeft1 = p1.x == d1.x
        val lead1 = if (isLeft1) -50f else 50f
        val isLeft2 = p2.x == d2.x
        val lead2 = if (isLeft2) -50f else 50f

        val pin1 = d1.device.pins.find { it.name == conn.fromPinName }
        val pin2 = d2.device.pins.find { it.name == conn.toPinName }
        val isGnd = (pin1?.name?.contains("GND", ignoreCase = true) == true) ||
                (pin2?.name?.contains("GND", ignoreCase = true) == true)

        val currentPaint = if (conn.color != null) {
            Paint(linePaint).apply { color = conn.color!! }
        } else if (isGnd) {
            gndLinePaint
        } else {
            linePaint
        }

        val path = Path()
        path.moveTo(p1.x, p1.y)
        path.lineTo(p1.x + lead1, p1.y) // Обязательный вынос

        if (conn.path.isEmpty()) {
            val p1OutX = p1.x + lead1
            val p2OutX = p2.x + lead2

            // Решаем: идти через середину или обходить сбоку?
            var midX = (p1OutX + p2OutX) / 2

            // Если середина пути перекрывается самими модулями-участниками
            val d1Range = (d1.x - 20f)..(d1.x + d1.width + 20f)
            val d2Range = (d2.x - 20f)..(d2.x + d2.width + 20f)

            if (midX in d1Range || midX in d2Range) {
                // Выносим трассу за пределы обоих модулей
                midX = if (isLeft1 && isLeft2) {
                    Math.min(p1OutX, p2OutX) - 40f
                } else if (!isLeft1 && !isLeft2) {
                    Math.max(p1OutX, p2OutX) + 40f
                } else {
                    // Если пины с разных сторон, выбираем свободный фланг
                    if (p1OutX < p2OutX) p1OutX else p1OutX
                    midX // оставляем как есть или пробуем сместить
                }
            }

            // Проверка на препятствия (другие модули на пути)
            val midY = (p1.y + p2.y) / 2
            val obstacle = devices.find { it.id != d1.id && it.id != d2.id && it.isHit(midX, midY) }

            if (obstacle != null) {
                // Огибаем
                val avoidY = if (p1.y < obstacle.y + obstacle.height / 2) obstacle.y - 70f else obstacle.y + obstacle.height + 70f
                path.lineTo(p1OutX, avoidY)
                path.lineTo(p2OutX, avoidY)
            } else {
                // Г-образное или Z-образное соединение
                path.lineTo(midX, p1.y)
                path.lineTo(midX, p2.y)
            }
            path.lineTo(p2OutX, p2.y)
        } else {
            conn.path.forEach { path.lineTo(it.x, it.y) }
            path.lineTo(p2.x + lead2, p2.y)
        }

        path.lineTo(p2.x, p2.y)
        canvas.drawPath(path, currentPaint)
    }

    private fun drawActiveLine(canvas: Canvas, dev: DeviceInstance, pin: String) {
        val p1 = dev.getPinPos(pin)
        val isLeft = p1.x == dev.x
        val lead = if (isLeft) -50f else 50f

        val path = Path()
        path.moveTo(p1.x, p1.y)
        path.lineTo(p1.x + lead, p1.y)
        activePath?.forEach { path.lineTo(it.x, it.y) }

        val endX: Float
        val endY: Float

        if (snapTarget != null) {
            val targetPos = snapTarget!!.first.getPinPos(snapTarget!!.second)
            endX = targetPos.x
            endY = targetPos.y
            canvas.drawCircle(endX, endY, 20f, highlightPinPaint)
        } else {
            endX = touchX
            endY = touchY
        }

        path.lineTo(endX, endY)

        val currentPaint = if (pin.contains("GND", ignoreCase = true)) gndLinePaint else linePaint
        canvas.drawPath(path, currentPaint)
    }

    fun exportToBitmap(): Bitmap? {
        if (devices.isEmpty()) return null

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        devices.forEach {
            minX = minOf(minX, it.x - 100f) // С запасом на подписи
            minY = minOf(minY, it.y - 50f)
            maxX = maxOf(maxX, it.x + it.width + 100f)
            maxY = maxOf(maxY, it.y + it.height + 50f)
        }

        val padding = 50f
        val bitmapWidth = (maxX - minX + padding * 2).toInt().coerceAtLeast(100)
        val bitmapHeight = (maxY - minY + padding * 2).toInt().coerceAtLeast(100)

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#D1D1D1")) // Фон как в приложении

        canvas.translate(-minX + padding, -minY + padding)

        drawAll(canvas)

        return bitmap
    }

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var startTouchX = 0f
    private var startTouchY = 0f
    private var dragDeltaX = 0f
    private var dragDeltaY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        val worldPoints = floatArrayOf(event.x, event.y)
        inverseMatrix.mapPoints(worldPoints)
        val wx = worldPoints[0]
        val wy = worldPoints[1]
        
        touchX = wx
        touchY = wy

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startTouchX = event.x
                startTouchY = event.y
                lastTouchX = event.x
                lastTouchY = event.y
                
                // 1. Проверяем нажатие на пин
                for (inst in devices) {
                    val pin = inst.getPinAt(wx, wy)
                    if (pin != null) {
                        connectingSource = Pair(inst, pin)
                        activePath = mutableListOf()
                        return true
                    }
                }
                
                val hitDevice = devices.findLast { it.isHit(wx, wy) }
                if (hitDevice != null && !hitDevice.isLocked) {
                    draggedDevice = hitDevice
                    dragDeltaX = wx - hitDevice.x
                    dragDeltaY = wy - hitDevice.y
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (connectingSource != null) {
                    activePath?.add(PointF(wx, wy))
                    invalidate()
                }
                // Сбрасываем координаты для предотвращения рывка при зуме
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_POINTER_UP -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (scaleDetector.isInProgress) {
                    lastTouchX = event.x
                    lastTouchY = event.y
                    return true
                }
                
                if (draggedDevice != null) {
                    var newX = wx - dragDeltaX
                    var newY = wy - dragDeltaY
                    
                    if (snapToGrid) {
                        newX = Math.round(newX / gridSize) * gridSize
                        newY = Math.round(newY / gridSize) * gridSize
                    }
                    
                    draggedDevice!!.x = newX
                    draggedDevice!!.y = newY
                } else if (connectingSource != null) {
                    // Ищем пин для "прилипания"
                    var foundSnap = false
                    for (inst in devices) {
                        if (inst == connectingSource!!.first) continue
                        val pin = inst.getPinAt(wx, wy)
                        if (pin != null) {
                            snapTarget = Pair(inst, pin)
                            foundSnap = true
                            break
                        }
                    }
                    if (!foundSnap) snapTarget = null
                } else {
                    // Пан панорамирование: делим на scaleFactor, чтобы скорость соответствовала зуму
                    offsetX += (event.x - lastTouchX) / scaleFactor
                    offsetY += (event.y - lastTouchY) / scaleFactor
                }

                lastTouchX = event.x
                lastTouchY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (connectingSource != null) {
                    val finalTarget = snapTarget ?: run {
                        val target = devices.find { it.isHit(wx, wy) }
                        val targetPin = target?.getPinAt(wx, wy)
                        if (target != null && targetPin != null && target != connectingSource!!.first) {
                            Pair(target, targetPin)
                        } else null
                    }

                    if (finalTarget != null) {
                        connections.add(Connection(
                            fromDeviceId = connectingSource!!.first.id,
                            fromPinName = connectingSource!!.second,
                            toDeviceId = finalTarget.first.id,
                            toPinName = finalTarget.second,
                            path = activePath ?: mutableListOf()
                        ))
                    } else {
                        // Если это был просто клик на пине — показываем меню соединений
                        val dist = sqrt(((event.x - startTouchX) * (event.x - startTouchX) + (event.y - startTouchY) * (event.y - startTouchY)).toDouble())
                        if (dist < 20) {
                            val (inst, pin) = connectingSource!!
                            val existingConn = connections.findLast {
                                (it.fromDeviceId == inst.id && it.fromPinName == pin) ||
                                (it.toDeviceId == inst.id && it.toPinName == pin)
                            }
                            if (existingConn != null) {
                                showConnectionMenu(existingConn)
                            }
                        }
                    }
                    connectingSource = null
                    activePath = null
                    snapTarget = null
                }
                draggedDevice = null
                invalidate()
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()
}
