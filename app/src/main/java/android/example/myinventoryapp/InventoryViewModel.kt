package android.example.myinventoryapp

import android.example.myinventoryapp.data.Item
import android.example.myinventoryapp.data.ItemDao
import androidx.lifecycle.*
import kotlinx.coroutines.launch


class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {
    //Кэширует все элементы из базы данных с помощью LiveData
    val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()

    //Возвращают true, если товары доступны для продажи, иначе будет false
    fun isStockAvailable(item: Item): Boolean {
        return (item.quantityInStock > 0)
    }

    //Обновляет существующий элемент в базе данных
    fun updateItem(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, itemName, itemPrice, itemCount)
        updateItem(updatedItem)
    }

    //Запуск корутинов для обновления не блокирующих элементов
    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    //Уменьшает запас товаров на одну единицу и обновляет базу данных
    fun sellItem(item: Item) {
        if (item.quantityInStock > 0) {
            val newItem = item.copy(quantityInStock = item.quantityInStock - 1)
            updateItem(newItem)
        }
    }

    //Вставляет новый элемент в базу данных
    fun addNewItem(itemName: String, itemPrice: String, itemCount: String) {
        val newItem = getNewItemEntry(itemName, itemPrice, itemCount)
        insertItem(newItem)
    }

    //Запуск новых корутинов для вставки элемента без блокировки
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    //Удаление товара
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    //Удаление всего списка
    fun deleteAllItems(){
        viewModelScope.launch {
            itemDao.deleteAll()
        }
    }

    //Получить элемент из репозитория
    fun retrieveItem(id: Int): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }

    //Возвращает true, если EditTexts не пустые
    fun isEntryValid(itemName: String, itemPrice: String, itemCount: String): Boolean {
        if (itemName.isBlank() || itemPrice.isBlank() || itemCount.isBlank()) {
            return false
        }
        return true
    }

    //Используется для добавления новой записи в базу данных которую добавил пользователь
    private fun getNewItemEntry(itemName: String, itemPrice: String, itemCount: String): Item {
        return Item(
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt()
        )
    }

    //Вызывается для обновления существующей записи в базе данных
    //Возвращает экземпляр класса сущности [Item] с информацией об элементе, обновленной пользователем
    private fun getUpdatedItemEntry(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String
    ): Item {
        return Item(
            id = itemId,
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt()
        )
    }
}

class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}