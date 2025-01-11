import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class UserRepository {
    private val db = Firebase.firestore

    // Add User
    fun addUser(userId: String, name: String, email: String, onComplete: (Boolean) -> Unit) {
        Log.d("UserRepository", "addUser called with userId: $userId")
        val user = mapOf(
            "name" to name,
            "email" to email,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("UserRepository", "User successfully added to Firestore")
                onComplete(true) }
            .addOnFailureListener {e ->
                Log.e("UserRepository", "Error adding user to Firestore", e)
                onComplete(false) }
    }

    // Add Bank Card
    fun addBankCard(userId: String, cardData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(userId).collection("bankCards")
            .add(cardData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Fetch Bank Cards
    fun fetchBankCards(userId: String, onComplete: (List<Map<String, Any>>?) -> Unit) {
        db.collection("users").document(userId).collection("bankCards")
            .get()
            .addOnSuccessListener { result ->
                onComplete(result.map { it.data })
            }
            .addOnFailureListener { onComplete(null) }
    }
}