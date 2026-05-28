@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MatchType
import com.example.data.ReplyRule
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldStatus

import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@Composable
fun HomeScreen(viewModel: AutoReplyViewModel) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val remainingReplies by viewModel.remainingReplies.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var isAccessEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while(true) {
            try {
                val enabled = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.isNotificationServiceEnabled(context)
                }
                isAccessEnabled = enabled
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Error checking access in loop", e)
            }
            kotlinx.coroutines.delay(2000)
        }
    }

    LaunchedEffect(Unit) {
        while(true) {
            viewModel.updateUsage()
            kotlinx.coroutines.delay(5000)
        }
    }

    Scaffold(
        topBar = { ImmersiveHeader(context, isPro, onUpgradeClick = { showUpgradeDialog = true }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (!isPro && rules.size >= 2) {
                        showUpgradeDialog = true
                    } else {
                        showAddDialog = true 
                    }
                },
                containerColor = CyberCyan,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Rule",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isAccessEnabled) {
                item {
                    com.example.PermissionBanner(onGrantClick = {
                        openNotificationSettings(context)
                    })
                }
            }
            
            item { StatusSection() }
            item { AnalyticsGrid(rules.size, isPro, remainingReplies) }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ACTIVE RULES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { openNotificationSettings(context) }) {
                        Text("Permission Settings", color = CyberCyan, fontSize = 12.sp)
                    }
                }
            }

            if (rules.isEmpty()) {
                item { EmptyState() }
            } else {
                items(rules, key = { it.id }) { rule ->
                    RuleRow(
                        rule = rule,
                        onToggle = { viewModel.toggleRule(rule) },
                        onDelete = { viewModel.deleteRule(rule) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SafetyGuidelinesCard() }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (showAddDialog) {
            AddRuleDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { pattern, reply, matchType ->
                    val success = viewModel.addRule(pattern, reply, matchType)
                    if (success) {
                        showAddDialog = false
                    } else {
                        showUpgradeDialog = true
                    }
                }
            )
        }

        if (showUpgradeDialog) {
            UpgradeDialog(
                onDismiss = { showUpgradeDialog = false },
                onUpgrade = { 
                    viewModel.upgrade()
                    showUpgradeDialog = false
                }
            )
        }
    }
}

@Composable
fun ImmersiveHeader(context: Context, isPro: Boolean, onUpgradeClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                if (isPro) "PRO ENGINE ACTIVE" else "NEURAL ENGINE ACTIVE",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPro) Color(0xFFFFD700) else CyberCyan
            )
            Text(
                text = buildAnnotatedString {
                    append("Autozapp")
                    withStyle(SpanStyle(color = if (isPro) Color(0xFFFFD700) else CyberCyan)) {
                        append(".")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!isPro) {
                IconButton(
                    onClick = onUpgradeClick,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = "Upgrade", tint = Color(0xFFFFD700))
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val pulse by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(if (isPro) Color(0xFFFFD700) else CyberCyan)
                )
            }
        }
    }
}

@Composable
fun StatusSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        // Blur glow
        Box(
            modifier = Modifier
                .offset(x = 180.dp, y = (-60).dp)
                .size(120.dp)
                .blur(40.dp)
                .clip(CircleShape)
                .background(CyberCyan.copy(alpha = 0.15f))
        )

        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            Text(
                "Anti-Block Status",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Shield Protected",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusTag("FLUID MOTION", EmeraldStatus)
                StatusTag("AI-HUMANIZED", CyberCyan)
            }
        }
    }
}

@Composable
fun StatusTag(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnalyticsGrid(rulesCount: Int, isPro: Boolean, remaining: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnalyticsCard(
            "Limit", 
            if (isPro) "∞" else "$remaining", 
            if (isPro) "Unlimited" else "Daily Remaining", 
            if (isPro) Color(0xFFFFD700) else CyberCyan, 
            Modifier.weight(1f)
        )
        AnalyticsCard(
            "Rules", 
            "$rulesCount", 
            if (isPro) "Unlimited" else "Max 2", 
            EmeraldStatus, 
            Modifier.weight(1f)
        )
    }
}

@Composable
fun AnalyticsCard(label: String, value: String, subValue: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light
        )
        Text(
            subValue,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp
        )
    }
}

@Composable
fun RuleRow(
    rule: ReplyRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val currentAlpha = if (rule.isEnabled) 1f else 0.5f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(currentAlpha)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val iconText = when (rule.matchType) {
                    MatchType.EXACT -> "EX"
                    MatchType.CONTAINS -> "CO"
                    MatchType.REGEX -> "RE"
                }
                Text(
                    iconText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (rule.isEnabled) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.pattern,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Trigger Type:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = rule.matchType.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberCyan,
                    checkedTrackColor = CyberCyan.copy(alpha = 0.2f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "RESPONSE PAYLOAD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = rule.reply,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 2
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF5252).copy(alpha = 0.1f)),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFF5252))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Rule",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = CyberCyan.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No active rules",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, MatchType) -> Unit
) {
    var pattern by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf(MatchType.EXACT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "INITIALIZE PROTOCOL",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Trigger Pattern") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                OutlinedTextField(
                    value = reply,
                    onValueChange = { reply = it },
                    label = { Text("Response Payload") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Text("Match Type", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MatchType.values().forEach { type ->
                        FilterChip(
                            selected = matchType == type,
                            onClick = { matchType = type },
                            label = { Text(type.name) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.1f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (pattern.isNotEmpty() && reply.isNotEmpty()) onConfirm(pattern, reply, matchType) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
            ) {
                Text("CONFIRM")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ABORT", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = Color(0xFF111418),
        shape = RoundedCornerShape(32.dp)
    )
}

@Composable
fun UpgradeDialog(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "UNLOCK PRO ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFD700)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProFeatureRow("Unlimited Rules", "Create as many automations as you need.")
                ProFeatureRow("Priority Routing", "Faster response times with direct injection.")
                ProFeatureRow("Zero Limits", "Say goodbye to daily response caps.")
                ProFeatureRow("Neural Core", "Unlock advanced Regex and pattern matching.")
            }
        },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color.Black
                )
            ) {
                Text("UPGRADE NOW", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("NOT NOW", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = Color(0xFF0F1115),
        shape = RoundedCornerShape(32.dp)
    )
}

@Composable
fun ProFeatureRow(title: String, description: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFFFFD700)
        )
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    context.startActivity(intent)
}

@Composable
fun SafetyGuidelinesCard() {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
            .border(
                1.dp, 
                if (expanded) CyberCyan.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), 
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield Protection",
                        tint = CyberCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "SEGURANÇA & ANTI-BLOQUEIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Como funciona e como evitar banimentos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
                
                Text(
                    text = "A automação de respostas é uma ferramenta crucial para empresas e profissionais garantirem atendimento instantâneo 24h, aumentando a conversão de leads e satisfação em até 85%. Para manter o funcionamento seguro e fluido sem bloqueios da Meta, siga as diretrizes integradas abaixo:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                SafetyInstructionRow(
                    index = "01",
                    title = "Funcionamento Não-Intrusivo",
                    description = "O Autozapp lê a notificação nativa do aparelho e interage pelo canal de resposta do Android. Não altera o código do WhatsApp, não precisa de root, e por isso é 100% invisível aos rastreadores de bot corporativos."
                )
                
                SafetyInstructionRow(
                    index = "02",
                    title = "Pacing Neural Cadenciado",
                    description = "Nossa tecnologia simula o atraso de digitação natural. Respostas instantâneas milissegundas repetitivas ativam alarmes de spam das operadoras. Mantenha os fluxos fluidos usando mensagens simplificadas e objetivas."
                )
                
                SafetyInstructionRow(
                    index = "03",
                    title = "Personalização de Triggers",
                    description = "Evite regras genéricas que respondam a qualquer saudação simples. Use gatilhos baseados em palavras-chave que exigem respostas funcionais de suporte, preços ou agendamentos."
                )
                
                SafetyInstructionRow(
                    index = "04",
                    title = "Proibido Disparos em Massa",
                    description = "Utilize o aplicativo estritamente como receptor-respondedor de mensagens recebidas organicamente. O envio em massa para listas frias viola severamente os termos da Meta e causa bloqueios em poucas horas."
                )
            }
        }
    }
}

@Composable
fun SafetyInstructionRow(index: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = index,
            style = MaterialTheme.typography.labelLarge,
            color = CyberCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
