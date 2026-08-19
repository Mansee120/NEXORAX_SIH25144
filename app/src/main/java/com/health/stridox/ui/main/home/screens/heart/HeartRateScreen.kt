@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.health.stridox.ui.main.home.screens.heart

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.stridox.R
import ir.ehsannarmani.compose_charts.LineChart
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateScreen(onBackClick: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.heart_rate_title),
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.back_content_description)
                    )
                }
            }
        )
    }) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            val options = listOf(
                stringResource(R.string.week_time_period),
                stringResource(R.string.month_time_period)
            )
            val weekPoints = listOf(70f, 74f, 76f, 73f, 77f, 75f, 78f) // 7 aggregated week points
            val monthPoints =
                listOf(68f, 70f, 72f, 74f, 76f, 80f, 78f, 77f, 75f, 76f, 100f, 120f) // 12 sample

            // current selection + chart data
            var selectedIndex by remember { mutableIntStateOf(0) } // 0=Day,1=Week,2=Month
            var currentPoints by remember { mutableStateOf(weekPoints) } // default Day

            // Big card with current SpO2%
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6B6B).copy(alpha = 0.06f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFFF6B6B).copy(alpha = 0.18f)
                )
            ) {

                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.current_metric_label), color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "75",
                            fontSize = 72.sp,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.bpm_unit),
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.heart_rate_status_normal),
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // -- inside your column where Trends card was --
            Card(
                colors = CardDefaults.cardColors(Color(0xFFFF6B6B).copy(alpha = 0.06f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFFF6B6B).copy(alpha = 0.18f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row with segmented ToggleButtons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.animateContentSize()
                    ) {
                        Text(
                            stringResource(R.string.trends_title_format, options[selectedIndex]),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.animateContentSize()
                        )
                        // Segmented ToggleButton group for Day / Week / Month
                        Row(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val modifiers = listOf(
                                Modifier.weight(1f),
                                Modifier.weight(1f),
                                Modifier.weight(1f)
                            )

                            options.forEachIndexed { index, label ->
                                ToggleButton(
                                    checked = selectedIndex == index,
                                    onCheckedChange = {
                                        selectedIndex = index
                                        // update dummy data on selection change:
                                        currentPoints = when (index) {
                                            0 -> weekPoints
                                            1 -> monthPoints
                                            else -> monthPoints
                                        }
                                    },
                                    modifier = modifiers[index]
                                        .semantics { role = Role.RadioButton }
                                        .padding(vertical = 2.dp)
                                        .height(38.dp),
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        checkedContainerColor = Color(0xFFFF6B6B).copy(alpha = 0.6f),
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = ButtonDefaults.elevatedButtonElevation(
                                        1.dp, 1.dp, 1.dp, 1.dp, 1.dp
                                    ),
                                    contentPadding = PaddingValues(
                                        vertical = 4.dp,
                                        horizontal = 6.dp
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selectedIndex == index) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val bmi = stringResource(R.string.bpm_unit)
                    // Chart area - use currentPoints (converted to Double for Line)
                    Box(
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth()
                    ) {
                        val heartRateLine = Line(
                            values = currentPoints.map { it.toDouble() },
                            color = SolidColor(Color(0xFFE01C30)),
                            label = stringResource(R.string.bpm_unit),
                            curvedEdges = true,
                            drawStyle = DrawStyle.Fill,
                            firstGradientFillColor = Color(0xFFE01C30).copy(alpha = 0.18f),
                            secondGradientFillColor = Color.Transparent,
                            dotProperties = DotProperties(enabled = true),
                            popupProperties = PopupProperties(
                                enabled = true,
                                textStyle = TextStyle.Default.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                ),
                                containerColor = Color.Transparent,
                                duration = 2000,
                                contentBuilder = {
                                    it.value.toInt().toString() + " " + bmi
                                }
                            )
                        )

                        LineChart(
                            modifier = Modifier.fillMaxSize(),
                            data = listOf(heartRateLine),
                            curvedEdges = true,
                            animationDelay = 250,
                            animationMode = AnimationMode.Together(),
                            dividerProperties = DividerProperties(enabled = false),
                            gridProperties = GridProperties(enabled = false),
                            indicatorProperties = HorizontalIndicatorProperties(enabled = false),
                            labelHelperProperties = LabelHelperProperties(
                                textStyle = TextStyle.Default.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            ),
                            labelProperties = LabelProperties(
                                enabled = false, textStyle = TextStyle.Default.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            ),
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

            // Cards for daily low / high
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(Color(0xFFFF6B6B).copy(alpha = 0.06f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFFF6B6B).copy(alpha = 0.18f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.low_value_format, options[selectedIndex]),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                currentPoints.min().toInt().toString(),
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.bpm_unit), color = Color(0xFF94A3B8))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Card(
                    colors = CardDefaults.cardColors(Color(0xFFFF6B6B).copy(alpha = 0.06f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFFF6B6B).copy(alpha = 0.18f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.high_value_format, options[selectedIndex]),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                currentPoints.max().toInt().toString(),
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.bpm_unit), color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

        }
    }
}