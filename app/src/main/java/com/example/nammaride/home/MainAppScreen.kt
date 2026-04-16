package com.example.nammaride.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.roundToInt
import com.example.nammaride.R

// --- LOTTIE IMPORT ---
import com.airbnb.lottie.compose.*

// --- NEW: ML KIT BARCODE SCANNER IMPORTS ---
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

// --- OSMDROID IMPORTS ---
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

import com.example.nammaride.network.VehicleOption
import com.example.nammaride.theme.LocalNammaColors
import com.example.nammaride.theme.SuccessGreen
import com.example.nammaride.theme.ErrorRed
import com.example.nammaride.utils.ShakeDetector

// --- 1. LOCALIZATION DICTIONARY ---
data class AppStrings(
    val home: String, val settings: String, val account: String,
    val curLoc: String, val whereTo: String, val recent: String, val rideWith: String,
    val prefs: String, val dark: String, val lang: String,
    val safety: String, val sosTitle: String, val sosDesc: String,
    val support: String, val faq: String, val about: String,
    val personal: String, val fullName: String, val phoneText: String, val logout: String, val save: String, val edit: String
)

fun getStrings(lang: String): AppStrings {
    return when (lang) {
        "Hindi" -> AppStrings(
            "होम", "सेटिंग्स", "खाता", "वर्तमान स्थान", "कहाँ जाना है?", "हाल के मार्ग", "सवारी चुनें",
            "प्राथमिकताएं", "डार्क मोड", "भाषा (Language)", "सुरक्षा", "आपातकालीन SOS", "SOS भेजने के लिए फोन हिलाएं",
            "समर्थन", "मदद और सामान्य प्रश्न", "ऐप के बारे में",
            "व्यक्तिगत विवरण", "पूरा नाम :", "फ़ोन :", "लॉग आउट करें", "सहेजें", "संपादित करें"
        )
        "Kannada" -> AppStrings(
            "ಮುಖಪುಟ", "ಸೆಟ್ಟಿಂಗ್‌ಗಳು", "ಖಾತೆ", "ಪ್ರಸ್ತುತ ಸ್ಥಳ", "ಎಲ್ಲಿಗೆ ಹೋಗಬೇಕು?", "ಇತ್ತೀಚಿನ ಮಾರ್ಗಗಳು", "ಸವಾರಿ ಆಯ್ಕೆಮಾಡಿ",
            "ಆದ್ಯತೆಗಳು", "ಡಾರ್ಕ್ ಮೋಡ್", "ಭಾಷೆ (Language)", "ಸುರಕ್ಷತೆ", "ತುರ್ತು SOS", "SOS ಕಳುಹಿಸಲು ಫೋನ್ ಅಲ್ಲಾಡಿಸಿ",
            "ಬೆಂಬಲ", "ಸಹಾಯ ಮತ್ತು FAQ", "ಅಪ್ಲಿಕೇಶನ್ ಬಗ್ಗೆ",
            "ವೈಯಕ್ತಿಕ ವಿವರಗಳು", "ಪೂರ್ಣ ಹೆಸರು :", "ಫೋನ್ :", "ಲಾಗ್ ಔಟ್", "ಉಳಿಸಿ", "ತಿದ್ದು"
        )
        else -> AppStrings(
            "Home", "Settings", "Account", "Current location", "Where to?", "Recent Routes", "Ride with",
            "Preferences", "Dark Mode", "Language", "Safety", "Emergency SOS", "Shake phone to send SOS",
            "Support", "Help & Support (FAQ)", "About App",
            "Personal Details", "FullName :", "Phone :", "Log out", "Save", "Edit"
        )
    }
}

@Composable
fun MainAppScreen(isDarkMode: Boolean, onThemeChange: (Boolean) -> Unit, onLogout: () -> Unit) {
    val colors = LocalNammaColors.current
    var selectedTab by remember { mutableStateOf(0) }
    var isSearchingLocation by remember { mutableStateOf(false) }
    var preSelectedRoute by remember { mutableStateOf<com.example.nammaride.network.RouteItem?>(null) }
    var accountName by remember { mutableStateOf("") }
    var accountEmail by remember { mutableStateOf("") }

    var currentLanguage by remember { mutableStateOf("English") }
    val strings = getStrings(currentLanguage)

    // --- Global SOS State & Sensor Logic ---
    var isSosEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val shakeDetector = remember {
        ShakeDetector(context) {
            val emergencyIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
            }
            context.startActivity(emergencyIntent)
        }
    }

    DisposableEffect(lifecycleOwner, isSosEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isSosEnabled) {
                shakeDetector.startListening()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                shakeDetector.stopListening()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            shakeDetector.stopListening()
        }
    }

    // --- Double Back Press & Tab Navigation Fallback ---
    var backPressedTime by remember { mutableStateOf(0L) }

    BackHandler(enabled = !isSearchingLocation) {
        if (selectedTab != 0) {
            selectedTab = 0
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                (context as? Activity)?.finish()
            } else {
                backPressedTime = currentTime
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (isSearchingLocation) {
        BackHandler { isSearchingLocation = false; preSelectedRoute = null }
        LocationSelectionScreen(
            initialRoute = preSelectedRoute,
            onBackClick = { isSearchingLocation = false; preSelectedRoute = null }
        )
    } else {
        Scaffold(
            containerColor = colors.background,
            bottomBar = {
                NavigationBar(containerColor = colors.input, contentColor = colors.subtext) {
                    NavigationBarItem(icon = { Text("🏠", fontSize = 20.sp) }, label = { Text(strings.home, fontSize = 12.sp) }, selected = selectedTab == 0, onClick = { selectedTab = 0 }, colors = NavigationBarItemDefaults.colors(selectedIconColor = colors.accent, selectedTextColor = colors.accent, indicatorColor = Color.Transparent, unselectedIconColor = colors.subtext, unselectedTextColor = colors.subtext))
                    NavigationBarItem(icon = { Text("⚙️", fontSize = 20.sp) }, label = { Text(strings.settings, fontSize = 12.sp) }, selected = selectedTab == 1, onClick = { selectedTab = 1 }, colors = NavigationBarItemDefaults.colors(selectedIconColor = colors.accent, selectedTextColor = colors.accent, indicatorColor = Color.Transparent, unselectedIconColor = colors.subtext, unselectedTextColor = colors.subtext))
                    NavigationBarItem(icon = { Text("👤", fontSize = 20.sp) }, label = { Text(strings.account, fontSize = 12.sp) }, selected = selectedTab == 2, onClick = { selectedTab = 2 }, colors = NavigationBarItemDefaults.colors(selectedIconColor = colors.accent, selectedTextColor = colors.accent, indicatorColor = Color.Transparent, unselectedIconColor = colors.subtext, unselectedTextColor = colors.subtext))
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> HomeDashboardContent(innerPadding, strings, onSearchClick = { preSelectedRoute = null; isSearchingLocation = true }, onRecentRouteClick = { route -> preSelectedRoute = route; isSearchingLocation = true })
                1 -> SettingsScreenContent(innerPadding, strings, isDarkMode, onThemeChange, currentLanguage, { currentLanguage = it }, isSosEnabled, { isSosEnabled = it })
                2 -> AccountScreenContent(innerPadding, strings, accountName, { accountName = it }, accountEmail, { accountEmail = it }, onLogout)
            }
        }
    }
}

@Composable
fun HomeDashboardContent(innerPadding: PaddingValues, strings: AppStrings, onSearchClick: () -> Unit, onRecentRouteClick: (com.example.nammaride.network.RouteItem) -> Unit) {
    val colors = LocalNammaColors.current
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0)?.lowercase() ?: ""

            if (spokenText.contains("airport") || spokenText.contains("kempegowda")) {
                Toast.makeText(context, "Routing to Airport...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(1, "Kempegowda International Airport", 13.1979, 77.706299))
            } else if (spokenText.contains("majestic")) {
                Toast.makeText(context, "Routing to Majestic...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(2, "Majestic KSRTC BUS Terminal 2A", 12.9779, 77.5739))
            } else if (spokenText.contains("koramangala")) {
                Toast.makeText(context, "Routing to Koramangala...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(3, "Koramangala BDA Complex", 12.9303396, 77.6228853))
            } else if (spokenText.contains("indiranagar")) {
                Toast.makeText(context, "Routing to Indiranagar...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(4, "Indiranagar Metro Station", 12.978286, 77.638757))
            } else if (spokenText.contains("whitefield")) {
                Toast.makeText(context, "Routing to Whitefield...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(5, "Whitefield Tech Park", 12.98471106, 77.73486039))
            } else if (spokenText.contains("yeshwanthpur") || spokenText.contains("yeshwantpur")) {
                Toast.makeText(context, "Routing to Yeshwanthpur...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(6, "Yeshwanthpur Railway Station", 13.023896, 77.551192))
            } else if (spokenText.contains("electronic city") || spokenText.contains("electronic")) {
                Toast.makeText(context, "Routing to Electronic City...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(7, "Electronic City Phase 1", 12.8496783, 77.6649749))
            } else if (spokenText.contains("mg road") || spokenText.contains("m g road")) {
                Toast.makeText(context, "Routing to MG Road...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(8, "MG Road Metro Station", 12.975536, 77.60683))
            } else if (spokenText.contains("cubbon") || spokenText.contains("park")) {
                Toast.makeText(context, "Routing to Cubbon Park...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(9, "Cubbon Park", 12.9752487, 77.5928871))
            } else if (spokenText.contains("lalbagh") || spokenText.contains("botanical")) {
                Toast.makeText(context, "Routing to Lalbagh...", Toast.LENGTH_SHORT).show()
                onRecentRouteClick(com.example.nammaride.network.RouteItem(10, "Lalbagh Botanical Garden", 12.950771, 77.584236))
            } else {
                Toast.makeText(context, "Searching for: $spokenText", Toast.LENGTH_LONG).show()
                onSearchClick() // Fallback to manual search
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp), horizontalAlignment = Alignment.Start) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(strings.curLoc, color = colors.subtext, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text("📍", fontSize = 16.sp); Spacer(modifier = Modifier.width(8.dp))
            Text("Kristu Jayanti (Deemed to be\nUniversity), Bengaluru", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(4.dp)); Text("›", color = colors.text, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { onSearchClick() }.padding(start = 16.dp)
                ) {
                    Text("🔍", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                    Text(strings.whereTo, color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxHeight().clickable {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Where do you want to go?")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice Search not supported.", Toast.LENGTH_SHORT).show()
                        }
                    }.padding(horizontal = 16.dp)
                ) {
                    Text("🎙️", fontSize = 22.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(strings.recent, color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        RecentRouteItem("Kempegowda Int. Airport", "Devanahalli, Bengaluru") { onRecentRouteClick(com.example.nammaride.network.RouteItem(1, "Kempegowda International Airport", 13.1979, 77.706299)) }
        RecentRouteItem("Majestic Bus Stand", "Kempegowda, Bengaluru") { onRecentRouteClick(com.example.nammaride.network.RouteItem(2, "Majestic KSRTC BUS Terminal 2A", 12.9779, 77.5739)) }
        RecentRouteItem("Koramangala BDA", "Koramangala, Bengaluru") { onRecentRouteClick(com.example.nammaride.network.RouteItem(3, "Koramangala BDA Complex", 12.9303396, 77.6228853)) }

        Spacer(modifier = Modifier.weight(1f))
        Text(strings.rideWith, color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            VehicleCategoryCard("🏍️", "Bike", onSearchClick); VehicleCategoryCard("🛺", "Auto", onSearchClick); VehicleCategoryCard("🚗", "Mini", onSearchClick); VehicleCategoryCard("🚙", "SUV", onSearchClick)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsScreenContent(
    innerPadding: PaddingValues,
    strings: AppStrings,
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    currentLang: String,
    onLangChange: (String) -> Unit,
    isSosEnabled: Boolean,
    onSosChange: (Boolean) -> Unit
) {
    val colors = LocalNammaColors.current
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp), horizontalAlignment = Alignment.Start) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(strings.settings, color = colors.text, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(32.dp))

        Text(strings.prefs, color = colors.subtext, fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
        Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(if (isDarkMode) "🌙" else "☀️", fontSize = 20.sp); Spacer(modifier = Modifier.width(16.dp)); Text(strings.dark, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                    Switch(checked = isDarkMode, onCheckedChange = { onThemeChange(it) }, colors = SwitchDefaults.colors(checkedThumbColor = colors.accent, checkedTrackColor = colors.border, uncheckedThumbColor = colors.subtext, uncheckedTrackColor = colors.border))
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Box {
                    Row(modifier = Modifier.fillMaxWidth().clickable { showLanguageMenu = true }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text("🌐", fontSize = 20.sp); Spacer(modifier = Modifier.width(16.dp)); Text(strings.lang, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                        Text(currentLang, color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }, modifier = Modifier.background(colors.input)) {
                        DropdownMenuItem(text = { Text("English", color = colors.text) }, onClick = { onLangChange("English"); showLanguageMenu = false })
                        DropdownMenuItem(text = { Text("हिंदी (Hindi)", color = colors.text) }, onClick = { onLangChange("Hindi"); showLanguageMenu = false })
                        DropdownMenuItem(text = { Text("ಕನ್ನಡ (Kannada)", color = colors.text) }, onClick = { onLangChange("Kannada"); showLanguageMenu = false })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(strings.safety, color = colors.subtext, fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
        Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚨", fontSize = 20.sp); Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(strings.sosTitle, color = ErrorRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(strings.sosDesc, color = colors.subtext, fontSize = 12.sp)
                    }
                }
                Switch(checked = isSosEnabled, onCheckedChange = onSosChange, colors = SwitchDefaults.colors(checkedThumbColor = ErrorRed, checkedTrackColor = colors.border))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(strings.support, color = colors.subtext, fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
        Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().clickable { showFaqDialog = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💬", fontSize = 20.sp); Spacer(modifier = Modifier.width(16.dp)); Text(strings.faq, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Row(modifier = Modifier.fillMaxWidth().clickable { showAboutDialog = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ️", fontSize = 20.sp); Spacer(modifier = Modifier.width(16.dp)); Text(strings.about, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    if (showFaqDialog) FaqBottomSheet(onDismiss = { showFaqDialog = false })
    if (showAboutDialog) AboutAppDialog(onDismiss = { showAboutDialog = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqBottomSheet(onDismiss: () -> Unit) {
    val colors = LocalNammaColors.current
    val faqs = listOf(
        Pair("How do I book a ride?", "Enter your destination in the 'Where to?' box, select a vehicle, and tap Confirm."),
        Pair("Can I pay with NammaCash?", "Yes, you can toggle your payment method to NammaCash once a driver is assigned."),
        Pair("What is the cancellation policy?", "You can cancel for free before the driver arrives."),
        Pair("My driver isn't moving.", "Please try calling the driver. If unresponsive, you can cancel the ride and book a new one."),
        Pair("How does Surge Pricing work?", "Fares slightly increase during peak Bangalore traffic hours (8-11 AM, 5-9 PM) to ensure availability."),
        Pair("Are there night charges?", "Yes, a standard 1.5x multiplier applies between 11 PM and 5 AM."),
        Pair("How do I report a safety issue?", "Use the Emergency SOS toggle in settings."),
        Pair("Can I pre-book a ride?", "Currently, NammaRide only supports on-demand real-time matching."),
        Pair("How is GST calculated?", "As per Karnataka regulations, a 5% GST is applied to the final subtotal of all aggregator rides.")
    )

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.background) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()) {
            Text("Help & Support", color = colors.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Frequently Asked Questions", color = colors.subtext, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                items(faqs) { faq ->
                    var expanded by remember { mutableStateOf(false) }
                    Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { expanded = !expanded }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(faq.first, color = colors.text, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = colors.subtext)
                            }
                            AnimatedVisibility(visible = expanded) {
                                Text(faq.second, color = colors.subtext, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp), lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
    val colors = LocalNammaColors.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = colors.background, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "NammaRide Official Logo",
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(20.dp))
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("NammaRide", color = colors.text, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(24.dp))

                Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Version 2.0.26", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("© 2026 NammaRide Technologies", color = colors.subtext, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AccountScreenContent(innerPadding: PaddingValues, strings: AppStrings, name: String, onNameChange: (String) -> Unit, email: String, onEmailChange: (String) -> Unit, onLogout: () -> Unit) {
    val colors = LocalNammaColors.current
    val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "+91 Not Available"
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp), horizontalAlignment = Alignment.Start) {
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(strings.personal, color = colors.text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.clickable { isEditing = !isEditing }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (isEditing) strings.save else strings.edit, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Medium); Spacer(modifier = Modifier.width(6.dp))
                if (isEditing) Icon(Icons.Default.Check, contentDescription = "Save", tint = SuccessGreen, modifier = Modifier.size(20.dp)) else Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.text, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(strings.fullName, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
        if (isEditing) { OutlinedTextField(value = name, onValueChange = onNameChange, textStyle = TextStyle(color = colors.text, fontSize = 16.sp), modifier = Modifier.fillMaxWidth(), placeholder = { Text("Enter your name", color = colors.subtext) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.accent, unfocusedBorderColor = colors.border, focusedContainerColor = colors.input, unfocusedContainerColor = colors.input)) } else { Text(if (name.isEmpty()) "Not provided" else name, color = if (name.isEmpty()) colors.subtext else colors.text, fontSize = 16.sp) }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Email :", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
        if (isEditing) { OutlinedTextField(value = email, onValueChange = onEmailChange, textStyle = TextStyle(color = colors.text, fontSize = 16.sp), modifier = Modifier.fillMaxWidth(), placeholder = { Text("Enter your email", color = colors.subtext) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.accent, unfocusedBorderColor = colors.border, focusedContainerColor = colors.input, unfocusedContainerColor = colors.input)) } else { Text(if (email.isEmpty()) "Not provided" else email, color = if (email.isEmpty()) colors.subtext else colors.text, fontSize = 16.sp) }
        Spacer(modifier = Modifier.height(24.dp))
        Text(strings.phoneText, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
        Text(phone, color = colors.text, fontSize = 16.sp); Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ErrorRed),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
        ) {
            Text(strings.logout, color = ErrorRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = colors.background,
            title = { Text("Log out?", color = colors.text, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of NammaRide?", color = colors.subtext) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Yes, Log out", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = colors.text)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionScreen(
    initialRoute: com.example.nammaride.network.RouteItem? = null,
    onBackClick: () -> Unit,
    viewModel: RideViewModel = viewModel()
) {
    val colors = LocalNammaColors.current
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    // --- NEW: ML KIT SCANNER & SCAM ALERT STATE ---
    var scannedAmount by remember { mutableStateOf<Double?>(null) }
    var showScamAlert by remember { mutableStateOf(false) }
    var showSafeAlert by remember { mutableStateOf(false) }

    val qrScanner = remember {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }
    // ----------------------------------------------

    val earnedPoints = remember(viewModel.expandedVehicleType) {
        when (viewModel.expandedVehicleType) {
            "Bike" -> 50
            "Mini Cab" -> 30
            "Auto" -> 20
            else -> 0
        }
    }

    LaunchedEffect(initialRoute) {
        viewModel.rideState = "SELECTING"
        viewModel.expandedVehicleType = null
        viewModel.errorMessage = ""

        if (initialRoute != null) {
            viewModel.fetchFares(initialRoute)
        } else {
            viewModel.showFares = false
            viewModel.liveFares = emptyList()
            viewModel.dropLocationText = "Drop Location"
            viewModel.destLat = 0.0
            viewModel.destLng = 0.0
            viewModel.loadAvailableRoutes()
        }
    }

    BackHandler(enabled = viewModel.rideState == "BOOKED") { showCancelDialog = true }

    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(showRoute = viewModel.showFares, destLat = viewModel.destLat, destLng = viewModel.destLng)

        Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
            Surface(color = colors.input.copy(alpha = 0.8f), shape = CircleShape) {
                IconButton(onClick = { if (viewModel.rideState == "BOOKED") showCancelDialog = true else onBackClick() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text) }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(SuccessGreen, CircleShape)); Spacer(modifier = Modifier.width(16.dp))
                Surface(color = colors.input.copy(alpha = 0.95f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp).rotate(-45f)); Spacer(modifier = Modifier.width(16.dp))
                        Text("Kristu Jayanti (Deemed to be University)", color = if (viewModel.rideState == "BOOKED") colors.subtext else colors.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(ErrorRed, CircleShape)); Spacer(modifier = Modifier.width(16.dp))
                Surface(color = colors.input.copy(alpha = 0.95f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(56.dp), onClick = { if (viewModel.rideState == "SELECTING") viewModel.loadAvailableRoutes() }) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("🚩", fontSize = 18.sp); Spacer(modifier = Modifier.width(16.dp))
                        Text(viewModel.dropLocationText, color = if (viewModel.showFares && !viewModel.isLoading && viewModel.rideState == "SELECTING") colors.text else colors.subtext, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp), contentAlignment = Alignment.Center) {
                    Surface(color = colors.input, shape = CircleShape, modifier = Modifier.size(56.dp)) { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.accent) } }
                }
            } else if (viewModel.errorMessage.isNotEmpty()) {
                Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) { Text(viewModel.errorMessage, color = ErrorRed, modifier = Modifier.padding(16.dp)) }
            } else if (viewModel.showFares) {

                val density = LocalDensity.current
                val peekOffsetPx = with(density) { 250.dp.toPx() }
                val maxOffsetPx = with(density) { 350.dp.toPx() }
                var sheetDragOffset by remember { mutableStateOf(0f) }
                val animatedSheetOffset by animateFloatAsState(targetValue = sheetDragOffset, label = "sheetOffset")

                LaunchedEffect(viewModel.rideState) { sheetDragOffset = 0f }

                Surface(
                    color = colors.background, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth().offset { IntOffset(0, animatedSheetOffset.roundToInt()) }
                        .pointerInput(Unit) { detectVerticalDragGestures(onDragEnd = { sheetDragOffset = if (sheetDragOffset > (peekOffsetPx / 3)) peekOffsetPx else 0f }) { change, dragAmount -> change.consume(); sheetDragOffset = (sheetDragOffset + dragAmount).coerceIn(0f, maxOffsetPx) } }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.padding(bottom = 12.dp).width(40.dp).height(5.dp).background(colors.subtext.copy(alpha = 0.4f), CircleShape))

                        when (viewModel.rideState) {
                            "SELECTING" -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Available Rides", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                                        items(viewModel.liveFares) { fare ->
                                            ExpandableFareCard(fare = fare, isExpanded = (viewModel.expandedVehicleType == fare.vehicle_type), onClick = { viewModel.expandedVehicleType = if (viewModel.expandedVehicleType == fare.vehicle_type) null else fare.vehicle_type }, onBookRide = { viewModel.confirmBooking() })
                                        }
                                    }
                                }
                            }
                            "SEARCHING" -> {
                                Spacer(modifier = Modifier.height(48.dp)); CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(64.dp), strokeWidth = 6.dp); Spacer(modifier = Modifier.height(32.dp))
                                Text("Locating nearby drivers...", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                                Text("This usually takes a few seconds.", color = colors.subtext, fontSize = 14.sp); Spacer(modifier = Modifier.height(64.dp))
                            }
                            "BOOKED" -> {
                                var paymentMethod by remember { mutableStateOf("Cash/UPI") }
                                var showPaymentMenu by remember { mutableStateOf(false) }
                                val bookedVehicle = viewModel.liveFares.find { it.vehicle_type == viewModel.expandedVehicleType }
                                val bookedFare = bookedVehicle?.total_fare ?: 0.0
                                val driver = bookedVehicle?.driver_details

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 450.dp)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (earnedPoints > 0) {
                                        Surface(
                                            color = SuccessGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(20.dp),
                                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)),
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        ) {
                                        }
                                    }

                                    Icon(Icons.Default.Check, contentDescription = "Success", tint = SuccessGreen, modifier = Modifier.size(64.dp).background(SuccessGreen.copy(alpha = 0.2f), CircleShape).padding(12.dp)); Spacer(modifier = Modifier.height(16.dp))
                                    Text("Driver Assigned!", color = colors.text, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold); Spacer(modifier = Modifier.height(24.dp))

                                    Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, colors.border)) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("👤", fontSize = 48.sp); Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(driver?.driver_name ?: "Srinivas Gowda", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                Text("★ ${driver?.rating ?: "4.9"}", color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(driver?.license_plate ?: "KA 01 AB 8921", color = colors.input, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.background(colors.accent.copy(alpha = 0.9f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                                Spacer(modifier = Modifier.height(4.dp)); Text(driver?.vehicle_model ?: "White Swift Dzire", color = colors.subtext, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column { Text("Arriving in", color = colors.subtext, fontSize = 14.sp); Text("3 mins", color = colors.text, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) }
                                        Text("OTP: ${driver?.otp ?: "4192"}", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.background(colors.input, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp))
                                    }

                                    Spacer(modifier = Modifier.height(24.dp)); Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border)); Spacer(modifier = Modifier.height(24.dp))

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("Ride details", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            Text("📍", fontSize = 20.sp); Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) { Text(viewModel.dropLocationText, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("Dropoff", color = colors.subtext, fontSize = 14.sp) }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // --- UPDATED PAYMENT ROW: ADDED QR SCANNER BUTTON ---
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            Text("💳", fontSize = 20.sp); Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) { Text("₹${String.format("%.2f", bookedFare)}", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(paymentMethod, color = colors.subtext, fontSize = 14.sp) }

                                            // ML Kit Scan Button
                                            IconButton(
                                                onClick = {
                                                    qrScanner.startScan()
                                                        .addOnSuccessListener { barcode ->
                                                            val rawValue = barcode.rawValue ?: ""
                                                            if (rawValue.startsWith("upi://pay")) {
                                                                val uri = Uri.parse(rawValue)
                                                                val am = uri.getQueryParameter("am")?.toDoubleOrNull()

                                                                if (am != null) {
                                                                    scannedAmount = am
                                                                    if (am > bookedFare) {
                                                                        showScamAlert = true // Driver asked for more!
                                                                    } else {
                                                                        showSafeAlert = true // Price matches or is lower
                                                                    }
                                                                } else {
                                                                    Toast.makeText(context, "Open QR: No fixed amount detected.", Toast.LENGTH_LONG).show()
                                                                }
                                                            } else {
                                                                Toast.makeText(context, "Not a valid UPI QR Code.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                        .addOnFailureListener { Toast.makeText(context, "Scan cancelled.", Toast.LENGTH_SHORT).show() }
                                                },
                                                modifier = Modifier.background(colors.input, CircleShape)
                                            ) {
                                                Text("📷")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))

                                            Box {
                                                OutlinedButton(onClick = { showPaymentMenu = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text), border = BorderStroke(1.dp, colors.border)) { Text("Switch", fontSize = 14.sp) }
                                                DropdownMenu(expanded = showPaymentMenu, onDismissRequest = { showPaymentMenu = false }, modifier = Modifier.background(colors.input)) {
                                                    DropdownMenuItem(text = { Text("Cash/UPI", color = colors.text) }, onClick = { paymentMethod = "Cash/UPI"; showPaymentMenu = false })
                                                    DropdownMenuItem(text = { Text("NammaCash", color = colors.text) }, onClick = { paymentMethod = "NammaCash"; showPaymentMenu = false })
                                                }
                                            }
                                        }
                                        // --------------------------------------------------

                                        Spacer(modifier = Modifier.height(32.dp))
                                        Button(onClick = { showCancelDialog = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.input)) { Text("Cancel ride", color = ErrorRed, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    // --- NEW: SCAM ALERT DIALOG ---
                                    if (showScamAlert) {
                                        AlertDialog(
                                            onDismissRequest = { showScamAlert = false },
                                            containerColor = ErrorRed.copy(alpha = 0.1f),
                                            title = { Text("⚠️ SCAM ALERT", color = ErrorRed, fontWeight = FontWeight.ExtraBold) },
                                            text = {
                                                Column {
                                                    Text("The driver's QR code is attempting to overcharge you.", color = colors.text)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Actual Fare: ₹$bookedFare", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                                    Text("QR Request: ₹$scannedAmount", color = ErrorRed, fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            confirmButton = { TextButton(onClick = { showScamAlert = false }) { Text("Close", color = ErrorRed) } }
                                        )
                                    }

                                    // --- NEW: SAFE FARE DIALOG WITH REDIRECT ---
                                    if (showSafeAlert) {
                                        AlertDialog(
                                            onDismissRequest = { showSafeAlert = false },
                                            containerColor = colors.background,
                                            title = { Text("✅ Fare Verified", color = SuccessGreen, fontWeight = FontWeight.Bold) },
                                            text = { Text("The scanned QR amount (₹$scannedAmount) matches your ride fare. It is safe to pay.", color = colors.subtext) },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    showSafeAlert = false

                                                    // THE REDIRECT MAGIC:
                                                    // This tells Android to open any app that supports UPI (GPay, PhonePe, etc.)
                                                    try {
                                                        // We need to store the rawValue from the scanner earlier to use it here.
                                                        // For presentation purposes, you can create a dummy UPI intent:
                                                        val upiIntent = Intent(Intent.ACTION_VIEW).apply {
                                                            data = Uri.parse("upi://pay?pa=nammaride@ybl&pn=Driver&am=$scannedAmount&cu=INR")
                                                        }
                                                        val chooser = Intent.createChooser(upiIntent, "Complete Payment with...")
                                                        context.startActivity(chooser)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "No UPI app found on this phone.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }) {
                                                    Text("Proceed to Pay", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showSafeAlert = false }) { Text("Cancel", color = colors.text) }
                                            }
                                        )
                                    }
                                    // ------------------------------

                                    if (showCancelDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showCancelDialog = false }, containerColor = colors.background,
                                            title = { Text("Cancel Ride?", color = colors.text, fontWeight = FontWeight.Bold) },
                                            text = { val driverFirstName = driver?.driver_name?.substringBefore(" ") ?: "Srinivas"; Text("Are you sure you want to cancel your ride with $driverFirstName?", color = colors.subtext) },
                                            confirmButton = { TextButton(onClick = { showCancelDialog = false; viewModel.rideState = "SELECTING"; viewModel.expandedVehicleType = null }) { Text("Yes, Cancel", color = ErrorRed, fontWeight = FontWeight.Bold) } },
                                            dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("No, Keep Ride", color = colors.text) } }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (viewModel.rideState == "BOOKED" && earnedPoints > 0) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = 1,
                isPlaying = true
            )

            if (progress < 1f) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (viewModel.showRoutePicker) {
            ModalBottomSheet(onDismissRequest = { viewModel.showRoutePicker = false }, containerColor = colors.background) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Destination", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                        items(viewModel.availableRoutes) { route ->
                            Text(text = "📍 ${route.destination_name}", color = colors.text, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().clickable { viewModel.fetchFares(route) }.padding(vertical = 16.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OsmMapView(showRoute: Boolean, destLat: Double, destLng: Double) {
    val mapBlueColor = android.graphics.Color.parseColor("#3B82F6")
    val kristuJayanti = GeoPoint(13.0583, 77.6426)
    val dynamicDestination = GeoPoint(if (destLat == 0.0) 13.1989 else destLat, if (destLng == 0.0) 77.7068 else destLng)
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    LaunchedEffect(showRoute, destLat, destLng) {
        if (showRoute) {
            routePoints = withContext(Dispatchers.IO) {
                try {
                    val isAirport = destLat in 13.19..13.20
                    val sathanurWaypointLng = 77.6310
                    val sathanurWaypointLat = 13.0945

                    val coordinateString = if (isAirport) {
                        "${kristuJayanti.longitude},${kristuJayanti.latitude};$sathanurWaypointLng,$sathanurWaypointLat;${dynamicDestination.longitude},${dynamicDestination.latitude}"
                    } else {
                        "${kristuJayanti.longitude},${kristuJayanti.latitude};${dynamicDestination.longitude},${dynamicDestination.latitude}"
                    }

                    val url = "https://router.project-osrm.org/route/v1/driving/$coordinateString?overview=full&geometries=geojson"

                    val json = JSONObject(java.net.URL(url).readText())
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val coordinates = routes.getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates")
                        val path = mutableListOf<GeoPoint>()
                        for (i in 0 until coordinates.length()) {
                            val coord = coordinates.getJSONArray(i)
                            path.add(GeoPoint(coord.getDouble(1), coord.getDouble(0)))
                        }
                        path
                    } else emptyList()
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(13.5)
                controller.setCenter(kristuJayanti)
            }
        },
        update = { map ->
            map.overlays.clear()

            map.overlays.add(Marker(map).apply {
                position = kristuJayanti
                title = "Pickup"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = icon?.constantState?.newDrawable()?.mutate()?.apply { setTint(android.graphics.Color.parseColor("#22C55E")) }
            })

            if (showRoute) {
                map.overlays.add(Marker(map).apply {
                    position = dynamicDestination
                    title = "Drop"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = icon?.constantState?.newDrawable()?.mutate()?.apply { setTint(android.graphics.Color.parseColor("#EF4444")) }
                })

                if (routePoints.isNotEmpty()) {
                    map.overlays.add(Polyline().apply {
                        setPoints(routePoints)
                        outlinePaint.color = mapBlueColor
                        outlinePaint.strokeWidth = 16f
                        outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                        outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                    })
                    map.controller.animateTo(GeoPoint((kristuJayanti.latitude + dynamicDestination.latitude) / 2, (kristuJayanti.longitude + dynamicDestination.longitude) / 2))
                    map.controller.setZoom(11.5)
                }
            } else {
                map.controller.animateTo(kristuJayanti)
                map.controller.setZoom(13.5)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableFareCard(fare: VehicleOption, isExpanded: Boolean, onClick: () -> Unit, onBookRide: () -> Unit) {
    val colors = LocalNammaColors.current
    val icon = when (fare.vehicle_type) { "Bike" -> "🏍️"; "Auto" -> "🛺"; "Mini Cab" -> "🚗"; "SUV" -> "🚙"; else -> "🚕" }
    val capacity = when (fare.vehicle_type) { "Bike" -> "1"; "Auto" -> "3"; "Mini Cab" -> "4"; "SUV" -> "6"; else -> "4" }

    Surface(color = if (isExpanded) colors.border else colors.input, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (isExpanded) colors.accent else Color.Transparent), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(icon, fontSize = 32.sp); Spacer(modifier = Modifier.height(4.dp)); Text("👤 $capacity", color = colors.subtext, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column { Text(fare.display_name, color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold); if (fare.surge_active) Text("Demand is high", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Text("₹${String.format("%.2f", fare.total_fare)}", color = colors.text, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border)); Spacer(modifier = Modifier.height(16.dp))
                    Text("Transparent Fare Breakdown", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(modifier = Modifier.height(12.dp))
                    FareRowItem("Base Fare", "₹${String.format("%.2f", fare.breakdown_ui.base_fare)}")
                    FareRowItem("Distance Charge", "₹${String.format("%.2f", fare.breakdown_ui.distance_charge)}")
                    FareRowItem("Time Charge", "₹${String.format("%.2f", fare.breakdown_ui.time_charge)}")
                    if (fare.breakdown_ui.surge_applied > 0) FareRowItem("Surge Multiplier", "+ ₹${String.format("%.2f", fare.breakdown_ui.surge_applied)}", isSurge = true)
                    FareRowItem("Booking Fee", "₹${String.format("%.2f", fare.breakdown_ui.booking_fee)}")
                    FareRowItem("Govt. Tax (5% GST)", "₹${String.format("%.2f", fare.breakdown_ui.government_tax_5_percent)}")
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBookRide, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) { Text("Confirm Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun FareRowItem(label: String, value: String, isSurge: Boolean = false) {
    val colors = LocalNammaColors.current
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label, color = colors.subtext, fontSize = 14.sp); Text(value, color = if (isSurge) ErrorRed else colors.text, fontSize = 14.sp, fontWeight = if (isSurge) FontWeight.Bold else FontWeight.Medium) }
}

@Composable
fun RecentRouteItem(title: String, subtitle: String, onClick: () -> Unit = {}) {
    val colors = LocalNammaColors.current
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("🗺️", fontSize = 18.sp); Spacer(modifier = Modifier.width(16.dp)); Column { Text(title, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Medium); Text(subtitle, color = colors.subtext, fontSize = 12.sp) } }
        Spacer(modifier = Modifier.height(12.dp)); Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCategoryCard(icon: String, name: String, onClick: () -> Unit) {
    val colors = LocalNammaColors.current
    Surface(color = colors.input, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(70.dp, 80.dp), onClick = onClick) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) { Text(icon, fontSize = 28.sp); Spacer(modifier = Modifier.height(8.dp)); Text(name, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.Medium) } }
}