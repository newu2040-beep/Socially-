package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Album
import com.example.data.SavedPhoto
import kotlinx.coroutines.launch

@Composable
fun AestheticPermissionsDialog(
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SociallyTheme.PageBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = SociallyTheme.BrandBrown,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Aesthetic Clearances",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        color = SociallyTheme.TextMain
                    )
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "To unlock the full potential of Analog Lab Studio, please permit the following core coordinates:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SociallyTheme.TextMuted,
                        lineHeight = 18.sp
                    )
                )

                // Gallery description
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SociallyTheme.CardBeige, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = SociallyTheme.BrandBrown,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Full Gallery Access",
                            fontWeight = FontWeight.Bold,
                            color = SociallyTheme.TextMain,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Required to seamlessly load your raw camera negatives, apply customized film grain algorithms, and map layout corrections.",
                            color = SociallyTheme.TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Push notifications description
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SociallyTheme.CardBeige, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notifications",
                        tint = SociallyTheme.BrandBrown,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Push Notifications",
                            fontWeight = FontWeight.Bold,
                            color = SociallyTheme.TextMain,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Alerts you instantly when Gemini finishes drafting your premium Instagram copy or when your photo vault completes cloud synchronization.",
                            color = SociallyTheme.TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRequestPermissions,
                colors = ButtonDefaults.buttonColors(containerColor = SociallyTheme.BrandBrown),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Grant Clearances",
                    color = SociallyTheme.PageBg,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Maybe Later",
                    color = SociallyTheme.TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SociallyAppContent(viewModel: SociallyViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

    var showPermissionsDialog by remember { mutableStateOf(false) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Check standard gallery and push permissions
        val hasGallery = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            results[android.Manifest.permission.READ_MEDIA_IMAGES] == true
        } else {
            results[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        val hasNotifications = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            results[android.Manifest.permission.POST_NOTIFICATIONS] == true
        } else {
            true
        }
        showPermissionsDialog = !(hasGallery && hasNotifications)
    }

    LaunchedEffect(Unit) {
        val hasGallery = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        val hasNotifications = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!hasGallery || !hasNotifications) {
            showPermissionsDialog = true
        }
    }

    if (showPermissionsDialog) {
        AestheticPermissionsDialog(
            onDismiss = { showPermissionsDialog = false },
            onRequestPermissions = {
                val list = mutableListOf<String>()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    list.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                    list.add(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    list.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permissionsLauncher.launch(list.toTypedArray())
            }
        )
    }

    // File Picker for custom user images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.selectPresetOrUpload(it.toString())
        }
    }

    val pickImageWithPermissionCheck = {
        val hasGallery = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (hasGallery) {
            photoPickerLauncher.launch("image/*")
        } else {
            showPermissionsDialog = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SociallyTheme.MatteCharcoal,
        topBar = {
            SociallyTopAppBar(
                pendingBackups = pendingCount,
                isSyncing = isCloudSyncing,
                onSyncClick = { viewModel.runCloudStorageSync() },
                onChatNav = { viewModel.navigateTo(Screen.ChatBot) },
                onUploadClick = pickImageWithPermissionCheck
            )
        },
        bottomBar = {
            SociallyBottomNavBar(
                currentScreen = currentScreen,
                onTabSelected = { target ->
                    if (target == Screen.EditStudio && viewModel.workingUri.value.isEmpty()) {
                        // Default to Tokyo preset if user presses edit empty
                        viewModel.selectPresetOrUpload(viewModel.presets.first().imageUrl)
                    } else {
                        viewModel.navigateTo(target)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    Screen.Gallery -> GalleryScreen(
                        viewModel = viewModel,
                        onPickImage = pickImageWithPermissionCheck,
                        onRequestPermissionsTrigger = { showPermissionsDialog = true }
                    )
                    Screen.EditStudio -> EditStudioScreen(viewModel)
                    Screen.Albums -> AlbumsScreen(viewModel)
                    Screen.ChatBot -> ChatBotScreen(viewModel)
                    Screen.Detail -> PhotoDetailScreen(viewModel)
                }
            }
        }
    }
}

// --- APP BAR ---
@Composable
fun SociallyTopAppBar(
    pendingBackups: Int,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    onChatNav: () -> Unit,
    onUploadClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SociallyTheme.MatteCharcoal,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "S O C I A L L Y",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        color = SociallyTheme.BrandBrown,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = "ANALOG LAB STUDIO",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SociallyTheme.TextMuted.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Cloud backup indicator button
                TactileClickable(
                    onClick = onSyncClick,
                    enabled = !isSyncing,
                    testTag = "cloud_sync_icon_button"
                ) { isPressed ->
                    Row(
                        modifier = Modifier
                            .background(
                                if (pendingBackups > 0) SociallyTheme.Terracotta.copy(alpha = 0.15f)
                                else SociallyTheme.BadgeCream,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (pendingBackups > 0) SociallyTheme.Terracotta.copy(alpha = 0.3f)
                                else SociallyTheme.BorderTan,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudQueue,
                            contentDescription = "Sync Indicator",
                            tint = if (isSyncing) SociallyTheme.BrandBrown else if (pendingBackups > 0) SociallyTheme.Terracotta else SociallyTheme.TextMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .drawBehind {
                                    if (isSyncing) {
                                        // Spin effect is done by engine, we animate statically
                                    }
                                }
                        )
                        if (isSyncing) {
                            Text("Syncing...", fontSize = 10.sp, color = SociallyTheme.BrandBrown, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = if (pendingBackups > 0) "$pendingBackups rolls" else "Synced",
                                fontSize = 10.sp,
                                color = if (pendingBackups > 0) SociallyTheme.Terracotta else SociallyTheme.TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Upload trigger icon
                IconButton(
                    onClick = onUploadClick,
                    modifier = Modifier
                        .background(SociallyTheme.BadgeCream, shape = CircleShape)
                        .border(1.dp, SociallyTheme.BorderTan, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Upload Photo",
                        tint = SociallyTheme.BrandBrown,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // AI chat toggle badge
                IconButton(
                    onClick = onChatNav,
                    modifier = Modifier
                        .background(SociallyTheme.BadgeCream, shape = CircleShape)
                        .border(1.dp, SociallyTheme.BorderTan, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Companion",
                        tint = SociallyTheme.BrandBrown,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- BOTTOM NAVBAR ---
@Composable
fun SociallyBottomNavBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        color = SociallyTheme.PageBg,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, SociallyTheme.BorderBeige)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(64.dp)
        ) {
            val items = listOf(
                Triple(Screen.Gallery, Icons.Default.FilterFrames, "Gallery"),
                Triple(Screen.EditStudio, Icons.Default.Tune, "Studio"),
                Triple(Screen.Albums, Icons.Default.FolderSpecial, "Albums"),
                Triple(Screen.ChatBot, Icons.Default.Forum, "AI Chat")
            )

            items.forEach { (screen, icon, label) ->
                val selected = currentScreen == screen || (screen == Screen.Gallery && currentScreen == Screen.Detail)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(screen) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SociallyTheme.PageBg,
                        selectedTextColor = SociallyTheme.BrandBrown,
                        indicatorColor = SociallyTheme.BrandBrown,
                        unselectedIconColor = SociallyTheme.TextMuted.copy(alpha = 0.5f),
                        unselectedTextColor = SociallyTheme.TextMuted.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

// ==========================================
// SCREEN 1: GALLERY BACKSTORE & PRESETS
// ==========================================
@Composable
fun GalleryScreen(
    viewModel: SociallyViewModel,
    onPickImage: () -> Unit,
    onRequestPermissionsTrigger: () -> Unit
) {
    val context = LocalContext.current
    val savedPhotos by viewModel.photosFlow.collectAsStateWithLifecycle()

    val hasGallery = remember(context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    val hasNotifications = remember(context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (!hasGallery || !hasNotifications) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = SociallyTheme.CardBeige),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SociallyTheme.BorderBeige)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SociallyTheme.BrandBrown.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SociallyTheme.BrandBrown,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Permissions recommended",
                                fontWeight = FontWeight.Bold,
                                color = SociallyTheme.TextMain,
                                fontSize = 12.sp
                            )
                            val missingText = when {
                                !hasGallery && !hasNotifications -> "Please enable gallery access and push notifications for full studio support."
                                !hasGallery -> "Please enable gallery access to pick local photo rolls."
                                else -> "Please enable notifications to receive sync completion updates."
                            }
                            Text(
                                missingText,
                                color = SociallyTheme.TextMuted,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }

                        Button(
                            onClick = onRequestPermissionsTrigger,
                            colors = ButtonDefaults.buttonColors(containerColor = SociallyTheme.BrandBrown),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Enable", color = SociallyTheme.PageBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            // GORGEOUS HERO HEADER WITH 3D DEPTH
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SociallyTheme.CardSlate, SociallyTheme.BadgeCream),
                            start = Offset(0f, 0f),
                            end = Offset(400f, 400f)
                        )
                    )
                    .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                // Background depth decal
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    tint = SociallyTheme.BrandBrown.copy(alpha = 0.05f),
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp, y = 10.dp)
                )

                Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Develop Nostalgia",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = SociallyTheme.TextMain
                        )
                    )
                    Text(
                        text = "Upload custom images or try dynamic preloaded Fuji simulation negatives below.",
                        color = SociallyTheme.TextMuted.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp, end = 20.dp)
                    )
                }
            }
        }

        // QUICK ACTION BUTTONS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pick custom photo button
                Button(
                    onClick = onPickImage,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pick_custom_photo_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SociallyTheme.GoldBrass),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = SociallyTheme.PageBg)
                        Text("Pick Custom Photo", color = SociallyTheme.PageBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // AI Studio chat trigger
                Button(
                    onClick = { viewModel.navigateTo(Screen.ChatBot) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SociallyTheme.BadgeCream),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SociallyTheme.BorderTan)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SociallyTheme.BrandBrown)
                        Text("AI Copilot", color = SociallyTheme.TextMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // PRESET SAMPLES GRID Header
        item {
            Column {
                Text(
                    text = "Retro Negative Filmstrips",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = SociallyTheme.GoldBrass
                    )
                )
                Text(
                    "High resolution landscape and street motifs loaded immediately in the developer sandbox.",
                    color = SociallyTheme.TextMuted.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // PRESETS ROW HORIZONTAL
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(viewModel.presets) { preset ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { viewModel.selectPresetOrUpload(preset.imageUrl) }
                            .testTag("preset_card_${preset.id}"),
                        colors = CardDefaults.cardColors(containerColor = SociallyTheme.CardSlate),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column {
                            Box {
                                AsyncImage(
                                    model = preset.imageUrl,
                                    contentDescription = preset.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                // Fuji emblem tag overlay
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Text("ISO 400", color = SociallyTheme.GoldBrass, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    preset.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SociallyTheme.TextMain,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    preset.description,
                                    fontSize = 10.sp,
                                    color = SociallyTheme.TextMuted.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // DEVELOPED PHOTO ROLLS (SAVED FROM ROOM DB)
        item {
            Column {
                Text(
                    text = "Saved Developed Negatives (${savedPhotos.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = SociallyTheme.TextMain
                    )
                )
                Text(
                    "Your local analog lab chamber rolls, secured offline in SQLite with cloud backup triggers.",
                    color = SociallyTheme.TextMuted.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        if (savedPhotos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp)
                        .background(SociallyTheme.CardSlate, shape = RoundedCornerShape(16.dp))
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = "Empty",
                            tint = SociallyTheme.BrandBrown.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "No Developed Negatives Yet",
                            fontWeight = FontWeight.Bold,
                            color = SociallyTheme.TextMain,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Text(
                            "Pick a preset or custom photo above, apply custom vintage grains or fine control dials, and hit save to record it!",
                            color = SociallyTheme.TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp)
                        )
                    }
                }
            }
        } else {
            // Display Grid of Saved photos
            item {
                val gridHeight = if (savedPhotos.size <= 2) 130.dp else if (savedPhotos.size <= 4) 260.dp else 400.dp
                Box(modifier = Modifier.height(gridHeight)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(savedPhotos) { photo ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.loadPhotoForDetail(photo) }
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                            ) {
                                // Load original or edited visual representation
                                AsyncImage(
                                    model = photo.fileUri,
                                    contentDescription = photo.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    colorFilter = createCombinedMatrix(
                                        filterName = photo.filterName,
                                        brightness = photo.brightness,
                                        contrast = photo.contrast,
                                        saturation = photo.saturation,
                                        warmth = photo.warmth
                                    )
                                )

                                // Text banner
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                photo.title,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                photo.filterName,
                                                color = SociallyTheme.GoldBrass,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Backup status badge
                                        Icon(
                                            imageVector = if (photo.syncStatus == "Synced") Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                            contentDescription = photo.syncStatus,
                                            tint = if (photo.syncStatus == "Synced") SociallyTheme.TealCyan else SociallyTheme.Terracotta,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ==========================================
// SCREEN 2: PHOTO EDITING LAB STUDIO
// ==========================================
@Composable
fun EditStudioScreen(viewModel: SociallyViewModel) {
    val context = LocalContext.current
    val workingUri by viewModel.workingUri.collectAsStateWithLifecycle()
    val workingTitle by viewModel.workingTitle.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val brightness by viewModel.brightness.collectAsStateWithLifecycle()
    val contrast by viewModel.contrast.collectAsStateWithLifecycle()
    val saturation by viewModel.saturation.collectAsStateWithLifecycle()
    val warmth by viewModel.warmth.collectAsStateWithLifecycle()
    val vignette by viewModel.vignette.collectAsStateWithLifecycle()
    val sharpness by viewModel.sharpness.collectAsStateWithLifecycle()

    val generatedCaption by viewModel.generatedCaption.collectAsStateWithLifecycle()
    val isGeneratingCaption by viewModel.isGeneratingCaption.collectAsStateWithLifecycle()
    val albums by viewModel.albumsFlow.collectAsStateWithLifecycle()

    var showAlbumSaveDialog by remember { mutableStateOf(false) }
    var selectedAlbumIdForSave by remember { mutableStateOf<Int?>(null) }

    // Dynamic Cropping bounding box overlay state
    var selectedAspectCrop by remember { mutableStateOf("Full") }

    val filters = listOf("Normal", "Classic Chrome", "Superia X-TRA", "Velvia", "Astia", "Provia", "Acros")

    if (workingUri.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Select or Pick a Photo from the Gallery to start crafting in our Studio chamber.", color = SociallyTheme.TextMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        // IMAGE CONTROLLER (3D Perspectives)
        Text(
            text = "3D PERSPECTIVE INTERACTION",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = SociallyTheme.GoldBrass,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Interactive3DTiltCard(
                testTag = "studio_3d_card"
            ) { tiltX, tiltY ->
                val aspectModifier = when (selectedAspectCrop) {
                    "1:1" -> Modifier.aspectRatio(1f)
                    "4:5" -> Modifier.aspectRatio(0.8f)
                    "16:9" -> Modifier.aspectRatio(1.77f)
                    else -> Modifier.fillMaxSize()
                }

                // Photo load with applied custom ColorMatrix filters
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = workingUri,
                        contentDescription = "Working negative",
                        modifier = aspectModifier,
                        contentScale = ContentScale.Crop,
                        colorFilter = createCombinedMatrix(
                            filterName = selectedFilter,
                            brightness = brightness,
                            contrast = contrast,
                            saturation = saturation,
                            warmth = warmth
                        )
                    )

                    // Sharpness feedback filter overlay
                    if (sharpness > 0.1f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = sharpness * 0.04f))
                        )
                    }

                    // Vignette edge shading simulation!
                    if (vignette > 0.05f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    val grad = Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = vignette * 0.9f)),
                                        center = Offset(size.width / 2, size.height / 2),
                                        radius = size.width * 0.75f
                                    )
                                    drawRect(brush = grad)
                                }
                        )
                    }

                    // Drag tip trigger
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Gesture, contentDescription = null, tint = SociallyTheme.GoldBrass, modifier = Modifier.size(10.dp))
                            Text("DRAG TO TILT 3D", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CROP ASPECT SLIDER KEYS
        Text(
            text = "CHAMBER BOUNDARY (CROP RATIO)",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = SociallyTheme.TextMuted.copy(alpha = 0.8f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val aspectRatios = listOf("Full", "1:1", "4:5", "16:9")
            items(aspectRatios) { ratio ->
                FilterChip(
                    selected = selectedAspectCrop == ratio,
                    onClick = { selectedAspectCrop = ratio },
                    label = { Text(ratio, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SociallyTheme.GoldBrass,
                        selectedLabelColor = SociallyTheme.PageBg,
                        containerColor = SociallyTheme.BadgeCream,
                        labelColor = SociallyTheme.TextMuted
                    ),
                    border = BorderStroke(1.dp, SociallyTheme.BorderTan)
                )
            }
        }

        // FUJI SIMULATION EMULSIONS CHIPS row
        Text(
            text = "FUJI EMULSION FILM CHROMES",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = SociallyTheme.GoldBrass,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filters) { fl ->
                val active = selectedFilter == fl
                Box(
                    modifier = Modifier
                        .background(
                            if (active) SociallyTheme.GoldBrass else SociallyTheme.BadgeCream,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(1.dp, SociallyTheme.BorderTan, RoundedCornerShape(10.dp))
                        .clickable { viewModel.setFilter(fl) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_chip_$fl")
                ) {
                    Text(
                        text = fl,
                        color = if (active) SociallyTheme.PageBg else SociallyTheme.TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // PHYSICAL CAMERA CORRECTION SLIDER KNOBS
        Text(
            text = "MANUAL LOG CHAMBER CORRECTIONS",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = SociallyTheme.TextMuted.copy(alpha = 0.8f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp)
        )

        // SLIDER SETS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(SociallyTheme.CardSlate, shape = RoundedCornerShape(16.dp))
                .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brightness
            StudioDialSlider(
                label = "EV Exposure (Brightness)",
                value = brightness,
                valueRange = -0.5f..0.5f,
                formattedValue = String.format("%.2f EV", brightness),
                onValueChange = { viewModel.setBrightness(it) }
            )

            // Contrast
            StudioDialSlider(
                label = "Analog Contrast",
                value = contrast,
                valueRange = 0.6f..1.4f,
                formattedValue = String.format("%.2f", contrast),
                onValueChange = { viewModel.setContrast(it) }
            )

            // Saturation
            StudioDialSlider(
                label = "Chroma Saturation",
                value = saturation,
                valueRange = 0.0f..1.8f,
                formattedValue = String.format("%.2f", saturation),
                onValueChange = { viewModel.setSaturation(it) }
            )

            // Warmth
            StudioDialSlider(
                label = "Kelvin Warmth (Yellow balance)",
                value = warmth,
                valueRange = -0.4f..0.4f,
                formattedValue = String.format("%.2f K", warmth * 1000f),
                onValueChange = { viewModel.setWarmth(it) }
            )

            // Vignette
            StudioDialSlider(
                label = "Draped Vignette Shadows",
                value = vignette,
                valueRange = 0f..1f,
                formattedValue = String.format("%d %%", (vignette * 100f).toInt()),
                onValueChange = { viewModel.setVignette(it) }
            )

            // Sharpness
            StudioDialSlider(
                label = "Micro-contrast (Sharpening)",
                value = sharpness,
                valueRange = 0f..1f,
                formattedValue = String.format("%d %%", (sharpness * 100f).toInt()),
                onValueChange = { viewModel.setSharpness(it) }
            )
        }

        // ==========================================
        // AI CAPTION GENERATOR SECTION
        // ==========================================
        Text(
            text = "AI ARTISTIC INSTAGRAM CAPTION ENGINE",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = SociallyTheme.BrandBrown,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(SociallyTheme.CardSlate, shape = RoundedCornerShape(16.dp))
                .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SociallyTheme.BrandBrown, modifier = Modifier.size(16.dp))
                    Text("Tailor Aesthetic Script", color = SociallyTheme.TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { viewModel.triggerAICaptionGeneration(context) },
                    enabled = !isGeneratingCaption,
                    colors = ButtonDefaults.buttonColors(containerColor = SociallyTheme.GoldBrass),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("trigger_caption_btn")
                ) {
                    if (isGeneratingCaption) {
                        CircularProgressIndicator(color = SociallyTheme.PageBg, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                    } else {
                        Text("Draft AI Capture", color = SociallyTheme.PageBg, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            if (generatedCaption.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SociallyTheme.BadgeCream, shape = RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = generatedCaption,
                            color = SociallyTheme.TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val clipboard = LocalClipboardManager.current
                        Row(
                            modifier = Modifier
                                .background(SociallyTheme.BorderBeige, shape = RoundedCornerShape(6.dp))
                                .clickable { clipboard.setText(AnnotatedString(generatedCaption)) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SociallyTheme.BrandBrown, modifier = Modifier.size(12.dp))
                            Text("Copy caption block", color = SociallyTheme.BrandBrown, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    "Hit draft to let Gemini analyze your photo color tones, elements, and selected movie filter simulation to yield customized, premium posts.",
                    color = SociallyTheme.TextMuted.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FINAL ACTIONS: SAVE NEGATIVE ROLL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            var inputTitleText by remember { mutableStateOf(workingTitle) }

            OutlinedTextField(
                value = inputTitleText,
                onValueChange = {
                    inputTitleText = it
                    viewModel.setWorkingTitle(it)
                },
                label = { Text("Negative Film Entry Label", color = SociallyTheme.TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SociallyTheme.BrandBrown,
                    unfocusedBorderColor = SociallyTheme.BorderTan,
                    focusedTextColor = SociallyTheme.TextMain,
                    unfocusedTextColor = SociallyTheme.TextMuted,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("photo_label_input")
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        viewModel.setWorkingTitle(inputTitleText)
                        showAlbumSaveDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_to_chamber_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SociallyTheme.GoldBrass),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save to Lab Chamber", color = SociallyTheme.PageBg, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // --- DIALOG FOR SELECTING ALBUM FOR SAVING ---
    if (showAlbumSaveDialog) {
        AlertDialog(
            onDismissRequest = { showAlbumSaveDialog = false },
            containerColor = SociallyTheme.CardSlate,
            title = {
                Text(
                    "Select Developer Chamber",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontFamily = FontFamily.Serif)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a folder to record your retro negative, or save uncategorized:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)

                    // Standard Unassigned choice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedAlbumIdForSave == null) SociallyTheme.GoldBrass.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedAlbumIdForSave = null }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedAlbumIdForSave == null, onClick = { selectedAlbumIdForSave = null })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("General Darkroom Drawer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Map folder albums
                    albums.forEach { album ->
                        Row(
                            modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            if (selectedAlbumIdForSave == album.id) SociallyTheme.GoldBrass.copy(alpha = 0.15f) else Color.Transparent,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                .clickable { selectedAlbumIdForSave = album.id }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedAlbumIdForSave == album.id, onClick = { selectedAlbumIdForSave = album.id })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(album.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.savePhotoToAlbum(selectedAlbumIdForSave, workingTitle, context) {
                            showAlbumSaveDialog = false
                            viewModel.navigateTo(Screen.Gallery)
                        }
                    },
                    modifier = Modifier.testTag("confirm_album_save_btn")
                ) {
                    Text("Record negative", color = SociallyTheme.GoldBrass, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlbumSaveDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}

@Composable
fun StudioDialSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    formattedValue: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = SociallyTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(formattedValue, color = SociallyTheme.BrandBrown, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = SociallyTheme.BrandBrown,
                activeTrackColor = SociallyTheme.BrandBrown,
                inactiveTrackColor = SociallyTheme.BorderBeige
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ==========================================
// SCREEN 3: LAB ALBUMS MANAGER & CLOUD SYNC
// ==========================================
@Composable
fun AlbumsScreen(viewModel: SociallyViewModel) {
    val albums by viewModel.albumsFlow.collectAsStateWithLifecycle()
    val savedPhotos by viewModel.photosFlow.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

    var showCreateAlbumDialog by remember { mutableStateOf(false) }
    var inputAlbumName by remember { mutableStateOf("") }
    var inputAlbumDesc by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BACKUP CONTROLLER STATUS PANEL
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(SociallyTheme.CardSlate, SociallyTheme.BadgeCream)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Cloud storage coordination",
                                color = SociallyTheme.TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                if (pendingCount > 0) "$pendingCount negative rolls pending local synchronization" else "SQLite vault is fully synced to secure cloud buckets",
                                color = SociallyTheme.TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Icon(
                            imageVector = if (pendingCount > 0) Icons.Default.CloudQueue else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (pendingCount > 0) SociallyTheme.Terracotta else SociallyTheme.TealCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Progress indicators
                    if (isCloudSyncing) {
                        LinearProgressIndicator(
                            color = SociallyTheme.BrandBrown,
                            trackColor = SociallyTheme.BorderBeige,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SociallyTheme.BorderBeige)
                    )

                    Button(
                        onClick = { viewModel.runCloudStorageSync() },
                        enabled = !isCloudSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pendingCount > 0) SociallyTheme.BrandBrown else SociallyTheme.BadgeCream
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("sync_cloud_button")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCloudSyncing) Icons.Default.Sync else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (pendingCount > 0) SociallyTheme.PageBg else SociallyTheme.BrandBrown,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isCloudSyncing) "TRANSLATING NEGATIVE PACKETS..." else "COIL SYNCHRONIZATION BACKUP",
                                color = if (pendingCount > 0) SociallyTheme.PageBg else SociallyTheme.BrandBrown,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // CHAMBERS ALIAS DIRECTORIES HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Darkroom Chamber Folders",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = SociallyTheme.BrandBrown
                        )
                    )
                    Text("Subdivided rolls categorized cleanly in Room tables.", color = SociallyTheme.TextMuted, fontSize = 11.sp)
                }

                IconButton(
                    onClick = { showCreateAlbumDialog = true },
                    modifier = Modifier
                        .background(SociallyTheme.BadgeCream, shape = CircleShape)
                        .border(1.dp, SociallyTheme.BorderTan, CircleShape)
                        .size(36.dp)
                        .testTag("open_create_album_dialog_btn")
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Add Album", tint = SociallyTheme.BrandBrown, modifier = Modifier.size(18.dp))
                }
            }
        }

        if (albums.isEmpty()) {
            item {
                Text("Zero directories recorded.", color = SociallyTheme.TextMuted, modifier = Modifier.fillMaxWidth().padding(30.dp), textAlign = TextAlign.Center)
            }
        } else {
            items(albums) { album ->
                val photosInAlbum = savedPhotos.filter { it.albumId == album.id }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SociallyTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SociallyTheme.BorderBeige)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = SociallyTheme.GoldBrass, modifier = Modifier.size(26.dp))
                                Column {
                                    Text(album.name, fontWeight = FontWeight.Bold, color = SociallyTheme.TextMain, fontSize = 14.sp)
                                    Text(album.description, color = SociallyTheme.TextMuted, fontSize = 10.sp)
                                }
                            }

                            // Actions
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(SociallyTheme.BadgeCream, shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${photosInAlbum.size} rolls", color = SociallyTheme.BrandBrown, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }

                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Album",
                                    tint = SociallyTheme.Terracotta,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { viewModel.deleteAlbum(album) }
                                )
                            }
                        }

                        // Scrollable nested previews
                        if (photosInAlbum.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(photosInAlbum) { ph ->
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.loadPhotoForDetail(ph) }
                                            .border(1.dp, SociallyTheme.BorderTan, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = ph.fileUri,
                                            contentDescription = ph.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            colorFilter = createCombinedMatrix(
                                                filterName = ph.filterName,
                                                brightness = ph.brightness,
                                                contrast = ph.contrast,
                                                saturation = ph.saturation,
                                                warmth = ph.warmth
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // CREATE NEW ALBUM MODAL DIALOGUE
    if (showCreateAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showCreateAlbumDialog = false },
            containerColor = SociallyTheme.PageBg,
            title = {
                Text(
                    "Open New Chamber Cabin",
                    style = MaterialTheme.typography.titleMedium.copy(color = SociallyTheme.TextMain, fontFamily = FontFamily.Serif)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputAlbumName,
                        onValueChange = { inputAlbumName = it },
                        label = { Text("Folder Name", color = SociallyTheme.TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SociallyTheme.BrandBrown,
                            unfocusedBorderColor = SociallyTheme.BorderTan,
                            focusedTextColor = SociallyTheme.TextMain,
                            unfocusedTextColor = SociallyTheme.TextMuted,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("album_name_input")
                    )

                    OutlinedTextField(
                        value = inputAlbumDesc,
                        onValueChange = { inputAlbumDesc = it },
                        label = { Text("Aesthetic Description", color = SociallyTheme.TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SociallyTheme.BrandBrown,
                            unfocusedBorderColor = SociallyTheme.BorderTan,
                            focusedTextColor = SociallyTheme.TextMain,
                            unfocusedTextColor = SociallyTheme.TextMuted,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("album_desc_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputAlbumName.isNotEmpty()) {
                            viewModel.createAndInsertAlbum(inputAlbumName, inputAlbumDesc)
                            inputAlbumName = ""
                            inputAlbumDesc = ""
                            showCreateAlbumDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_album_btn")
                ) {
                    Text("Mold Chamber", color = SociallyTheme.BrandBrown, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAlbumDialog = false }) {
                    Text("Cancel", color = SociallyTheme.TextMuted)
                }
            }
        )
    }
}

// ==========================================
// SCREEN 4: DETAILED EXAMINE GALLERY ITEMS
// ==========================================
@Composable
fun PhotoDetailScreen(viewModel: SociallyViewModel) {
    val context = LocalContext.current
    val photo by viewModel.detailPhoto.collectAsStateWithLifecycle()

    if (photo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No negative selected.", color = SociallyTheme.TextMuted)
        }
        return
    }

    val p = photo!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BACK HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileClickable(
                onClick = { viewModel.navigateTo(Screen.Gallery) },
                testTag = "detail_back_button"
            ) { isPressed ->
                Row(
                    modifier = Modifier
                        .background(SociallyTheme.BadgeCream, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, SociallyTheme.BorderTan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SociallyTheme.BrandBrown, modifier = Modifier.size(16.dp))
                    Text("Gallery Lab", color = SociallyTheme.BrandBrown, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = { viewModel.deletePhoto(p) },
                modifier = Modifier
                    .background(SociallyTheme.Terracotta.copy(alpha = 0.15f), shape = CircleShape)
                    .size(36.dp)
                    .testTag("delete_photo_btn")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Scrap Negative", tint = SociallyTheme.Terracotta, modifier = Modifier.size(18.dp))
            }
        }

        // FULL RENDER CHASSIS (3D Depth tilt)
        Interactive3DTiltCard(
            testTag = "detail_3d_card"
        ) { tx, ty ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = p.fileUri,
                    contentDescription = p.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = createCombinedMatrix(
                        filterName = p.filterName,
                        brightness = p.brightness,
                        contrast = p.contrast,
                        saturation = p.saturation,
                        warmth = p.warmth
                    )
                )

                if (p.vignette > 0.05f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val grad = Brush.radialGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = p.vignette * 0.9f)),
                                    center = Offset(size.width / 2, size.height / 2),
                                    radius = size.width * 0.75f
                                )
                                drawRect(brush = grad)
                            }
                    )
                }
            }
        }

        // DESCRIPTION CORE
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SociallyTheme.CardSlate, shape = RoundedCornerShape(16.dp))
                .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    p.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = SociallyTheme.TextMain,
                        fontWeight = FontWeight.Bold
                    )
                )

                Box(
                    modifier = Modifier
                        .background(SociallyTheme.BadgeCream, shape = RoundedCornerShape(6.dp))
                        .border(1.dp, SociallyTheme.BorderTan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Fuji ${p.filterName}", color = SociallyTheme.BrandBrown, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = SociallyTheme.TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                val formatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()) }
                val dateStr = remember(p.savedAt) { formatter.format(java.util.Date(p.savedAt)) }
                Text(dateStr, color = SociallyTheme.TextMuted, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .background(
                            if (p.syncStatus == "Synced") SociallyTheme.TealCyan.copy(alpha = 0.15f)
                            else SociallyTheme.Terracotta.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (p.syncStatus == "Synced") "ONLINE BACKUP" else "LOCAL STORAGE COIL",
                        color = if (p.syncStatus == "Synced") SociallyTheme.TealCyan else SociallyTheme.Terracotta,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!p.caption.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("AI Structured Script", color = SociallyTheme.BrandBrown, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SociallyTheme.BadgeCream, shape = RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(p.caption, color = SociallyTheme.TextMuted, fontSize = 11.sp, lineHeight = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        val clip = LocalClipboardManager.current
                        Row(
                            modifier = Modifier
                                .background(SociallyTheme.BorderBeige, shape = RoundedCornerShape(6.dp))
                                .clickable { clip.setText(AnnotatedString(p.caption)) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = SociallyTheme.BrandBrown, modifier = Modifier.size(10.dp))
                            Text("Copy to clipboard", color = SociallyTheme.BrandBrown, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // PLATFORM QUICK MOCK EXPORTS
        Text("QUICK PLATFORM CHANNELS EXPORT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SociallyTheme.TextMuted)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val shares = listOf(
                Pair("Instagram", Icons.Default.CameraAlt),
                Pair("Lab Roll", Icons.Default.PhotoLibrary),
                Pair("Native Share", Icons.Default.Share)
            )

            shares.forEach { (name, icon) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(SociallyTheme.CardSlate, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(12.dp))
                        .clickable {
                            if (name == "Native Share") {
                                // Trigger genuine Android Platform Action share
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, p.title)
                                    putExtra(Intent.EXTRA_TEXT, "${p.title}\n\nFilm: Fuji ${p.filterName}\n\n${p.caption ?: ""}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Retro Negative"))
                            } else {
                                // Trigger Mock sharing success
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Exporting ${p.title} fuji analog output to $name!")
                                }
                                context.startActivity(Intent.createChooser(intent, "Mocking post on $name"))
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(icon, contentDescription = name, tint = SociallyTheme.BrandBrown, modifier = Modifier.size(18.dp))
                        Text(name, color = SociallyTheme.TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ==========================================
// SCREEN 5: AI PHOTO CHATBOT CONVERSATIONS (MANDATORY METADATA REQUIREMENT)
// ==========================================
@Composable
fun ChatBotScreen(viewModel: SociallyViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var currentTextQuery by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()

    // Scroll to latest message dynamically
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            coroutine.launch {
                scrollState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        // AI HEADER PANEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SociallyTheme.CardSlate, shape = RoundedCornerShape(14.dp))
                .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(14.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SociallyTheme.BadgeCream, shape = CircleShape)
                        .border(1.dp, SociallyTheme.BorderTan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SociallyTheme.BrandBrown, modifier = Modifier.size(16.dp))
                }

                Column {
                    Text("Retro Film AI Chamber", fontWeight = FontWeight.Bold, color = SociallyTheme.TextMain, fontSize = 13.sp)
                    Text("Knowledge Base: Retro Fuji, Composition, Exps", color = SociallyTheme.TextMuted, fontSize = 9.sp)
                }
            }

            TextButton(onClick = { viewModel.clearChat() }) {
                Text("Clear History", color = SociallyTheme.Terracotta, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // MSG SCROLLABLE CANVAS
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatHistory) { msg ->
                val fromUser = msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .wrapContentWidth(align = if (fromUser) Alignment.End else Alignment.Start)
                            .background(
                                color = if (fromUser) SociallyTheme.BadgeCream else SociallyTheme.CardSlate,
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (fromUser) 14.dp else 2.dp,
                                    bottomEnd = if (fromUser) 2.dp else 14.dp
                                )
                            )
                            .border(
                                1.dp,
                                if (fromUser) SociallyTheme.BorderTan else SociallyTheme.BorderBeige,
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (fromUser) 14.dp else 2.dp,
                                    bottomEnd = if (fromUser) 2.dp else 14.dp
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (fromUser) "YOU" else "SOCIALLY AI",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (fromUser) SociallyTheme.BrandBrown else SociallyTheme.TextMuted,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = msg.text,
                                color = SociallyTheme.TextMain,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SociallyTheme.CardSlate, shape = RoundedCornerShape(12.dp))
                                .border(1.dp, SociallyTheme.BorderBeige, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(color = SociallyTheme.BrandBrown, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                Text("AI Assistant structuring photography coordinates...", color = SociallyTheme.TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // QUICK CHEATS PHRASES ROW
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Suggest a dynamic night formula",
                "Explain fuji classic chrome filters",
                "How do I shoot aesthetic bokeh?",
                "Give me vintage record caption vibes"
            )

            items(suggestions) { keyword ->
                Box(
                    modifier = Modifier
                        .background(SociallyTheme.BadgeCream, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, SociallyTheme.BorderTan, RoundedCornerShape(10.dp))
                        .clickable {
                            currentTextQuery = keyword
                            viewModel.askAIShopAssistant(keyword)
                            currentTextQuery = ""
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(keyword, color = SociallyTheme.BrandBrown, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // INPUT PANEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentTextQuery,
                onValueChange = { currentTextQuery = it },
                placeholder = { Text("Consult Fuji AI assistant...", color = SociallyTheme.TextMuted.copy(alpha = 0.6f), fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SociallyTheme.BrandBrown,
                    unfocusedBorderColor = SociallyTheme.BorderTan,
                    focusedTextColor = SociallyTheme.TextMain,
                    unfocusedTextColor = SociallyTheme.TextMuted,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_textfield"),
                maxLines = 2
            )

            IconButton(
                onClick = {
                    if (currentTextQuery.isNotBlank()) {
                        viewModel.askAIShopAssistant(currentTextQuery)
                        currentTextQuery = ""
                    }
                },
                modifier = Modifier
                    .background(SociallyTheme.GoldBrass, shape = RoundedCornerShape(12.dp))
                    .size(48.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = SociallyTheme.MatteCharcoal, modifier = Modifier.size(18.dp))
            }
        }
    }
}
