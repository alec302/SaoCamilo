package com.pisc.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pisc.project.data.local.SweatRateEntity
import com.pisc.project.data.repository.SweatRateRepository
import com.pisc.project.ui.*
import kotlinx.coroutines.launch

@Composable
fun App(repository: SweatRateRepository) {
    var loggedInUserEmail by remember { mutableStateOf<String?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }

    val colors = if (isDarkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalAppColors provides colors) {
        val colorScheme = if (isDarkTheme) {
            darkColorScheme(background = colors.bg, surface = colors.surface, primary = colors.accentStart, onBackground = colors.textPrimary, onSurface = colors.textPrimary)
        } else {
            lightColorScheme(background = colors.bg, surface = colors.surface, primary = colors.accentStart, onBackground = colors.textPrimary, onSurface = colors.textPrimary)
        }
        
        MaterialTheme(colorScheme = colorScheme) {
            Surface(modifier = Modifier.fillMaxSize(), color = colors.bg) {
                if (loggedInUserEmail == null) {
                LoginScreen(onLoginSuccess = { email, darkTheme -> 
                    loggedInUserEmail = email
                    isDarkTheme = darkTheme 
                })
            } else {
                val coroutineScope = rememberCoroutineScope()

                MainScaffoldContent(
                    repository = repository,
                    userEmail = loggedInUserEmail!!,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { newTheme -> 
                        isDarkTheme = newTheme 
                        coroutineScope.launch {
                            com.pisc.project.data.remote.AuthApiService().updateTheme(loggedInUserEmail!!, newTheme)
                        }
                    },
                    onLogout = { loggedInUserEmail = null },
                    onEmailUpdated = { newEmail -> loggedInUserEmail = newEmail }
                )
            }
            }
        }
    }
}

@Composable
fun MainScaffoldContent(
    repository: SweatRateRepository,
    userEmail: String,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onEmailUpdated: (String) -> Unit
) {
    val viewModel: SweatRateViewModel = viewModel { SweatRateViewModel(repository, userEmail) }
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()
    val colors = LocalAppColors.current

    var currentTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = colors.bg,
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                contentColor = colors.textPrimary
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Calculadora") },
                    label = { Text("Calculadora") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = colors.accentStart, selectedTextColor = colors.accentStart, indicatorColor = colors.surface, unselectedIconColor = colors.textSecondary, unselectedTextColor = colors.textSecondary)
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Histórico") },
                    label = { Text("Histórico") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = colors.accentStart, selectedTextColor = colors.accentStart, indicatorColor = colors.surface, unselectedIconColor = colors.textSecondary, unselectedTextColor = colors.textSecondary)
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Painel") },
                    label = { Text("Painel") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = colors.accentStart, selectedTextColor = colors.accentStart, indicatorColor = colors.surface, unselectedIconColor = colors.textSecondary, unselectedTextColor = colors.textSecondary)
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = colors.accentStart, selectedTextColor = colors.accentStart, indicatorColor = colors.surface, unselectedIconColor = colors.textSecondary, unselectedTextColor = colors.textSecondary)
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(colors.bg)) {
            when (currentTab) {
                0 -> CalculatorScreen(viewModel, uiState)
                1 -> HistoryScreen(history)
                2 -> DashboardScreen(history, userEmail)
                3 -> SettingsScreen(isDarkTheme, { newTheme -> 
                    onThemeToggle(newTheme)
                }, onLogout, userEmail, repository, onEmailUpdated)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean, 
    onThemeToggle: (Boolean) -> Unit, 
    onLogout: () -> Unit, 
    userEmail: String,
    repository: SweatRateRepository,
    onEmailUpdated: (String) -> Unit
) {
    val colors = LocalAppColors.current
    var newEmail by remember { mutableStateOf(userEmail) }
    var newPassword by remember { mutableStateOf("") }
    var updateMessage by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = colors.bg, unfocusedContainerColor = colors.bg,
        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
        focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary, cursorColor = colors.accentStart
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("CONFIGURAÇÕES", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = colors.textPrimary), modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
        Box(modifier = Modifier.padding(bottom = 16.dp).height(4.dp).width(40.dp).background(colors.accentBrush, CircleShape))

        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Usuário Ativo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = colors.textPrimary))
                    Text(userEmail, style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary))
                }
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerBg, contentColor = colors.danger)
                ) {
                    Text("Sair da Conta", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Modo Escuro (Dark Mode)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = colors.textPrimary))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accentStart, 
                        checkedTrackColor = colors.accentStart.copy(alpha=0.5f),
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.bg
                    )
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("ATUALIZAR CREDENCIAIS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 1.sp))
                
                TextField(value = newEmail, onValueChange = { newEmail = it }, placeholder = { Text("Novo E-mail", color = colors.textSecondary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = textFieldColors)
                TextField(value = newPassword, onValueChange = { newPassword = it }, placeholder = { Text("Nova Senha", color = colors.textSecondary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = textFieldColors)

                Button(
                    onClick = {
                        if (newEmail.isNotBlank() && newPassword.isNotBlank()) {
                            isUpdating = true
                            updateMessage = "Atualizando..."
                            coroutineScope.launch {
                                val authApi = com.pisc.project.data.remote.AuthApiService()
                                val cloudDb = com.pisc.project.data.remote.CloudSweatRateDataSource()
                                
                                val authSuccess = authApi.updateCredentials(userEmail, newEmail, newPassword)
                                if (authSuccess) {
                                    if (newEmail != userEmail) {
                                        cloudDb.updateUserEmail(userEmail, newEmail)
                                        repository.updateEmailLocally(userEmail, newEmail)
                                        onEmailUpdated(newEmail)
                                    }
                                    updateMessage = "Credenciais atualizadas com sucesso!"
                                } else {
                                    updateMessage = "Erro ao atualizar credenciais."
                                }
                                isUpdating = false
                            }
                        } else {
                            updateMessage = "Preencha e-mail e senha."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp), 
                    shape = CircleShape, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), 
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isUpdating
                ) {
                    val bgModifier = if (isUpdating) Modifier.background(colors.textSecondary) else Modifier.background(colors.accentBrush)
                    Box(modifier = Modifier.fillMaxSize().then(bgModifier), contentAlignment = Alignment.Center) {
                        Text(if (isUpdating) "ATUALIZANDO..." else "SALVAR ALTERAÇÕES", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color.White))
                    }
                }

                if (updateMessage.isNotEmpty()) {
                    Text(updateMessage, color = if (updateMessage.contains("Erro")) colors.danger else colors.success, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: SweatRateViewModel, uiState: SweatRateResult?) {
    val colors = LocalAppColors.current

    val formState by viewModel.formState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchWeatherIfNeeded()
    }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = colors.bg, unfocusedContainerColor = colors.bg,
        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
        focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary, cursorColor = colors.accentStart
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("TAXA DE SUDORESE", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = colors.textPrimary), modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
        Box(modifier = Modifier.padding(bottom = 16.dp).height(4.dp).width(40.dp).background(colors.accentBrush, CircleShape))

        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("DADOS DO TREINO", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 1.sp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = formState.trainingType, onValueChange = { viewModel.updateForm { s -> s.copy(trainingType = it) } }, placeholder = { Text("Treino (Ex: Corrida)", color = colors.textSecondary) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), singleLine = true, colors = textFieldColors)
                    TextField(
                        value = formState.climate, 
                        onValueChange = { viewModel.updateForm { s -> s.copy(climate = it) } }, 
                        placeholder = { Text("Clima", color = colors.textSecondary) }, 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(16.dp), 
                        singleLine = true, 
                        colors = textFieldColors,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.forceFetchWeather() }) {
                                if (formState.isLoadingWeather) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.accentStart, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Atualizar Clima", tint = colors.accentStart)
                                }
                            }
                        }
                    )
                }

                TextField(value = formState.preWeight, onValueChange = { viewModel.updateForm { s -> s.copy(preWeight = it) } }, placeholder = { Text("Massa Inicial (kg)", color = colors.textSecondary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = textFieldColors)
                TextField(value = formState.postWeight, onValueChange = { viewModel.updateForm { s -> s.copy(postWeight = it) } }, placeholder = { Text("Massa Final (kg)", color = colors.textSecondary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = textFieldColors)
                TextField(value = formState.intake, onValueChange = { viewModel.updateForm { s -> s.copy(intake = it) } }, placeholder = { Text("Fluidos Ingeridos (mL)", color = colors.textSecondary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = textFieldColors)
                TextField(value = formState.duration, onValueChange = { viewModel.updateForm { s -> s.copy(duration = it) } }, placeholder = { Text("Duração (minutos)", color = colors.textSecondary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = textFieldColors)

                Button(
                    onClick = { viewModel.onCalculateClicked(formState.preWeight, formState.postWeight, formState.intake, formState.urine, formState.duration, formState.trainingType, formState.climate) },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues(0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(colors.accentBrush), contentAlignment = Alignment.Center) {
                        Text("CALCULAR", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color.White))
                    }
                }
            }
        }

        AnimatedVisibility(visible = uiState != null, enter = fadeIn() + expandVertically()) {
            uiState?.let { result ->
                val isDanger = result.weightChangePercentage > 2.0
                val isOverIntake = result.overIntakeRisk

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (isDanger) colors.dangerBg else colors.surface)) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("PERFORMANCE RESULT", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = if (isDanger) colors.danger else colors.textSecondary, letterSpacing = 1.sp), modifier = Modifier.padding(bottom = 16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Taxa de Sudorese", style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text("${((result.hourlySweatRateL * 100).toInt() / 100.0)} L/h", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black, color = colors.accentStart)) }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Variação de Massa", style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text("${((result.weightChangePercentage * 100).toInt() / 100.0)} %", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black, color = if (isDanger) colors.danger else colors.textPrimary)) }

                            if (isDanger) {
                                Spacer(Modifier.height(16.dp))
                                Box(modifier = Modifier.fillMaxWidth().background(colors.accentBrush, RoundedCornerShape(12.dp)).padding(16.dp)) {
                                    Text("ALERTA CRÍTICO: Risco severo de desidratação (> 2%).", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (isOverIntake) colors.warningBg else colors.surface)) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("PLANO DE HIDRATAÇÃO", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = if (isOverIntake) colors.warning else colors.textSecondary, letterSpacing = 1.sp), modifier = Modifier.padding(bottom = 16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Meta de Ingestão", style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text("${result.targetIntakeMlPerHour} mL/h", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black, color = colors.success)) }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Fracionamento", style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text(result.fractionationSuggestion, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, color = colors.textPrimary)) }

                            if (isOverIntake) {
                                Spacer(Modifier.height(16.dp))
                                Box(modifier = Modifier.fillMaxWidth().background(colors.warning, RoundedCornerShape(12.dp)).padding(16.dp)) {
                                    Text("ALERTA DE SUPERINGESTÃO: Você ganhou peso ou ingeriu mais líquido do que perdeu. Isso aumenta o risco de hiponatremia.", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(history: List<SweatRateEntity>, userEmail: String) {
    val clipboardManager = LocalClipboardManager.current
    var copiedMessage by remember { mutableStateOf("") }
    val colors = LocalAppColors.current

    val validHistory = history.filter { it.hourlyRateL > 0 }
    val mean = if (validHistory.isNotEmpty()) validHistory.map { it.hourlyRateL }.average() else 0.0
    val sorted = validHistory.map { it.hourlyRateL }.sorted()
    val median = if (sorted.isNotEmpty()) {
        if (sorted.size % 2 == 0) (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0 else sorted[sorted.size / 2]
    } else 0.0

    val byTraining = validHistory.groupBy { it.trainingType.ifBlank { "N/A" } }.mapValues { it.value.map { v -> v.hourlyRateL }.average() }
    val byClimate = validHistory.groupBy { it.climate.ifBlank { "N/A" } }.mapValues { it.value.map { v -> v.hourlyRateL }.average() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("PAINEL LONGITUDINAL", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = colors.textPrimary), modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
        Box(modifier = Modifier.padding(bottom = 16.dp).height(4.dp).width(40.dp).background(colors.accentBrush, CircleShape))

        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("ESTATÍSTICAS GERAIS", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 1.sp), modifier = Modifier.padding(bottom = 16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Média Global", style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text("${((mean * 100).toInt() / 100.0)} L/h", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black, color = colors.accentStart)) }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Mediana", style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text("${((median * 100).toInt() / 100.0)} L/h", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black, color = colors.textPrimary)) }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total de Treinos", style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text("${history.size}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black, color = colors.textPrimary)) }
            }
        }

        if (byTraining.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("VARIABILIDADE POR TREINO", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 1.sp), modifier = Modifier.padding(bottom = 16.dp))
                    byTraining.forEach { (type, avg) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(type, style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text("${((avg * 100).toInt() / 100.0)} L/h", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black, color = colors.textPrimary)) }
                    }
                }
            }
        }

        if (byClimate.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("VARIABILIDADE POR CLIMA", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 1.sp), modifier = Modifier.padding(bottom = 16.dp))
                    byClimate.forEach { (climate, avg) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(climate, style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)); Text("${((avg * 100).toInt() / 100.0)} L/h", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black, color = colors.textPrimary)) }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    val csv = buildString {
                        append("Timestamp,Tipo,Clima,DuracaoMin,TaxaLh\n")
                        validHistory.forEach { append("${it.dateTimestamp},${it.trainingType},${it.climate},${it.durationMin},${it.hourlyRateL}\n") }
                    }
                    clipboardManager.setText(AnnotatedString(csv))
                    copiedMessage = "CSV copiado para a Área de Transferência!"
                },
                modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
            ) { Text("Copiar CSV", color = colors.accentStart) }

            Button(
                onClick = {
                    val summary = buildString {
                        append("Relatório de Performance (São Camilo)\n")
                        append("Média Global: ${((mean * 100).toInt() / 100.0)} L/h\n")
                        append("Mediana: ${((median * 100).toInt() / 100.0)} L/h\n\n")
                        if (byTraining.isNotEmpty()) { append("Por Treino:\n"); byTraining.forEach { append("- ${it.key}: ${((it.value * 100).toInt() / 100.0)} L/h\n") } }
                    }
                    clipboardManager.setText(AnnotatedString(summary))
                    copiedMessage = "Resumo copiado para compartilhamento!"
                },
                modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
            ) { Text("Copiar Resumo", color = colors.accentStart) }
        }

        Button(
            onClick = {
                com.pisc.project.util.exportHistoryToPdf(
                    history = validHistory,
                    userEmail = userEmail,
                    onSuccess = { path -> copiedMessage = "PDF salvo com sucesso!" },
                    onError = { err -> copiedMessage = "Erro: $err" }
                )
            },
            modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).height(50.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = colors.dangerBg, contentColor = colors.danger)
        ) { Text("Exportar para PDF", fontWeight = FontWeight.Bold) }
        
        if (copiedMessage.isNotEmpty()) { Text(copiedMessage, color = colors.success, style = MaterialTheme.typography.labelMedium) }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HistoryScreen(history: List<SweatRateEntity>) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("HISTÓRICO", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = colors.textPrimary), modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
        Box(modifier = Modifier.padding(bottom = 16.dp).height(4.dp).width(40.dp).background(colors.accentBrush, CircleShape))

        if (history.isEmpty()) Text("Nenhum treino registrado ainda.", color = colors.textSecondary)

        history.forEach { session ->
            Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${session.hourlyRateL.toString().take(4)} L/h", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = colors.textPrimary))
                        Text("${session.trainingType.ifEmpty { "Geral" }} | ${session.durationMin} min", style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary))
                    }
                    Surface(shape = CircleShape, color = if(session.isSynced) colors.successBg else colors.accentStart.copy(alpha=0.2f)) {
                        Text(if(session.isSynced) "SYNCED" else "PENDING", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if(session.isSynced) colors.success else colors.accentStart), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }
}