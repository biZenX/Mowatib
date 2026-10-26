package com.etrisad.zenith.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.etrisad.zenith.data.remote.model.GitHubRelease
import com.etrisad.zenith.ui.theme.ZenithTheme
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    release: GitHubRelease,
    useExpressiveColors: Boolean,
    isDark: Boolean = isSystemInDarkTheme(),
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var seedColor by remember { mutableStateOf<Color?>(null) }
    
    val firstImageUrl = remember(release.body) {
        val lines = release.body.trim().lines()
        lines.firstOrNull { it.trim().startsWith("![") || it.trim().startsWith("<img", ignoreCase = true) }?.let { line ->
            if (line.trim().startsWith("![")) {
                line.substringAfter("(").substringBefore(")")
            } else {
                line.substringAfter("src=\"", "").substringBefore("\"")
            }
        }
    }

    LaunchedEffect(firstImageUrl) {
        if (firstImageUrl != null) {
            val loader = coil.Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(firstImageUrl)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is coil.request.SuccessResult) {
                val bitmap = result.drawable.toBitmap()
                Palette.from(bitmap).generate { palette ->
                    seedColor = palette?.vibrantSwatch?.rgb?.let { Color(it) }
                        ?: palette?.dominantSwatch?.rgb?.let { Color(it) }
                }
            }
        }
    }

    val currentScheme = MaterialTheme.colorScheme
    
    val dynamicColorScheme = remember(seedColor, isDark) {
        seedColor?.let {
            generateDynamicColorScheme(it, isDark, currentScheme)
        } ?: currentScheme
    }

    val hideAndDismiss = {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    val content = @Composable {
        val containerColor by animateColorAsState(
            targetValue = if (useExpressiveColors) MaterialTheme.colorScheme.surfaceContainerHighest
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "containerColor"
        )

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            UpdateBottomSheetContent(
                release = release,
                containerColor = containerColor,
                screenHeight = screenHeight,
                onDismiss = { hideAndDismiss() },
                onUpdate = {
                    onUpdate()
                    hideAndDismiss()
                }
            )
        }
    }

    MaterialTheme(colorScheme = dynamicColorScheme) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogBottomSheet(
    releases: List<GitHubRelease>,
    useExpressiveColors: Boolean,
    isDark: Boolean = isSystemInDarkTheme(),
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Changelog History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(releases) { release ->
                    ChangelogVersionCard(
                        release = release,
                        useExpressiveColors = useExpressiveColors,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
fun ChangelogVersionCard(
    release: GitHubRelease,
    useExpressiveColors: Boolean,
    isDark: Boolean
) {
    val context = LocalContext.current
    var seedColor by remember { mutableStateOf<Color?>(null) }
    
    val firstImageUrl = remember(release.body) {
        val lines = release.body.trim().lines()
        lines.firstOrNull { it.trim().startsWith("![") || it.trim().startsWith("<img", ignoreCase = true) }?.let { line ->
            if (line.trim().startsWith("![")) {
                line.substringAfter("(").substringBefore(")")
            } else {
                line.substringAfter("src=\"", "").substringBefore("\"")
            }
        }
    }

    LaunchedEffect(firstImageUrl) {
        if (firstImageUrl != null) {
            val loader = coil.Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(firstImageUrl)
                .allowHardware(false)
                .build()
            val result = try { loader.execute(request) } catch (e: Exception) { null }
            if (result is coil.request.SuccessResult) {
                val bitmap = result.drawable.toBitmap()
                Palette.from(bitmap).generate { palette ->
                    seedColor = palette?.vibrantSwatch?.rgb?.let { Color(it) }
                        ?: palette?.dominantSwatch?.rgb?.let { Color(it) }
                }
            }
        }
    }

    val currentScheme = MaterialTheme.colorScheme
    
    val dynamicColorScheme = remember(seedColor, isDark) {
        seedColor?.let {
            generateDynamicColorScheme(it, isDark, currentScheme)
        } ?: currentScheme
    }

    MaterialTheme(colorScheme = dynamicColorScheme) {
        val targetCardColor = MaterialTheme.colorScheme.surfaceContainerLow
        val animatedCardColor by animateColorAsState(
            targetValue = targetCardColor,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "cardColor"
        )
        
        val targetMarkdownColor = if (useExpressiveColors) MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainerHigh
        
        val animatedMarkdownColor by animateColorAsState(
            targetValue = targetMarkdownColor,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "markdownColor"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = animatedCardColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = release.tagName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatReleaseDate(release.publishedAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                MarkdownBody(
                    content = release.body,
                    containerColor = animatedMarkdownColor
                )
            }
        }
    }
}

private fun formatReleaseDate(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val instant = java.time.Instant.parse(dateString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString.substringBefore("T")
    }
}

private fun generateDynamicColorScheme(seed: Color, isDark: Boolean, currentScheme: ColorScheme): ColorScheme {
    val hsl = FloatArray(3)
    val argb = seed.toArgb()
    android.graphics.Color.RGBToHSV(
        android.graphics.Color.red(argb),
        android.graphics.Color.green(argb),
        android.graphics.Color.blue(argb),
        hsl
    )
    
    val primaryV = if (isDark) 0.9f else 0.4f
    val primary = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], hsl[1] * 0.8f, primaryV)))
    
    val secondaryV = if (isDark) 0.9f else 0.35f
    val secondary = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], hsl[1] * 0.4f, secondaryV)))
    
    val tertiaryV = if (isDark) 0.9f else 0.45f
    val tertiary = Color(android.graphics.Color.HSVToColor(floatArrayOf((hsl[0] + 60f) % 360f, hsl[1] * 0.6f, tertiaryV)))

    val surfaceV = if (isDark) 0.1f else 0.98f
    val surface = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], hsl[1] * 0.1f, surfaceV)))
    val surfaceContainerLowest = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], hsl[1] * 0.05f, if (isDark) 0.04f else 1.0f)))
    val surfaceContainerLow = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], hsl[1] * 0.1f, if (isDark) 0.12f else 0.96f)))
    val surfaceContainer = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], hsl[1] * 0.15f, if (isDark) 0.15f else 0.94f)))
    val surfaceContainerHigh = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], hsl[1] * 0.2f, if (isDark) 0.2f else 0.9f)))

    return currentScheme.copy(
        primary = primary,
        onPrimary = if (isDark) Color.Black else Color.White,
        primaryContainer = primary.copy(alpha = if (isDark) 0.3f else 0.15f),
        onPrimaryContainer = primary,
        
        secondary = secondary,
        onSecondary = if (isDark) Color.Black else Color.White,
        secondaryContainer = secondary.copy(alpha = if (isDark) 0.25f else 0.2f),
        onSecondaryContainer = secondary,
        
        tertiary = tertiary,
        onTertiary = if (isDark) Color.Black else Color.White,
        tertiaryContainer = tertiary.copy(alpha = if (isDark) 0.25f else 0.14f),
        onTertiaryContainer = tertiary,

        surface = surface,
        onSurface = if (isDark) Color.White else Color.Black,
        surfaceVariant = surfaceContainer,
        onSurfaceVariant = if (isDark) Color.LightGray else Color.DarkGray,
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHigh.copy(alpha = 0.9f)
    )
}

@Composable
fun UpdateBottomSheetContent(
    release: GitHubRelease,
    containerColor: Color,
    screenHeight: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }

    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = screenHeight * 0.9f)
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            UpdateHeaderSection(
                tagName = release.tagName,
                iconScale = iconScale
            )

            MarkdownBody(
                content = release.body,
                containerColor = containerColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            ZenithGroupedButton(
                size = ZenithButtonSize.ExtraLarge
            ) {
                ZenithButtonWeighted(
                    onClick = onDismiss,
                    weight = 1f,
                    text = "Later",
                    type = ZenithButtonType.Tonal,
                    size = ZenithButtonSize.ExtraLarge,
                    isFirst = true,
                    isLast = false
                )

                ZenithButtonWeighted(
                    onClick = onUpdate,
                    weight = 1.5f,
                    text = "Update",
                    icon = Icons.Outlined.Download,
                    type = ZenithButtonType.Filled,
                    size = ZenithButtonSize.ExtraLarge,
                    isFirst = false,
                    isLast = true
                )
            }
        }
    }
}


@Composable
private fun UpdateHeaderSection(
    tagName: String,
    iconScale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "New Version Available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Surface(
            color = MaterialTheme.colorScheme.tertiary,
            shape = CircleShape,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = tagName,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private val EMOJI_REGEX = Regex(
    "[\\uD83C-\\uDBFF][\\uDC00-\\uDFFF]|" +
    "[\\u2600-\\u27BF]|" +
    "[\\u2300-\\u23FF]|" +
    "[\\u2B50-\\u2B55]|" +
    "[\\u203C\\u2049\\u2139\\u2194-\\u21AA\\u25AA-\\u25FE]|" +
    "[\\uFE00-\\uFE0F]|" +
    "[\\u200D]"
)

fun stripEmojis(input: String): String {
    val noEmoji = input.replace(EMOJI_REGEX, "")
    return noEmoji.replace(Regex("\\s+"), " ").trim()
}

fun hasBilingualContent(text: String): Boolean {
    val hasEn = text.contains("<!-- lang:en -->") || text.contains(Regex("(?im)^##+\\s+(english|\\[en\\])"))
    val hasAr = text.contains("<!-- lang:ar -->") || text.contains(Regex("(?im)^##+\\s+(العربية|\\[ar\\])"))
    return hasEn && hasAr
}

fun extractLocalizedContent(raw: String, targetLang: String): String {
    if (!hasBilingualContent(raw)) return raw

    if (raw.contains("<!-- lang:")) {
        val parts = raw.split("<!-- lang:")
        val prefix = parts.firstOrNull()?.trim() ?: ""
        for (part in parts.drop(1)) {
            val tag = part.substringBefore("-->").trim()
            val body = part.substringAfter("-->", "").trim()
            if (tag.equals(targetLang, ignoreCase = true)) {
                return if (prefix.isNotEmpty()) "$prefix\n\n$body" else body
            }
        }
    }

    val lines = raw.lines()
    val prefixLines = mutableListOf<String>()
    var currentLang: String? = null
    val enLines = mutableListOf<String>()
    val arLines = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.matches(Regex("(?im)^##+\\s+(english|\\[en\\]).*"))) {
            currentLang = "en"
            continue
        } else if (trimmed.matches(Regex("(?im)^##+\\s+(العربية|\\[ar\\]).*"))) {
            currentLang = "ar"
            continue
        }

        if (currentLang == null) {
            prefixLines.add(line)
        } else if (currentLang == "en") {
            enLines.add(line)
        } else if (currentLang == "ar") {
            arLines.add(line)
        }
    }

    val selectedLines = if (targetLang == "ar") arLines else enLines
    if (selectedLines.isNotEmpty()) {
        val prefix = prefixLines.joinToString("\n").trim()
        val body = selectedLines.joinToString("\n").trim()
        return if (prefix.isNotEmpty()) "$prefix\n\n$body" else body
    }
    return raw
}

fun getHeaderIcon(headerText: String): ImageVector {
    val clean = stripEmojis(headerText).lowercase()
    return when {
        clean.contains("telegram") || clean.contains("تليجرام") || clean.contains("تيليجرام") || clean.contains("بوت") ->
            Icons.Outlined.SmartToy
        clean.contains("widget") || clean.contains("ويدجت") || clean.contains("ودجت") ->
            Icons.Outlined.Widgets
        clean.contains("add") || clean.contains("new") || clean.contains("feature") || clean.contains("ميز") || clean.contains("اضاف") || clean.contains("إضاف") || clean.contains("جديد") ->
            Icons.Outlined.AddCircleOutline
        clean.contains("fix") || clean.contains("bug") || clean.contains("اصلاح") || clean.contains("إصلاح") || clean.contains("تصحيح") ->
            Icons.Outlined.BugReport
        clean.contains("change") || clean.contains("improve") || clean.contains("تعديل") || clean.contains("تحسين") || clean.contains("تغيير") || clean.contains("ترقي") ->
            Icons.Outlined.Tune
        clean.contains("security") || clean.contains("امن") || clean.contains("أمان") || clean.contains("حماي") ->
            Icons.Outlined.Shield
        clean.contains("perf") || clean.contains("speed") || clean.contains("سرع") || clean.contains("اداء") || clean.contains("أداء") ->
            Icons.Outlined.Speed
        clean.contains("focus") || clean.contains("pomodoro") || clean.contains("تركيز") || clean.contains("بومودورو") || clean.contains("وقت") ->
            Icons.Outlined.Timer
        clean.contains("task") || clean.contains("مهم") || clean.contains("مهام") ->
            Icons.Outlined.TaskAlt
        clean.contains("sound") || clean.contains("audio") || clean.contains("صوت") ->
            Icons.Outlined.Headphones
        else ->
            Icons.AutoMirrored.Outlined.ListAlt
    }
}

@Composable
fun MarkdownBody(
    content: String,
    containerColor: Color
) {
    val isBilingual = remember(content) { hasBilingualContent(content) }
    val defaultLang = remember {
        val appLang = java.util.Locale.getDefault().language.lowercase()
        if (appLang.startsWith("ar")) "ar" else "en"
    }
    var selectedLanguage by remember(content) { mutableStateOf(defaultLang) }

    val displayContent = remember(content, selectedLanguage) {
        if (isBilingual) {
            extractLocalizedContent(content, selectedLanguage)
        } else {
            content
        }
    }

    val sections = remember(displayContent) { parseMarkdownIntoSections(displayContent) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (isBilingual) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isAr = selectedLanguage == "ar"
                        val isEn = selectedLanguage == "en"

                        FilterChip(
                            selected = isAr,
                            onClick = { selectedLanguage = "ar" },
                            label = { Text("العربية", fontWeight = if (isAr) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isAr) {
                                { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )

                        FilterChip(
                            selected = isEn,
                            onClick = { selectedLanguage = "en" },
                            label = { Text("English", fontWeight = if (isEn) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isEn) {
                                { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }
                }
            }
        }

        sections.forEach { section ->
            when (section) {
                is MarkdownSection.Header1 -> {
                    Text(
                        text = parseInlineMarkdown(section.text),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                is MarkdownSection.GroupedCard -> {
                    MarkdownGroupedCard(
                        elements = section.elements,
                        containerColor = containerColor
                    )
                }
                is MarkdownSection.Image -> {
                    MarkdownImageItem(url = section.url)
                }
            }
        }
    }
}

@Composable
fun MarkdownGroupedCard(
    elements: List<MarkdownElement>,
    containerColor: Color
) {
    val elementGroups = remember(elements) {
        val groups = mutableListOf<List<MarkdownElement>>()
        var currentSubGroup = mutableListOf<MarkdownElement>()
        
        elements.forEach { element ->
            if (element is MarkdownElement.Header2 && currentSubGroup.isNotEmpty()) {
                groups.add(currentSubGroup.toList())
                currentSubGroup = mutableListOf()
            }
            currentSubGroup.add(element)
        }
        if (currentSubGroup.isNotEmpty()) groups.add(currentSubGroup)
        groups
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        elementGroups.forEachIndexed { index, group ->
            val isFirst = index == 0
            val isLast = index == elementGroups.size - 1
            
            val topRadius = if (isFirst) 28.dp else 8.dp
            val bottomRadius = if (isLast) 28.dp else 8.dp
            val shape = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius)

            Card(
                modifier = Modifier.fillMaxWidth().clip(shape),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                shape = shape
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    group.forEach { element ->
                        when (element) {
                            is MarkdownElement.Header2 -> {
                                MarkdownHeaderItem(
                                    text = element.text,
                                    icon = getHeaderIcon(element.text),
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            is MarkdownElement.Header3 -> {
                                MarkdownHeaderItem(
                                    text = element.text,
                                    icon = getHeaderIcon(element.text),
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            is MarkdownElement.ListItem -> {
                                MarkdownListItemRow(text = element.text, isIndented = element.isIndented)
                            }
                            is MarkdownElement.NumberedListItem -> {
                                MarkdownNumberedListItemRow(number = element.number, text = element.text, isIndented = element.isIndented)
                            }
                            is MarkdownElement.Text -> {
                                Text(
                                    text = parseInlineMarkdown(element.text),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            is MarkdownElement.Quote -> {
                                MarkdownQuoteItem(text = element.text)
                            }
                            is MarkdownElement.Divider -> {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownHeaderItem(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = style,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MarkdownListItemRow(text: String, isIndented: Boolean = false) {
    val startPadding = if (isIndented) 24.dp else 8.dp
    val bulletSize = if (isIndented) 5.dp else 6.dp
    Row(modifier = Modifier.padding(start = startPadding, bottom = 4.dp, top = 4.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(bulletSize)
                .background(
                    if (isIndented) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.primary,
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MarkdownNumberedListItemRow(number: String, text: String, isIndented: Boolean = false) {
    val startPadding = if (isIndented) 24.dp else 8.dp
    Row(
        modifier = Modifier.padding(start = startPadding, bottom = 4.dp, top = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = "$number.",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MarkdownImageItem(url: String) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .heightIn(max = 400.dp)
            .clip(RoundedCornerShape(28.dp)),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun MarkdownQuoteItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(IntrinsicSize.Min)
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    val cleanText = stripEmojis(text)
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    var i = 0
    val colorScheme = MaterialTheme.colorScheme
    val onSurface = colorScheme.onSurface

    while (i < cleanText.length) {
        when {
            // Inline code: `code`
            cleanText.startsWith("`", i) -> {
                val end = cleanText.indexOf("`", i + 1)
                if (end != -1) {
                    val codeContent = cleanText.substring(i + 1, end)
                    builder.pushStyle(
                        androidx.compose.ui.text.SpanStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            background = colorScheme.surfaceContainerHighest,
                            color = colorScheme.primary
                        )
                    )
                    builder.append(" $codeContent ")
                    builder.pop()
                    i = end + 1
                } else {
                    builder.append(cleanText[i])
                    i++
                }
            }
            // Strikethrough: ~~text~~
            cleanText.startsWith("~~", i) -> {
                val end = cleanText.indexOf("~~", i + 2)
                if (end != -1) {
                    builder.pushStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
                    builder.append(cleanText.substring(i + 2, end))
                    builder.pop()
                    i = end + 2
                } else {
                    builder.append(cleanText[i])
                    i++
                }
            }
            // Bold: **text**
            cleanText.startsWith("**", i) -> {
                val end = cleanText.indexOf("**", i + 2)
                if (end != -1) {
                    builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = onSurface))
                    builder.append(cleanText.substring(i + 2, end))
                    builder.pop()
                    i = end + 2
                } else {
                    builder.append(cleanText[i])
                    i++
                }
            }
            // Italic: *text*
            cleanText.startsWith("*", i) -> {
                val end = cleanText.indexOf("*", i + 1)
                if (end != -1) {
                    builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                    builder.append(cleanText.substring(i + 1, end))
                    builder.pop()
                    i = end + 1
                } else {
                    builder.append(cleanText[i])
                    i++
                }
            }
            cleanText.startsWith("![", i) -> {
                val endAlt = cleanText.indexOf("]", i)
                if (endAlt != -1 && cleanText.startsWith("(", endAlt + 1)) {
                    val endUrl = cleanText.indexOf(")", endAlt + 1)
                    if (endUrl != -1) {
                        i = endUrl + 1
                    } else {
                        builder.append(cleanText[i])
                        i++
                    }
                } else {
                    builder.append(cleanText[i])
                    i++
                }
            }
            cleanText.startsWith("[", i) -> {
                val endText = cleanText.indexOf("]", i)
                if (endText != -1 && cleanText.startsWith("(", endText + 1)) {
                    val endUrl = cleanText.indexOf(")", endText + 1)
                    if (endUrl != -1) {
                        val linkText = cleanText.substring(i + 1, endText)
                        val url = cleanText.substring(endText + 2, endUrl)
                        builder.pushStyle(androidx.compose.ui.text.SpanStyle(
                            color = colorScheme.primary,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                            fontWeight = FontWeight.Bold
                        ))
                        builder.append(linkText)
                        builder.addStringAnnotation("URL", url, builder.length - linkText.length, builder.length)
                        builder.pop()
                        i = endUrl + 1
                    } else {
                        builder.append(cleanText[i])
                        i++
                    }
                } else {
                    builder.append(cleanText[i])
                    i++
                }
            }
            cleanText.startsWith("<img", i) -> {
                val end = cleanText.indexOf(">", i)
                if (end != -1) {
                    i = end + 1
                } else {
                    builder.append(cleanText[i])
                    i++
                }
            }
            cleanText[i] == '#' && i + 1 < cleanText.length && cleanText[i+1].isDigit() -> {
                var j = i + 1
                while (j < cleanText.length && cleanText[j].isDigit()) {
                    j++
                }
                builder.pushStyle(androidx.compose.ui.text.SpanStyle(
                    color = colorScheme.secondary,
                    fontWeight = FontWeight.ExtraBold
                ))
                builder.append(cleanText.substring(i, j))
                builder.pop()
                i = j
            }
            else -> {
                builder.append(cleanText[i])
                i++
            }
        }
    }
    return builder.toAnnotatedString()
}

sealed class MarkdownSection {
    data class Header1(val text: String) : MarkdownSection()
    data class Image(val url: String) : MarkdownSection()
    data class GroupedCard(val elements: List<MarkdownElement>) : MarkdownSection()
}

sealed class MarkdownElement {
    data class Header2(val text: String) : MarkdownElement()
    data class Header3(val text: String) : MarkdownElement()
    data class Text(val text: String) : MarkdownElement()
    data class ListItem(val text: String, val isIndented: Boolean = false) : MarkdownElement()
    data class NumberedListItem(val number: String, val text: String, val isIndented: Boolean = false) : MarkdownElement()
    data class Quote(val text: String) : MarkdownElement()
    object Divider : MarkdownElement()
}

private fun parseMarkdownIntoSections(content: String): List<MarkdownSection> {
    val lines = content.trim().lines()
    val result = mutableListOf<MarkdownSection>()
    var currentGroup = mutableListOf<MarkdownElement>()
    
    fun flushGroup() {
        if (currentGroup.isNotEmpty()) {
            result.add(MarkdownSection.GroupedCard(currentGroup.toList()))
            currentGroup = mutableListOf()
        }
    }

    lines.forEach { rawLine ->
        val trimmed = stripEmojis(rawLine.trim())
        val isIndented = rawLine.startsWith("  ") || rawLine.startsWith("\t")

        when {
            trimmed.startsWith("# ") -> {
                flushGroup()
                result.add(MarkdownSection.Header1(trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("![") -> {
                flushGroup()
                val url = trimmed.substringAfter("(").substringBefore(")")
                result.add(MarkdownSection.Image(url))
            }
            trimmed.startsWith("<img", ignoreCase = true) -> {
                flushGroup()
                val src = trimmed.substringAfter("src=\"", "").substringBefore("\"")
                if (src.isNotEmpty()) {
                    result.add(MarkdownSection.Image(src))
                }
            }
            trimmed.startsWith("## ") -> {
                val headerText = trimmed.removePrefix("## ").trim()
                if (headerText.isNotEmpty()) {
                    currentGroup.add(MarkdownElement.Header2(headerText))
                }
            }
            trimmed.startsWith("### ") -> {
                val headerText = trimmed.removePrefix("### ").trim()
                if (headerText.isNotEmpty()) {
                    currentGroup.add(MarkdownElement.Header3(headerText))
                }
            }
            trimmed.matches(Regex("^[-*_]{3,}$")) -> {
                currentGroup.add(MarkdownElement.Divider)
            }
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                val itemText = trimmed.substring(2).trim()
                if (itemText.isNotEmpty()) {
                    currentGroup.add(MarkdownElement.ListItem(itemText, isIndented = isIndented))
                }
            }
            trimmed.matches(Regex("^\\d+[.)]\\s+.*")) -> {
                val num = trimmed.substringBefore(" ").removeSuffix(".").removeSuffix(")")
                val itemText = trimmed.substringAfter(" ").trim()
                if (itemText.isNotEmpty()) {
                    currentGroup.add(MarkdownElement.NumberedListItem(num, itemText, isIndented = isIndented))
                }
            }
            trimmed.startsWith("> ") -> {
                val quoteText = trimmed.removePrefix("> ").trim()
                if (quoteText.isNotEmpty()) {
                    currentGroup.add(MarkdownElement.Quote(quoteText))
                }
            }
            trimmed.isNotBlank() -> {
                currentGroup.add(MarkdownElement.Text(trimmed))
            }
        }
    }
    flushGroup()
    return result
}

@Preview(showBackground = true)
@Composable
fun UpdateBottomSheetPreview() {
    ZenithTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            UpdateBottomSheetContent(
                release = GitHubRelease(
                    tagName = "v1.6.0",
                    name = "Zenith v1.6.0",
                    body = """
                        # Zenith v1.6.0
                        This is a major update with many new features and improvements.
                        
                        > This update is critical for database safety.
                        
                        ## New Features
                        * **Material 3 Expressive**: New floating tab bar.
                        * **GitHub Update Checker**: Fast updates.
                        ![Preview Image](https://raw.githubusercontent.com/biZenX/Mowatib/main/fastlane/metadata/android/en-US/images/featureGraphic.png)
                        
                        <img src="https://raw.githubusercontent.com/biZenX/Mowatib/main/fastlane/metadata/android/en-US/images/featureGraphic.png" alt="Banner" />

                        ## Improvements
                        ### Performance
                        * Optimized usage history loading.
                        ### UI/UX
                        * Better support for foldable devices.
                        
                        This version is the result of many weeks of work.
                    """.trimIndent(),
                    htmlUrl = "https://github.com/biZenX/Mowatib",
                    assets = emptyList()
                ),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                screenHeight = 800.dp,
                onDismiss = {},
                onUpdate = {}
            )
        }
    }
}
