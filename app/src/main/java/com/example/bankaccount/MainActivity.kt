package com.example.bankaccount

import UserViewModel
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bankaccount.ui.theme.BankAccountTheme
import androidx.compose.foundation.layout.Column
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // User not signed in; redirect to SignInActivity
            navigateToSignIn()
        } else {
            // User is signed in; proceed with MainActivity
            enableEdgeToEdge()
            setContent {
                BankAccountTheme {
                    BankAccountApp(onSignOut = { navigateToSignIn() })
                }
            }
        }
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountApp(onSignOut: () -> Unit) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            MainScreen(
                onAddAccountClick = { navController.navigate("country_selection") },
                onSignOut = onSignOut
            )
        }
        composable("country_selection") {
            CountrySelectionScreen(
                onCountrySelected = { selectedCountry ->
                    navController.navigate("add_bank_form/$selectedCountry")
                }
            )
        }
        composable(
            route = "add_bank_form/{country}",
            arguments = listOf(navArgument("country") { defaultValue = "Unknown" })
        ) { backStackEntry ->
            val country = backStackEntry.arguments?.getString("country") ?: "Unknown"
            AddBankCardForm(viewModel = UserViewModel(), userId = "user456", country = country)
        }
    }
}

@Composable
fun AddBankCardForm(viewModel: UserViewModel, userId: String, country: String) {
    var bankName by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center // Center the form horizontally and vertically
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp) // Add elevation for shadow effect
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Bank Account",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(text = "Country: $country", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // Bank Name Input Field
                TextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // IBAN Input Field
                TextField(
                    value = iban,
                    onValueChange = { iban = it },
                    label = { Text("IBAN") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Add Bank Card Button
                Button(
                    onClick = {
                        val bankCard = mapOf(
                            "bankName" to bankName,
                            "iban" to iban,
                            "country" to country,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        viewModel.addBankCard(userId, bankCard)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Bank Card")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier, onAddAccountClick: () -> Unit = {}, onSignOut: () -> Unit = {}) {
    // State to manage the visibility of the dropdown menu
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FinRepo") },
                navigationIcon = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu, // Default menu icon
                            contentDescription = "Menu"
                        )
                    }

                    // Dropdown Menu
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            onClick = {
                                menuExpanded = false // Close the menu
                                FirebaseAuth.getInstance().signOut() // Sign out from Firebase
                                onSignOut() // Navigate back to the SignInActivity
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAccountClick) { // Trigger navigation to Country Selection
                Icon(
                    imageVector = Icons.Default.Add, // "+" icon
                    contentDescription = "Add Account"
                )
            }
        }
    ) { paddingValues ->
        // Empty state message
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "You don't have any bank accounts added yet! Tap the '+' button to add them now",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), // Light gray color
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountrySelectionScreen(onCountrySelected: (String) -> Unit = {}) {
    val countries = listOf(
        "Afghanistan" to "🇦🇫",
        "Albania" to "🇦🇱",
        "Algeria" to "🇩🇿",
        "Andorra" to "🇦🇩",
        "Angola" to "🇦🇴",
        "Antigua and Barbuda" to "🇦🇬",
        "Argentina" to "🇦🇷",
        "Armenia" to "🇦🇲",
        "Australia" to "🇦🇺",
        "Austria" to "🇦🇹",
        "Azerbaijan" to "🇦🇿",
        "Bahamas" to "🇧🇸",
        "Bahrain" to "🇧🇭",
        "Bangladesh" to "🇧🇩",
        "Barbados" to "🇧🇧",
        "Belarus" to "🇧🇾",
        "Belgium" to "🇧🇪",
        "Belize" to "🇧🇿",
        "Benin" to "🇧🇯",
        "Bhutan" to "🇧🇹",
        "Bolivia" to "🇧🇴",
        "Bosnia and Herzegovina" to "🇧🇦",
        "Botswana" to "🇧🇼",
        "Brazil" to "🇧🇷",
        "Brunei" to "🇧🇳",
        "Bulgaria" to "🇧🇬",
        "Burkina Faso" to "🇧🇫",
        "Burundi" to "🇧🇮",
        "Cabo Verde" to "🇨🇻",
        "Cambodia" to "🇰🇭",
        "Cameroon" to "🇨🇲",
        "Canada" to "🇨🇦",
        "Central African Republic" to "🇨🇫",
        "Chad" to "🇹🇩",
        "Chile" to "🇨🇱",
        "China" to "🇨🇳",
        "Colombia" to "🇨🇴",
        "Comoros" to "🇰🇲",
        "Congo (Congo-Brazzaville)" to "🇨🇬",
        "Costa Rica" to "🇨🇷",
        "Croatia" to "🇭🇷",
        "Cuba" to "🇨🇺",
        "Cyprus" to "🇨🇾",
        "Czechia" to "🇨🇿",
        "Denmark" to "🇩🇰",
        "Djibouti" to "🇩🇯",
        "Dominica" to "🇩🇲",
        "Dominican Republic" to "🇩🇴",
        "Ecuador" to "🇪🇨",
        "Egypt" to "🇪🇬",
        "El Salvador" to "🇸🇻",
        "Equatorial Guinea" to "🇬🇶",
        "Eritrea" to "🇪🇷",
        "Estonia" to "🇪🇪",
        "Eswatini" to "🇸🇿",
        "Ethiopia" to "🇪🇹",
        "Fiji" to "🇫🇯",
        "Finland" to "🇫🇮",
        "France" to "🇫🇷",
        "Gabon" to "🇬🇦",
        "Gambia" to "🇬🇲",
        "Georgia" to "🇬🇪",
        "Germany" to "🇩🇪",
        "Ghana" to "🇬🇭",
        "Greece" to "🇬🇷",
        "Grenada" to "🇬🇩",
        "Guatemala" to "🇬🇹",
        "Guinea" to "🇬🇳",
        "Guinea-Bissau" to "🇬🇼",
        "Guyana" to "🇬🇾",
        "Haiti" to "🇭🇹",
        "Honduras" to "🇭🇳",
        "Hungary" to "🇭🇺",
        "Iceland" to "🇮🇸",
        "India" to "🇮🇳",
        "Indonesia" to "🇮🇩",
        "Iran" to "🇮🇷",
        "Iraq" to "🇮🇶",
        "Ireland" to "🇮🇪",
        "Israel" to "🇮🇱",
        "Italy" to "🇮🇹",
        "Jamaica" to "🇯🇲",
        "Japan" to "🇯🇵",
        "Jordan" to "🇯🇴",
        "Kazakhstan" to "🇰🇿",
        "Kenya" to "🇰🇪",
        "Kiribati" to "🇰🇮",
        "Kuwait" to "🇰🇼",
        "Kyrgyzstan" to "🇰🇬",
        "Laos" to "🇱🇦",
        "Latvia" to "🇱🇻",
        "Lebanon" to "🇱🇧",
        "Lesotho" to "🇱🇸",
        "Liberia" to "🇱🇷",
        "Libya" to "🇱🇾",
        "Liechtenstein" to "🇱🇮",
        "Lithuania" to "🇱🇹",
        "Luxembourg" to "🇱🇺",
        "Madagascar" to "🇲🇬",
        "Malawi" to "🇲🇼",
        "Malaysia" to "🇲🇾",
        "Maldives" to "🇲🇻",
        "Mali" to "🇲🇱",
        "Malta" to "🇲🇹",
        "Marshall Islands" to "🇲🇭",
        "Mauritania" to "🇲🇷",
        "Mauritius" to "🇲🇺",
        "Mexico" to "🇲🇽",
        "Micronesia" to "🇫🇲",
        "Moldova" to "🇲🇩",
        "Monaco" to "🇲🇨",
        "Mongolia" to "🇲🇳",
        "Montenegro" to "🇲🇪",
        "Morocco" to "🇲🇦",
        "Mozambique" to "🇲🇿",
        "Myanmar (Burma)" to "🇲🇲",
        "Namibia" to "🇳🇦",
        "Nauru" to "🇳🇷",
        "Nepal" to "🇳🇵",
        "Netherlands" to "🇳🇱",
        "New Zealand" to "🇳🇿",
        "Nicaragua" to "🇳🇮",
        "Niger" to "🇳🇪",
        "Nigeria" to "🇳🇬",
        "North Korea" to "🇰🇵",
        "North Macedonia" to "🇲🇰",
        "Norway" to "🇳🇴",
        "Oman" to "🇴🇲",
        "Pakistan" to "🇵🇰",
        "Palau" to "🇵🇼",
        "Palestine" to "🇵🇸",
        "Panama" to "🇵🇦",
        "Papua New Guinea" to "🇵🇬",
        "Paraguay" to "🇵🇾",
        "Peru" to "🇵🇪",
        "Philippines" to "🇵🇭",
        "Poland" to "🇵🇱",
        "Portugal" to "🇵🇹",
        "Qatar" to "🇶🇦",
        "Romania" to "🇷🇴",
        "Russia" to "🇷🇺",
        "Rwanda" to "🇷🇼",
        "Saint Kitts and Nevis" to "🇰🇳",
        "Saint Lucia" to "🇱🇨",
        "Saint Vincent and the Grenadines" to "🇻🇨",
        "Samoa" to "🇼🇸",
        "San Marino" to "🇸🇲",
        "Sao Tome and Principe" to "🇸🇹",
        "Saudi Arabia" to "🇸🇦",
        "Senegal" to "🇸🇳",
        "Serbia" to "🇷🇸",
        "Seychelles" to "🇸🇨",
        "Sierra Leone" to "🇸🇱",
        "Singapore" to "🇸🇬",
        "Slovakia" to "🇸🇰",
        "Slovenia" to "🇸🇮",
        "Solomon Islands" to "🇸🇧",
        "Somalia" to "🇸🇴",
        "South Africa" to "🇿🇦",
        "South Korea" to "🇰🇷",
        "South Sudan" to "🇸🇸",
        "Spain" to "🇪🇸",
        "Sri Lanka" to "🇱🇰",
        "Sudan" to "🇸🇩",
        "Suriname" to "🇸🇷",
        "Sweden" to "🇸🇪",
        "Switzerland" to "🇨🇭",
        "Syria" to "🇸🇾",
        "Tajikistan" to "🇹🇯",
        "Tanzania" to "🇹🇿",
        "Thailand" to "🇹🇭",
        "Timor-Leste" to "🇹🇱",
        "Togo" to "🇹🇬",
        "Tonga" to "🇹🇴",
        "Trinidad and Tobago" to "🇹🇹",
        "Tunisia" to "🇹🇳",
        "Turkey" to "🇹🇷",
        "Turkmenistan" to "🇹🇲",
        "Tuvalu" to "🇹🇻",
        "Uganda" to "🇺🇬",
        "Ukraine" to "🇺🇦",
        "United Arab Emirates" to "🇦🇪",
        "United Kingdom" to "🇬🇧",
        "United States" to "🇺🇸",
        "Uruguay" to "🇺🇾",
        "Uzbekistan" to "🇺🇿",
        "Vanuatu" to "🇻🇺",
        "Vatican City" to "🇻🇦",
        "Venezuela" to "🇻🇪",
        "Vietnam" to "🇻🇳",
        "Yemen" to "🇾🇪",
        "Zambia" to "🇿🇲",
        "Zimbabwe" to "🇿🇼"
    )
    var searchQuery by remember { mutableStateOf("") } // State to hold the search query

    // Filtered list based on the search query
    val filteredCountries = countries.filter {
        it.first.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select Your Bank Account's Country") }) // App bar with title
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            // Search Bar with Clear Button
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search for a country") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                }
            )

            // Display a message if no results are found
            if (filteredCountries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                // LazyColumn for the list
                LazyColumn {
                    items(filteredCountries) { countryPair ->
                        val (country, flag) = countryPair // Deconstruct the pair
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountrySelected(country) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = flag,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Text(
                                text = country,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Divider() // Divider between rows
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BankAccountTheme {
        MainScreen()
    }
}
