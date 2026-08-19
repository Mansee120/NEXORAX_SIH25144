@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.health.stridox.ui.main.home.screens.health

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.stridox.R
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlin.random.Random

// --- sample base points (week) ---
private val consistencyWeekSample = listOf(85f, 88f, 86f, 90f, 92f, 89f, 87f)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepConsistencyScreen(
    onBackClick: () -> Unit
) {
    // Colors matching screenshot
    val panelRounded = Color(0xFFA5D6A7).copy(alpha = 0.16f) // Green accent base
    val accent = Color(0xFFA5D6A7)
    val muted = Color(0xFF8C98A8)
    val colorLine = Color(0xFFA5D6A7) // main line
    val colorFill = colorLine

    val currentVal = 89f

    // --- Dummy datasets ---
    // Week: 7 points
    val weekPoints = consistencyWeekSample

    // Day: 24 points (simulate hourly data)
    val dayPoints = List(24) { idx ->
        (80f + Random(idx).nextFloat() * 15f)
    }

    // Selection: 0 = Day, 1 = Week
    var selectedIndex by remember { mutableIntStateOf(0) } // Default to Week
    var currentPoints by remember { mutableStateOf(dayPoints) }
    val options = listOf(
        "Day",
        "Week"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Step Consistency",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {

            // Big rounded panel with chart
            Card(
                colors = CardDefaults.cardColors(containerColor = panelRounded),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = accent.copy(alpha = 0.18f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Current Consistency", color = muted)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = currentVal.toInt().toString(),
                                    color = accent,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "%",
                                    color = muted,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Segmented ToggleButton for Day / Week
                        Row(
                            Modifier
                                .width(160.dp)
                                .animateContentSize()
                                .padding(6.dp)
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val modifiers =
                                listOf(
                                    Modifier.weight(1f),
                                    Modifier.weight(1f)
                                )
                            options.forEachIndexed { index, label ->
                                ToggleButton(
                                    checked = selectedIndex == index,
                                    onCheckedChange = {
                                        selectedIndex = index
                                        currentPoints = when (index) {
                                            0 -> dayPoints
                                            else -> weekPoints
                                        }
                                    },
                                    modifier = modifiers[index]
                                        .semantics { role = Role.RadioButton }
                                        .padding(vertical = 2.dp)
                                        .height(36.dp),
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        checkedContainerColor = muted,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = ButtonDefaults.elevatedButtonElevation(
                                        1.dp,
                                        1.dp,
                                        1.dp,
                                        1.dp,
                                        1.dp
                                    ),
                                    contentPadding = PaddingValues(
                                        vertical = 4.dp,
                                        horizontal = 6.dp
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chart area
                    Box(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth()
                    ) {
                        val unitTxt = "%"
                        // build a Line using same chart lib (values must be Double)
                        val line = Line(
                            values = currentPoints.map { it.toDouble() },
                            color = SolidColor(colorLine),
                            label = unitTxt,
                            curvedEdges = true,
                            drawStyle = DrawStyle.Fill,
                            firstGradientFillColor = colorFill,
                            secondGradientFillColor = Color.Transparent,
                            dotProperties = DotProperties(enabled = true),
                            popupProperties = PopupProperties(
                                enabled = true,
                                textStyle = TextStyle.Default.copy(
                                    color = Color.White,
                                    fontSize = 12.sp
                                ),
                                containerColor = Color(0xFF26323D),
                                duration = 1500,
                                contentBuilder = {
                                    it.value.format(0) + " " + unitTxt
                                }
                            )
                        )

                        LineChart(
                            modifier = Modifier.fillMaxSize(),
                            data = listOf(line),
                            curvedEdges = true,
                            animationDelay = 150,
                            animationMode = AnimationMode.Together(),
                            dividerProperties = DividerProperties(enabled = false),
                            gridProperties = GridProperties(enabled = false),
                            indicatorProperties = HorizontalIndicatorProperties(enabled = false),
                            labelHelperProperties = LabelHelperProperties(
                                textStyle = TextStyle.Default.copy(color = muted, fontSize = 12.sp)
                            ),
                            labelProperties = LabelProperties(enabled = false),
                            popupProperties = PopupProperties(
                                enabled = true,
                                containerColor = Color.Transparent,
                                textStyle = TextStyle.Default.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            ),
                            dotsProperties = DotProperties(enabled = true)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Average and Lowest cards
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = panelRounded),
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = accent.copy(alpha = 0.18f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Lowest", color = muted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                currentPoints.min().toInt().toString(),
                                fontSize = 32.sp,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("%", color = accent)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = panelRounded),
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = accent.copy(alpha = 0.18f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Highest", color = muted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                currentPoints.max().toInt().toString(),
                                fontSize = 32.sp,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("%", color = accent)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
