package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FirebaseSecurityStatus

data class PlayConsoleAssetSpec(
    val title: String,
    val dimensions: String,
    val formatAndLimit: String,
    val designGuidance: String
)

data class ScreenshotStoryboardItem(
    val slotNumber: Int,
    val screenTitle: String,
    val headlineOverlay: String,
    val keyFeaturesShown: String
)

object PlayStoreLaunchGuideCatalog {
    const val APP_NAME = "Crumb & Tier"
    const val APPLICATION_ID = "com.aistudio.crumbtier.p8k4wm"
    const val SHORT_DESCRIPTION =
        "Artisan cake order planner: tier servings, recipes, bake schedules & payments."

    val graphicAssetSpecs: List<PlayConsoleAssetSpec> = listOf(
        PlayConsoleAssetSpec(
            title = "High-Res Google Play Store Icon",
            dimensions = "512 × 512 px",
            formatAndLimit = "32-bit PNG (with alpha) • Max 1,024 KB",
            designGuidance = "Export a full-bleed square version of @drawable/img_app_icon on the #8C3B2B terracotta background. Do not pre-round corners or add drop shadows—Google Play dynamically applies a 20% corner mask and shadow."
        ),
        PlayConsoleAssetSpec(
            title = "Feature Graphic Banner",
            dimensions = "1024 × 500 px",
            formatAndLimit = "JPEG or 24-bit PNG (no alpha) • Max 15 MB",
            designGuidance = "Use the warm pâtisserie hero photo (@drawable/img_hero_patisserie) on the right with 'Crumb & Tier — Home Baker Order & Tier Studio' set in Playfair Display on a warm espresso-terracotta gradient on the left. Keep text inside the center 80% safe zone."
        ),
        PlayConsoleAssetSpec(
            title = "Phone Screenshots (2–8 Required, 5–6 Recommended)",
            dimensions = "1080 × 1920 px (9:16 portrait)",
            formatAndLimit = "JPEG or 24-bit PNG • Min 320 px, Max 3840 px",
            designGuidance = "Capture real app screens in the Streaming Emulator with seeded cake orders, customer profiles, and tier blueprints."
        ),
        PlayConsoleAssetSpec(
            title = "7-inch & 10-inch Tablet Screenshots (At least 4 for Featured Placement)",
            dimensions = "1600 × 2560 px or 1920 × 1200 px (16:9 / 9:16)",
            formatAndLimit = "JPEG or 24-bit PNG (no alpha)",
            designGuidance = "Capture Crumb & Tier on an Expanded tablet viewport (>= 600dp) to showcase the adaptive Material 3 NavigationRail and multi-column layouts."
        )
    )

    val screenshotStoryboard: List<ScreenshotStoryboardItem> = listOf(
        ScreenshotStoryboardItem(
            slotNumber = 1,
            screenTitle = "Orders Board & Monthly Revenue Chart",
            headlineOverlay = "Plan Every Custom Cake Order from Inquiry to Delivery",
            keyFeaturesShown = "Patisserie Hero Header, Active Cake KPIs, Monthly Completed vs. Pipeline Revenue Bar Chart, and Status Filter Chips."
        ),
        ScreenshotStoryboardItem(
            slotNumber = 2,
            screenTitle = "Interactive Tier Studio & Stacked Blueprint",
            headlineOverlay = "Precision Tier Servings, Dowels & Batter Scaling",
            keyFeaturesShown = "Proportional Stacked Cake Blueprint Canvas, Round/Heart/Square/Hexagon Shapes, Wedding vs. Party Cut Toggle, and Baker's Scaling Breakdown."
        ),
        ScreenshotStoryboardItem(
            slotNumber = 3,
            screenTitle = "Cake Design Editor with Voice Dictation & Flavor Sommelier",
            headlineOverlay = "Hands-Free Voice Notes & AI Pastry Flavor Pairings",
            keyFeaturesShown = "Voice-to-Text Design Note Dictation with [Design Spec] / [Client Feedback] tags and Firebase AI Flavor Sommelier."
        ),
        ScreenshotStoryboardItem(
            slotNumber = 4,
            screenTitle = "Customer CRM & Past Order History",
            headlineOverlay = "Remember Client Preferences, Dietary Tags & Order History",
            keyFeaturesShown = "Customer Profile Cards, Repeat Client Badges, Lifetime Value Totals, and 1-Tap Rebooking."
        ),
        ScreenshotStoryboardItem(
            slotNumber = 5,
            screenTitle = "Ingredient Pantry & Comprehensive Order Summary",
            headlineOverlay = "Track Stock Reorder Alerts & Share Client Order Invoices",
            keyFeaturesShown = "Pantry Stock Progress Bars, Low-Stock Alerts, Auto-Recipe Allocation, and Printable/Shareable Order Summary Sheet."
        )
    )

    fun generateStoreListingCopy(): String = """
        APP TITLE (12 / 30 chars):
        Crumb & Tier

        SHORT DESCRIPTION (78 / 80 chars):
        $SHORT_DESCRIPTION

        FULL DESCRIPTION:
        Crumb & Tier is an all-in-one cake order planner and production studio built specifically for artisan home bakers, cottage bakeries, and custom cake designers.

        KEY FEATURES:
        • Custom Cake Order Board: Record cake designs, sponge flavors, compote fillings, buttercream finishes, color palettes, inscriptions, and reference photos.
        • Interactive Tier Serving Studio: Visualize multi-tier cakes in real time with wedding (1"×2") and party (1.5"×2") slice calculations, dowel rod counts, base board diameters, and batter/buttercream weight scaling.
        • Hands-Free Voice-to-Text Notes: Dictate piping instructions, allergy alerts, and client feedback while your hands are covered in flour or buttercream.
        • Complementary Flavor Sommelier (Secured by Firebase AI & App Check): Discover balanced sponge, filling, and frosting combinations tailored to seasonal ingredients and event themes.
        • Customer Profiles & Order History: Store client contact details, delivery addresses, dietary notes, and lifetime order history.
        • Ingredient Pantry & Usage Tracking: Monitor stock levels, receive low-stock reorder alerts, and deduct recipe ingredients per cake order.
        • Delivery Schedule, Payments Ledger & Revenue Analytics: Track deposits, remaining balances, bake prep checklists, and 6-month revenue trends.
    """.trimIndent()

    fun generatePrivacyPolicyTemplate(developerOrBakeryName: String = "Crumb & Tier Studio"): String = """
        # Privacy Policy for Crumb & Tier
        **Effective Date:** September 26, 2026
        **Application Package ID:** $APPLICATION_ID
        **Developer / Entity:** $developerOrBakeryName

        ## 1. Overview
        Crumb & Tier ("the App") is a cake order planning, tier calculation, and bakery inventory application for home bakers. We respect your privacy and are committed to protecting the information you record within the App.

        ## 2. Data Stored Locally on Your Device
        All core bakery records—including cake orders, tier specifications, customer profiles (client names, phone numbers, email addresses, delivery addresses, and order history), ingredient inventory levels, and payment ledger balances—are stored locally on your device inside an encrypted/sandboxed Android Room (SQLite) database.
        - We do **not** sell, rent, or share your customer lists or financial records with third-party advertisers.
        - You retain full ownership and control over all records and may edit or delete any customer profile, order, or ingredient at any time inside the App.

        ## 3. Permissions & Device Features Used
        - **Microphone (`android.permission.RECORD_AUDIO` — Optional):** Used exclusively when you tap the "Dictate Note" button on the Cake Design or Tier Studio screens to transcribe spoken design notes into text using Android's on-device/system speech recognizer. Audio is never recorded in the background.
        - **Photos & Media (Android System Photo Picker):** When you attach a cake reference photo to an order, the App uses the zero-permission Android System Photo Picker (`PickVisualMedia`). The App only accesses the specific photo URI you select and never scans your photo library.
        - **Internet (`android.permission.INTERNET`):** Used solely when you request complementary cake flavor pairings via Firebase AI Logic and when Firebase App Check verifies app authenticity via the Google Play Integrity API.

        ## 4. Firebase AI Logic & Firebase App Check Security
        When you optionally use the Flavor Pairing Sommelier feature, the ingredient or theme prompt you enter is transmitted over HTTPS (TLS) to Google Firebase AI Logic (`gemini-3.5-flash`) to generate flavor suggestions.
        - Do not include personally identifiable client information in flavor prompts.
        - Requests are protected by **Firebase App Check** using the **Google Play Integrity API** to verify that requests originate from an authentic, unmodified installation of Crumb & Tier.

        ## 5. Data Retention & Deletion
        Because your orders, customer profiles, and pantry inventory are stored locally on your device, deleting an item inside the App or uninstalling Crumb & Tier permanently removes all local database records from your device.

        ## 6. Children's Privacy
        Crumb & Tier is a business productivity tool intended for adult home bakers and pastry professionals and is not directed at children under the age of 13.

        ## 7. Contact Us
        If you have questions about this Privacy Policy or data practices, please contact $developerOrBakeryName via the support email listed on the Google Play Store listing page.
    """.trimIndent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayStoreLaunchGuideModalSheet(
    securityStatus: FirebaseSecurityStatus,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf(
        "1. Firebase AI Security",
        "2. Store Assets & Shots",
        "3. Privacy Policy & Safety"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .testTag("play_store_launch_guide_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column {
                    Text(
                        text = "PRODUCTION SECURITY & PLAY CONSOLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Crumb & Tier Launch Guide",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        label = { Text(label) },
                        modifier = Modifier.testTag("launch_guide_tab_$index")
                    )
                }
            }

            when (selectedSection) {
                0 -> FirebaseSecuritySection(securityStatus = securityStatus)
                1 -> StoreAssetsAndScreenshotsSection(context = context)
                2 -> PrivacyPolicyAndDataSafetySection(context = context)
            }
        }
    }
}

@Composable
private fun FirebaseSecuritySection(securityStatus: FirebaseSecurityStatus) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Production Firebase AI + App Check Hardening",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            SecurityCheckRow(
                label = "Client API Key Purged from APK",
                value = if (securityStatus.isClientKeyRemovedFromApk) {
                    "Secured — No GEMINI_API_KEY in BuildConfig"
                } else {
                    "Action Needed"
                }
            )
            SecurityCheckRow(
                label = "AI SDK Provider",
                value = "${securityStatus.aiBackendName} (${securityStatus.modelName})"
            )
            SecurityCheckRow(
                label = "App Check Attestation",
                value = securityStatus.activeAppCheckProvider
            )
            SecurityCheckRow(
                label = "Firebase Config (google-services.json)",
                value = if (securityStatus.isFirebaseProjectLinked) {
                    "Linked & Active"
                } else {
                    "Ready — Add app/google-services.json before release"
                }
            )
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Final Firebase Console Checklist for Production",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "1. In Firebase Console, create/select your project and register Android package '${PlayStoreLaunchGuideCatalog.APPLICATION_ID}'.\n" +
                    "2. Download google-services.json and place it at app/google-services.json.\n" +
                    "3. Open 'Firebase AI Logic' in the Firebase Console and enable the Gemini Developer API provider.\n" +
                    "4. Open 'App Check' in the Firebase Console, register your Play App Signing SHA-256 certificate fingerprint under Play Integrity, and click 'Enforce' for Firebase AI Logic.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SecurityCheckRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.45f)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E6F40),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.55f)
        )
    }
}

@Composable
private fun StoreAssetsAndScreenshotsSection(context: Context) {
    Text(
        text = "Required Google Play Store Graphic Assets",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    PlayStoreLaunchGuideCatalog.graphicAssetSpecs.forEach { spec ->
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = spec.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = spec.dimensions,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = spec.formatAndLimit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = spec.designGuidance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    HorizontalDivider()

    Text(
        text = "Recommended 5-Screen Store Listing Storyboard",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    PlayStoreLaunchGuideCatalog.screenshotStoryboard.forEach { shot ->
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Screenshot #${shot.slotNumber}: ${shot.screenTitle}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Caption Banner: “${shot.headlineOverlay}”",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = shot.keyFeaturesShown,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Button(
        onClick = {
            copyToClipboard(
                context = context,
                label = "Crumb & Tier Store Listing Copy",
                text = PlayStoreLaunchGuideCatalog.generateStoreListingCopy()
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("copy_store_listing_button")
    ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Copy Store Listing Title & Descriptions")
    }
}

@Composable
private fun PrivacyPolicyAndDataSafetySection(context: Context) {
    val policyText = remember { PlayStoreLaunchGuideCatalog.generatePrivacyPolicyTemplate() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Policy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Google Play Data Safety Form Quick-Reference",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "• Local Room Data (Orders, Clients, Pantry): Stored on-device only; not transmitted off-device.\n" +
                    "• Audio (`RECORD_AUDIO`): Processed ephemerally via system SpeechRecognizer only when the user taps Dictate.\n" +
                    "• Photos (`PickVisualMedia`): Zero-permission Android Photo Picker; no broad storage permission requested.\n" +
                    "• AI Prompts (`INTERNET`): Sent over TLS to Firebase AI Logic protected by Play Integrity App Check.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Privacy Policy Template (Ready to Host)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = policyText,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = {
                copyToClipboard(
                    context = context,
                    label = "Crumb & Tier Privacy Policy",
                    text = policyText
                )
            },
            modifier = Modifier
                .weight(1f)
                .testTag("copy_privacy_policy_button")
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy Policy")
        }

        Button(
            onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Crumb & Tier - Privacy Policy")
                    putExtra(Intent.EXTRA_TEXT, policyText)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Export Privacy Policy"))
            },
            modifier = Modifier
                .weight(1f)
                .testTag("share_privacy_policy_button")
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export Policy")
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}
