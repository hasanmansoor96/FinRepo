import android.util.Log
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    // Add a new user
    fun addUser(userId: String, name: String, email: String) {
        Log.d("UserViewModel", "addUser called with userId: $userId, name: $name, email: $email")
        repository.addUser(userId, name, email) { success ->
            if (success) {
                Log.d("UserViewModel", "User added successfully")
            } else {
                Log.e("UserViewModel", "Failed to add user")
            }
        }
    }

    // Add a new bank card
    fun addBankCard(userId: String, bankCard: Map<String, Any>) {
        repository.addBankCard(userId, bankCard) { success ->
            if (success) {
                Log.d("UserViewModel", "Bank card added successfully")
            } else {
                Log.e("UserViewModel", "Failed to add bank card")
            }
        }
    }

    // Fetch all bank cards
    fun fetchBankCards(userId: String) {
        repository.fetchBankCards(userId) { cards ->
            if (cards != null) {
                Log.d("UserViewModel", "Fetched cards: $cards")
            } else {
                Log.e("UserViewModel", "Failed to fetch bank cards")
            }
        }
    }
}
