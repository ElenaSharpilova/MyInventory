package android.example.myinventoryapp

import android.app.Application
import android.example.myinventoryapp.data.ItemRoomDatabase

class InventoryApplication: Application() {
    //База создается при необходимости, а не при запуске приложения
    val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(this) }
}