package android.example.myinventoryapp


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.example.myinventoryapp.data.Item
import android.example.myinventoryapp.databinding.FragmentAddItemBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

class AddItemFragment: Fragment() {
   // lateinit var imageView: ImageView

    val REQUEST_IMAGE = 100
    var profilePicturePath: String? = null
    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database
                .itemDao()
        )
    }

    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    lateinit var item: Item
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? { _binding = FragmentAddItemBinding.inflate(inflater, container, false)

       //Загружаем фото из галереи
        //imageView = binding.uploadPhoto
       // val getAction = registerForActivityResult(ActivityResultContracts.GetContent(),
       // ActivityResultCallback { uri ->
       //     imageView.setImageURI(uri)
       // })
       // binding.btnGallery.setOnClickListener {
       //     getAction.launch("image/*")
       // }

        binding.uploadPhoto.setOnClickListener{
            selectProfilePicture()
        }

        return binding.root
    }

    //Возвращаем истину, если EditText не пустой
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.itemName.text.toString(),
            binding.itemPrice.text.toString(),
            binding.itemCount.text.toString(),
            binding.uploadPhoto.toString()
        )
    }

    private fun bind(item: Item) {
        val price = "%.2f".format(item.itemPrice)
        binding.apply {
            itemName.setText(item.itemName, TextView.BufferType.SPANNABLE)
            itemPrice.setText(price, TextView.BufferType.SPANNABLE)
            itemCount.setText(item.quantityInStock.toString(), TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener { updateItem() }
        }
    }

    //Вставляет новый элемент в базу данных и переходит к фрагменту списка
    private fun addNewItem() {
        if (isEntryValid()) {
            viewModel.addNewItem(
                binding.itemName.text.toString(),
                binding.itemPrice.text.toString(),
                binding.itemCount.text.toString(),
                binding.uploadPhoto.toString()
            )
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    //Обновляет существующий элемент в базе данных и переходит к фрагменту списка
    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.updateItem(
                this.navigationArgs.itemId,
                this.binding.itemName.text.toString(),
                this.binding.itemPrice.text.toString(),
                this.binding.itemCount.text.toString(),
                binding.uploadPhoto.toString()
            )
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    //Вызывается при создании View
    //Аргумент itemId Navigation определяет элемент редактирования или добавление нового элемента
    //Если itemId положительный, этот метод извлекает информацию из базы данных и
    //позволяет пользователю обновлять его.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.itemId
        if (id > 0) {
            viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedItem ->
                item = selectedItem
                bind(item)
            }
        } else {
            binding.saveAction.setOnClickListener {
                addNewItem()
            }
        }
    }

    //Загружаем фото из галереи
    fun selectProfilePicture(){
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null){
            startActivityForResult(intent, REQUEST_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data

            profilePicturePath = imageUri.toString()
            binding.uploadPhoto.setImageURI(data?.data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Скрывает клавиатуру
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }
}