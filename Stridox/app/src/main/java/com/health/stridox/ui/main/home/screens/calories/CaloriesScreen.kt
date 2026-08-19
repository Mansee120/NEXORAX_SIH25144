@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.health.stridox.ui.main.home.screens.calories

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaloriesScreen(
    onBackClick: () -> Unit
) {
    // Colors
    val panelRounded = Color(0xFFFFA502).copy(alpha = 0.06f)
    val accent = Color(0xFFFFA502)
    val muted = Color(0xFF94A3B8)
    val intakeColor = Brush.verticalGradient(listOf(Color(0xFFF6A21A), Color(0xFFF6A21A)))
    val burnColor = Brush.verticalGradient(listOf(Color(0xFFFF8AA6), Color(0xFFFF8AA6)))
    val burnDarkColor = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary
        )
    )
    val accentGreen = Color(0xFF19C286)

    // Bar properties tuned to look like the screenshot:
    val barProps = BarProperties(
        thickness = 8.dp,       // bar width
        spacing = 4.dp,          // spacing between stacked/grouped bars
        cornerRadius = Bars.Data.Radius.Rectangle(6.dp, 6.dp, 6.dp, 6.dp),
        style = DrawStyle.Fill    // keep it filled
    )

    // --- Dummy dataset generators ---
    // Week: 7 items (Mon..Sun)
    val weekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val weekBars = weekLabels.map { day ->
        Bars(
            label = day, values = listOf(
                Bars.Data(
                    value = (500..1200).random().toDouble(),
                    color = intakeColor,
                    label = stringResource(R.string.intake_label)
                ),
                Bars.Data(
                    value = (0..500).random().toDouble(),
                    color = burnColor,
                    label = stringResource(R.string.burn_label)
                )
            )
        )
    }

    // Month: 30 days (1..30)
    val monthBars = (1..30).map { d ->
        Bars(
            label = d.toString(), values = listOf(
                Bars.Data(
                    value = (1200..2200).random().toDouble(),
                    color = intakeColor,
                    label = stringResource(R.string.intake_label)
                ), Bars.Data(
                    value = (0..900).random().toDouble(),
                    color = burnDarkColor,
                    label = stringResource(R.string.burn_label)
                )
            )
        )
    }

    // Year: 12 months (Jan..Dec)
    val monthShort = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val yearBars = monthShort.map { m ->
        Bars(
            label = m, values = listOf(
                Bars.Data(
                    value = (20000..60000).random().toDouble(),
                    color = intakeColor,
                    label = stringResource(R.string.intake_label)
                ), Bars.Data(
                    value = (5000..20000).random().toDouble(),
                    color = burnColor,
                    label = stringResource(R.string.burn_label)
                )
            )
        )
    }

    // Selection state: 0 = Week, 1 = Month, 2 = Year
    var selectedIndex by remember { mutableIntStateOf(0) }
    var currentBars by remember { mutableStateOf(weekBars) }
    val options = listOf(
        stringResource(R.string.week),
        stringResource(R.string.month),
        stringResource(R.string.year)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.calories_title),
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
                })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Chart card
            Card(
                colors = CardDefaults.cardColors(containerColor = panelRounded),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = accent.copy(alpha = 0.18f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .animateContentSize()
                ) {
                    // Header row: segmented Week / Month / Year control
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Text(
                            options[selectedIndex],
                            color = muted,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.animateContentSize()
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            Modifier
                                .padding(horizontal = 8.dp)
                                .animateContentSize()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val modifiers = listOf(
                                Modifier.weight(1f), Modifier.weight(1f), Modifier.weight(1f)
                            )
                            options.forEachIndexed { index, label ->
                                ToggleButton(
                                    checked = selectedIndex == index,
                                    onCheckedChange = {
                                        selectedIndex = index
                                        currentBars = when (index) {
                                            0 -> weekBars
                                            1 -> monthBars
                                            else -> yearBars
                                        }
                                    },
                                    modifier = modifiers[index]
                                        .semantics {
                                            role = Role.RadioButton
                                        }
                                        .padding(vertical = 2.dp)
                                        .height(38.dp),
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        checkedContainerColor = Color(0xFFDE9E3D).copy(alpha = 0.8f),
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = ButtonDefaults.elevatedButtonElevation(
                                        1.dp, 1.dp, 1.dp, 1.dp, 1.dp
                                    ),
                                    contentPadding = PaddingValues(
                                        vertical = 4.dp, horizontal = 8.dp
                                    )) {
                                    Text(
                                        text = label, color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ColumnChart area
                    Box(
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                    ) {
                        ColumnChart(
                            modifier = Modifier.fillMaxSize(),
                            data = currentBars,
                            barProperties = barProps,
                            indicatorProperties = HorizontalIndicatorProperties(
                                textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                                count = IndicatorCount.CountBased(5),
                                contentBuilder = {
                                    it.format(0)
                                }),
                            labelHelperProperties = LabelHelperProperties(
                                textStyle = TextStyle.Default.copy(color = muted)
                            ),
                            labelProperties = LabelProperties(
                                enabled = false,
                                textStyle = TextStyle.Default.copy(color = muted),
                            ),
                            gridProperties = GridProperties(enabled = true),
                            dividerProperties = DividerProperties(enabled = true),
                            popupProperties = PopupProperties(
                                enabled = true,
                                textStyle = TextStyle.Default.copy(
                                    color = Color.White, fontSize = 12.sp
                                ),
                                containerColor = Color(0xFF26323D),
                                duration = 1500,
                                contentBuilder = {
                                    it.value.format(0)
                                })
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary panel (intake / burn / net calories goal) — unchanged
            Card(
                colors = CardDefaults.cardColors(containerColor = panelRounded),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = accent.copy(alpha = 0.18f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.calorie_intake_title), color = muted)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "1,540",
                                    color = Color(0xFFF6A21A),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.kcal_unit), color = muted)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.calorie_burn_title),
                                color = muted,
                                modifier = Modifier.align(Alignment.End)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    "320",
                                    color = Color(0xFFFF8AA6),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.kcal_unit), color = muted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(stringResource(R.string.net_calories_goal_label), color = muted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "1,220",
                                    color = accentGreen,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.kcal_unit), color = muted)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // progress bar
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .height(12.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2B3944))
                                )
                                Box(
                                    modifier = Modifier
                                        .height(12.dp)
                                        .fillMaxWidth(0.68f) // sample progress (1220 / 1800)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentGreen)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                stringResource(R.string.goal_calories_format, "1,800"),
                                color = muted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
