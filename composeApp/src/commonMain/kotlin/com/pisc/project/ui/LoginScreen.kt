package com.pisc.project.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: (String, Boolean) -> Unit) {
    val viewModel = remember { LoginViewModel() }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loggedInEmail by viewModel.loggedInEmail.collectAsState()
    val loggedInTheme by viewModel.loggedInTheme.collectAsState()
    val isSignUpMode by viewModel.isSignUpMode.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Animação de entrada refinada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val colors = LocalAppColors.current

    // Animação suave para a sombra/elevação que se sobrepõe ao fundo
    val animatedElevation by animateDpAsState(
        targetValue = if (visible) 24.dp else 0.dp,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "refinedShadow"
    )

    // Se logou com sucesso, avisa o App.kt passando o email e a preferencia de tema
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && loggedInEmail != null) onLoginSuccess(loggedInEmail!!, loggedInTheme)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)) + 
                    slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(1000, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.94f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 400.dp)
                    .padding(16.dp)
                    .shadow(
                        elevation = animatedElevation,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = colors.accentStart.copy(alpha = 0.25f),
                        spotColor = colors.accentStart.copy(alpha = 0.45f)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Crossfade(targetState = isSignUpMode, modifier = Modifier.fillMaxWidth()) { signUp ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (signUp) "CRIAR CONTA" else "SÃO CAMILO",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                color = colors.textPrimary
                            )
                        )
                        
                        Box(modifier = Modifier.padding(top = 4.dp, bottom = 32.dp).height(4.dp).width(60.dp).background(colors.accentBrush, CircleShape))

                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("E-mail", color = colors.textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colors.bg,
                                unfocusedContainerColor = colors.bg,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                cursorColor = colors.accentStart
                            )
                        )
                        
                        Spacer(Modifier.height(16.dp))

                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Senha", color = colors.textSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colors.bg,
                                unfocusedContainerColor = colors.bg,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                cursorColor = colors.accentStart
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        AnimatedVisibility(visible = error != null) {
                            Text(
                                text = error ?: "", 
                                color = Color(0xFFFF5252),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.onActionClicked(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp), // Importante para o gradient preencher
                            enabled = !isLoading
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (isLoading) SolidColor(Color.Gray) else colors.accentBrush),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (signUp) "CADASTRAR E ENTRAR" else "ACESSAR SISTEMA", 
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        TextButton(onClick = { viewModel.toggleMode() }) {
                            Text(
                                text = if (signUp) "Já tem uma conta? Faça Login" else "Não tem conta? Cadastre-se",
                                color = colors.accentStart,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}