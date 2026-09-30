package com.example.data

import android.content.Context
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class FlavorPairingSuggestion(
    val pairingTitle: String,
    val spongeFlavor: String,
    val fillingFlavor: String,
    val frostingType: String,
    val tastingNotes: String,
    val recommendedColorsHex: List<String> = listOf("#FFF5E8", "#F4C2C2", "#E6C27A"),
    val designTip: String = ""
)

sealed class FlavorSommelierUiState {
    data object Idle : FlavorSommelierUiState()
    data object Loading : FlavorSommelierUiState()
    data class Success(
        val promptSummary: String,
        val suggestions: List<FlavorPairingSuggestion>,
        val statusNotice: String? = null
    ) : FlavorSommelierUiState()
    data class Error(val message: String) : FlavorSommelierUiState()
}

data class FirebaseSecurityStatus(
    val isClientKeyRemovedFromApk: Boolean = true,
    val isFirebaseProjectLinked: Boolean = false,
    val isAppCheckInstalled: Boolean = false,
    val activeAppCheckProvider: String = if (BuildConfig.DEBUG) {
        "DebugAppCheckProviderFactory (Debug Build)"
    } else {
        "PlayIntegrityAppCheckProviderFactory (Production Build)"
    },
    val aiBackendName: String = "Firebase AI Logic (GenerativeBackend.googleAI())",
    val modelName: String = "gemini-3.5-flash"
)

object FirebaseAiSecurityManager {
    @Volatile
    private var currentStatus = FirebaseSecurityStatus()

    fun getStatus(): FirebaseSecurityStatus = currentStatus

    /**
     * Initializes Firebase App & installs Firebase App Check (Play Integrity in production,
     * Debug Provider in debug builds) when google-services.json resources are present.
     * Never exposes or reads any client-side GEMINI_API_KEY from BuildConfig.
     */
    fun initializeProductionSecurity(context: Context): FirebaseSecurityStatus {
        return try {
            val appContext = context.applicationContext
            val existingApps = FirebaseApp.getApps(appContext)
            val firebaseApp = if (existingApps.isNotEmpty()) {
                FirebaseApp.getInstance()
            } else {
                val options = FirebaseOptions.fromResource(appContext)
                if (options != null && options.applicationId.isNotBlank()) {
                    FirebaseApp.initializeApp(appContext, options)
                } else {
                    null
                }
            }

            if (firebaseApp != null) {
                val appCheck = FirebaseAppCheck.getInstance(firebaseApp)
                val providerFactory = if (BuildConfig.DEBUG) {
                    DebugAppCheckProviderFactory.getInstance()
                } else {
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                }
                appCheck.installAppCheckProviderFactory(providerFactory)

                currentStatus = FirebaseSecurityStatus(
                    isClientKeyRemovedFromApk = true,
                    isFirebaseProjectLinked = true,
                    isAppCheckInstalled = true
                )
            } else {
                currentStatus = FirebaseSecurityStatus(
                    isClientKeyRemovedFromApk = true,
                    isFirebaseProjectLinked = false,
                    isAppCheckInstalled = false
                )
            }
            currentStatus
        } catch (_: Exception) {
            currentStatus = FirebaseSecurityStatus(
                isClientKeyRemovedFromApk = true,
                isFirebaseProjectLinked = false,
                isAppCheckInstalled = false
            )
            currentStatus
        }
    }
}

object GeminiFlavorSommelierClient {
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val flavorSuggestionsSchema = Schema.array(
        Schema.obj(
            mapOf(
                "pairingTitle" to Schema.string("Evocative name of the cake flavor combination."),
                "spongeFlavor" to Schema.string("The cake sponge flavor."),
                "fillingFlavor" to Schema.string("Complementary filling, curd, compote, or ganache."),
                "frostingType" to Schema.string("Complementary frosting or buttercream finish."),
                "tastingNotes" to Schema.string("Why these flavors balance each other."),
                "recommendedColorsHex" to Schema.array(
                    Schema.string("Three hex color strings starting with #.")
                ),
                "designTip" to Schema.string("Garnish or piping suggestion matching the flavor profile.")
            )
        )
    )

    fun parseFlavorSuggestionsJson(rawJson: String): List<FlavorPairingSuggestion> {
        val cleaned = rawJson.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        return json.decodeFromString<List<FlavorPairingSuggestion>>(cleaned)
    }

    suspend fun suggestComplementaryFlavors(
        ingredientsOrThemePrompt: String,
        eventType: String,
        dietaryTags: List<String>,
        inStockPantryNames: List<String>
    ): Result<List<FlavorPairingSuggestion>> = withContext(Dispatchers.IO) {
        val securityStatus = FirebaseAiSecurityManager.getStatus()
        if (!securityStatus.isFirebaseProjectLinked) {
            return@withContext Result.failure(
                IllegalStateException(
                    "Production Security Active (Firebase AI + App Check): Raw GEMINI_API_KEY exposure via BuildConfig is disabled. " +
                        "To complete live Firebase AI calls, add your Firebase project's app/google-services.json and enable Firebase AI Logic + App Check (Play Integrity) in the Firebase Console."
                )
            )
        }

        val dietaryLine = if (dietaryTags.isEmpty()) "None specified" else dietaryTags.joinToString(", ")
        val pantryLine = if (inStockPantryNames.isEmpty()) {
            "Standard artisan pastry pantry"
        } else {
            inStockPantryNames.take(8).joinToString(", ")
        }

        val prompt = """
            Suggest 3 distinct, complementary artisan cake flavor combinations for a new home bakery cake order.
            
            User-provided ingredients, notes, or theme: "$ingredientsOrThemePrompt"
            Celebration / Event type: "$eventType"
            Dietary requirements: "$dietaryLine"
            Available pantry ingredients on hand: "$pantryLine"
            
            For each combination, provide:
            - pairingTitle: An evocative pastry-chef title for the pairing.
            - spongeFlavor: The cake sponge flavor (e.g., Brown Butter Almond, Roasted Pistachio, Earl Grey Chiffon).
            - fillingFlavor: Complementary compote, curd, ganache, or praline filling.
            - frostingType: Complementary buttercream or ganache finish.
            - tastingNotes: 1-2 sentences explaining why the acidity, sweetness, and aroma balance each other.
            - recommendedColorsHex: 3 hex color codes (starting with #) matching the theme and flavors.
            - designTip: A concise piping, floral, or garnish suggestion for the home baker.
        """.trimIndent()

        try {
            val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = MODEL_NAME,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    responseSchema = flavorSuggestionsSchema
                    temperature = 0.75f
                },
                systemInstruction = content {
                    text("You are an executive pastry chef and cake flavor sommelier for an artisan home bakery.")
                }
            )

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text
                ?: throw IllegalStateException("Empty response from Firebase AI.")
            val parsed = parseFlavorSuggestionsJson(responseText)
            if (parsed.isEmpty()) {
                Result.failure(IllegalStateException("No flavor combinations returned."))
            } else {
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
