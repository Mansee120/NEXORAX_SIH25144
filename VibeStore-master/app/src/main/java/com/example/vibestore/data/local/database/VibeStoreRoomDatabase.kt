package com.example.vibestore.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.vibestore.data.local.dao.CartDao
import com.example.vibestore.data.local.dao.CheckoutDao
import com.example.vibestore.data.local.dao.FavouriteDao
import com.example.vibestore.data.local.dao.NotificationDao
import com.example.vibestore.data.local.dao.OrderDao
import com.example.vibestore.data.local.dao.UserLocationDao
import com.example.vibestore.data.local.entity.Cart
import com.example.vibestore.data.local.entity.Checkout
import com.example.vibestore.data.local.entity.Favourite
import com.example.vibestore.data.local.entity.Notification
import com.example.vibestore.data.local.entity.Order
import com.example.vibestore.data.local.entity.UserLocation

@Database(
    entities = [
        Cart::class,
        Favourite::class,
        Order::class,
        UserLocation::class,
        Checkout::class,
        Notification::class
    ],
    version = 5,      // ⬅ Updated version
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class VibeStoreRoomDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun orderDao(): OrderDao
    abstract fun checkoutDao(): CheckoutDao
    abstract fun userLocationDao(): UserLocationDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: VibeStoreRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): VibeStoreRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibeStoreRoomDatabase::class.java,
                    "vibestore_database"   // ⬅ Changed to a proper name
                )
                    .fallbackToDestructiveMigration(false) // auto migration if schema changes
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
