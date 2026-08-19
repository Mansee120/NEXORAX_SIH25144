@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.health.stridox.ui.main.home.screens.steps

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import java.text.NumberFormat
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsScreen(
    onBackClick: () -> Unit,
    steps: Int = 8421,
    goal: Int = 10_000,
) {
    // Colors closely matching screenshot
    val panel = Color(0xFF45B7D1).copy(alpha = 0.06f)
    val accent = Color(0xFF45B7D1) // ring + progress
    val accentDark = Color(0xFF2F3E4C) // ring darker arc

    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    // Dummy datasets:
    // Day -> 24 hourly points (0..23)
    val stepsDay = List(24) { idx -> (3000 + idx * 200 + Random(idx).nextInt(0, 800)).toFloat() }
    // Week -> 7 daily totals
    val stepsWeek = listOf(4500f, 6200f, 7800f, 8421f, 9200f, 10120f, 9800f)
    // Month -> 30 daily totals
    val stepsMonth = List(30) { (6000 + Random(it).nextInt(-1500, 2500)).toFloat() }
    // Selection state: 0=Day,1=Week,2=Month
    var selectedIndex by remember { mutableIntStateOf(0) }
    var currentPoints by remember { mutableStateOf(stepsDay) }
    val options = listOf(
        stringResource(R.string.day),
        stringResource(R.string.week),
        stringResource(R.string.month)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.steps_title),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null
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
        ) {

            // Top large panel with circular ring + progress bar
            Card(
                colors = CardDefaults.cardColors(containerColor = panel),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = accent.copy(alpha = 0.18f)
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Circular progress with center text
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularStepsRing(
                            progress = progress,
                            ringColor = accent,
                            ringBgColor = accentDark,
                            ringThickness = 14.dp,
                            modifier = Modifier.size(140.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Goal text
                    Text(
                        text = stringResource(R.string.goal_format, goal),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(10.dp))

                    // horizontal progress
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        // background track
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2B3944))
                        )
                        // filled progress
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accent)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(10.dp))

            // History header + segmented control (Day / Week / Month)
            Card(
                colors = CardDefaults.cardColors(containerColor = panel),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = accent.copy(alpha = 0.18f)
                )
            ) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.steps_history_title_format, options[selectedIndex]),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.animateContentSize()
                    )
                    Spacer(Modifier.weight(1f))

                    Row(
                        Modifier
                            .padding(6.dp)
                            .animateContentSize()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val modifiers =
                            listOf(Modifier.weight(1f), Modifier.weight(1f), Modifier.weight(1f))
                        options.forEachIndexed { index, label ->
                            ToggleButton(
                                checked = selectedIndex == index,
                                onCheckedChange = {
                                    selectedIndex = index
                                    currentPoints = when (index) {
                                        0 -> stepsDay
                                        1 -> stepsWeek
                                        else -> stepsMonth
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
                                    checkedContainerColor = accent.copy(alpha = 0.7f),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                elevation = ButtonDefaults.elevatedButtonElevation(
                                    1.dp,
                                    1.dp,
                                    1.dp,
                                    1.dp,
                                    1.dp
                                ),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                val stepsTxt = stringResource(R.string.steps_title)
                // Chart area (reacts to selected dataset)
                val line = Line(
                    values = currentPoints.map { it.toDouble() },
                    color = SolidColor(accent),
                    label = stringResource(R.string.steps_title),
                    curvedEdges = true,
                    drawStyle = DrawStyle.Fill,
                    firstGradientFillColor = accent,
                    secondGradientFillColor = Color.Transparent,
                    dotProperties = DotProperties(enabled = true),
                    popupProperties = PopupProperties(
                        enabled = true,
                        textStyle = TextStyle.Default.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        ),
                        containerColor = Color.Transparent,
                        duration = 1600,
                        contentBuilder = {
                            it.value.format(0) + " " + stepsTxt
                        }
                    )
                )

                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    LineChart(
                        modifier = Modifier.fillMaxSize(),
                        data = listOf(line),
                        curvedEdges = true,
                        animationDelay = 200,
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
                        labelProperties = LabelProperties(enabled = false),
                        popupProperties = PopupProperties(
                            enabled = true,
                            textStyle = TextStyle.Default.copy(
                                color = Color.White,
                                fontSize = 12.sp
                            ),
                            containerColor = Color(0xFF26323D),
                            duration = 1500,
                            contentBuilder = {
                                it.value.format(0)
                            }
                        ),
                        dotsProperties = DotProperties(enabled = true)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // Achievements header
            Text(
                stringResource(R.string.achievements_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Achievement cards
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AchievementCard(
                    title = stringResource(R.string.daily_goal_streak_title),
                    subtitle = stringResource(R.string.daily_goal_streak_subtitle),
                    iconRes = R.drawable.directions_walk,
                    containerColor = panel
                )
                Spacer(Modifier.height(10.dp))
                AchievementCard(
                    title = stringResource(R.string.perfect_week_title),
                    subtitle = stringResource(R.string.perfect_week_subtitle),
                    iconRes = R.drawable.fire,
                    containerColor = panel
                )
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun AchievementCard(
    title: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    containerColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(containerColor, shape = CardDefaults.shape)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF45B7D1).copy(alpha = 0.18f),
                ),
                CardDefaults.shape
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF6DA3D0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color(0xFF9AA6B0))
        }
    }
}

@Composable
private fun CircularStepsRing(
    progress: Float,
    ringColor: Color,
    ringBgColor: Color,
    ringThickness: Dp,
    modifier: Modifier = Modifier
) {
    // center values
    val steps = 8421
    val stepsText = NumberFormat.getIntegerInstance().format(steps)
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = ringThickness.toPx()
            val size = size.minDimension
            val radius = size / 2f - stroke / 2f
            val center = Offset(size / 2f, size / 2f)
            // background arc (full circle)
            drawArc(
                color = ringBgColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // progress arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stepsText,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(text = stringResource(R.string.steps_unit), color = Color(0xFF9AA6B0))
        }
    }
}
