@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.vibestore.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.vibestore.R
import com.example.vibestore.ui.theme.VibeStoreTheme
import com.example.vibestore.ui.theme.poppinsFontFamily

@Composable
fun OrderItem(
    modifier: Modifier = Modifier,
    date: String,
    firstOrderItemName: String,
    firstOrderItemImage: String,
    otherOrderQuantity: Int,
    quantity: Int,
    totalPrice: Double,
    id: Int,
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp
        ),
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
            .shadow(
                elevation = 5.dp,
                spotColor = MaterialTheme.colorScheme.onPrimary,
                ambientColor = Color(0x293F3F3F),
                shape = CardDefaults.shape,
                clip = true,
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier
                .padding(
                    vertical = 16.dp,
                    horizontal = 16.dp
                )
                .fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_shopping_bag_filled),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Shopping",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = poppinsFontFamily,
                        modifier = Modifier
                    )
                    Text(
                        text = date,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Light,
                        fontFamily = poppinsFontFamily,
                        modifier = Modifier
                    )
                }
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(4.dp)
                ) {
                    Text(
                        text = "Shipping",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFE3E3E3)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 14.dp)
            ) {
                AsyncImage(
                    model = firstOrderItemImage,
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(
                        text = "OrderId: $id",
                        maxLines = 1,
                        fontSize = 11.sp,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = firstOrderItemName,
                        maxLines = 1,
                        fontSize = 16.sp,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Qty: $quantity",
                        maxLines = 1,
                        fontSize = 14.sp,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Light
                    )
                    if (otherOrderQuantity > 0) {
                        Text(
                            text = "and $otherOrderQuantity other items",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = poppinsFontFamily,
                        )
                    }
                }
            }
            Column {
                Text(
                    text = "Total Order",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Light,
                    fontFamily = poppinsFontFamily,
                    modifier = Modifier
                )
                Text(
                    text = "₹${"%.2f".format(totalPrice)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = poppinsFontFamily,
                    modifier = Modifier
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderItemPreview() {
    VibeStoreTheme(
        dynamicColor = false
    ) {
        OrderItem(
            date = "4 September 2023",
            firstOrderItemName = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
            firstOrderItemImage = "",
            otherOrderQuantity = 1,
            totalPrice = 100.0,
            id = 1,
            quantity = 2
        )
    }
}