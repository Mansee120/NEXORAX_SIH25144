package com.example.vibestore.data.local

import androidx.compose.ui.graphics.Color
import com.example.vibestore.R
import com.example.vibestore.data.local.entity.UserLocation
import com.example.vibestore.model.Coupon
import com.example.vibestore.model.PaymentMethod
import com.example.vibestore.model.Shipping

object DataDummy {
    val dummyUserLocation = listOf(
        UserLocation(
            id = 1,
            name = "Mas Azi",
            address = "Jl. Durian No. 103, Marigold Society " +
                    "dange chowk, pune " +
                    "India 425412"
        ),
        UserLocation(
            id = 2,
            name = "Mansi patil",
            address = "Jl. Durian No. 123, Alard University " +
                    "Kab. Semarang, saras bagh " +
                    "India 50664"
        ),
    )
    val dummyShipping = listOf(
        Shipping(
            name = "Fast Delivery",
            price = 10.0,
            description = "Estimated time of arrival 3 - 5 days"
        ),
        Shipping(
            name = "Standard Delivery",
            price = 40.0,
            description = "Estimated time of arrival 1 - 2 days"
        ),
        Shipping(
            name = "Express Delivery",
            price = 50.0,
            description = "Estimated time of arrival 1 days"
        )
    )
    data class PaymentMethod(
        val icon: Int,
        val name: String,
        val color: Color = Color.White // <-- ADD THIS LINE
    )
    val dummyPaymentMethod = listOf(
        PaymentMethod(
            icon = R.drawable.upi,
            name = "UPI",
            color= Color.Blue
        ),

        PaymentMethod(
            icon = R.drawable.icon_master_card,
            name = "Mastercard"
        ),
        PaymentMethod(
            icon = R.drawable.cash,
            name = "Cash on delivery"
        ),
        PaymentMethod(
            icon = R.drawable.icon_paypal,
            name = "Paypal"
        ),
        PaymentMethod(
            icon = R.drawable.atm,
            name = "Credit/Debit/ATM card"
        ),

    )
    val dummyCoupon = listOf(
        Coupon(
            discountedPrice = "FREE SHIPPING",
            description = "Applies to get free shipping",
            expiredDate = "31 December 2026",
            color1 = Color(0xFF9733EE),
            color2 = Color(0xFFDA22FF),
            couponCode = "CRUTCH123"
        ),
        Coupon(
            discountedPrice = "25%",
            description = "Applies to get 25% off",
            expiredDate = "31 December 2028",
            color1 = Color(0xFFFFA726),
            color2 = Color(0xFFFFD54F),
            couponCode = "SIH2025"
        ),
        Coupon(
            discountedPrice = "40%",
            couponCode = "FIRSTUSERTOMEDIKART",
            description = "Applies to get 40% off",
            expiredDate = "31 December 2027",
            color1 = Color(0xFF00C9FF),
            color2 = Color(0xFF92FE9D)
        )
    )
}