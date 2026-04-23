package com.pisc.project // Ajuste o pacote se o seu for apenas com.pisc.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pisc.project.ui.SweatRateViewModel
import com.pisc.project.data.local.SweatRateDao // IMPORTANTE: Adicionado o import do DAO

@Composable
fun App(dao: SweatRateDao) { // IMPORTANTE: O App agora exige o DAO como parâmetro
    MaterialTheme {
        // IMPORTANTE: Passamos o DAO para a fábrica da ViewModel
        val viewModel: SweatRateViewModel = viewModel { SweatRateViewModel(dao) }

        val uiState by viewModel.uiState.collectAsState()

        // Estados para os campos de texto
        var preWeight by remember { mutableStateOf("") }
        var postWeight by remember { mutableStateOf("") }
        var intake by remember { mutableStateOf("") }
        var urine by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cálculo de Taxa de Sudorese", style = MaterialTheme.typography.headlineMedium)

            // Campos de Entrada Baseados no Escopo
            OutlinedTextField(
                value = preWeight,
                onValueChange = { preWeight = it },
                label = { Text("Massa Corporal Pré-Exercício (kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = postWeight,
                onValueChange = { postWeight = it },
                label = { Text("Massa Corporal Pós-Exercício (kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = intake,
                onValueChange = { intake = it },
                label = { Text("Ingestão de Fluidos (mL)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duração do Treino (minutos)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.onCalculateClicked(preWeight, postWeight, intake, urine, duration)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calcular Taxa")
            }

            // Exibição dos Resultados (Motor de Cálculo)
            uiState?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resultados:", style = MaterialTheme.typography.titleLarge)
                        Text("Taxa de Sudorese: ${((result.hourlySweatRateL * 100).toInt() / 100.0)} L/h")
                        Text("Perda Total: ${((result.totalFluidLossL * 100).toInt() / 100.0)} L")
                        Text("Variação de Massa: ${((result.weightChangePercentage * 100).toInt() / 100.0)} %")

                        if (result.weightChangePercentage > 2.0) {
                            Text(
                                "Alerta: Risco de desidratação excessiva!",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            // No App.kt, dentro da Column, após o uiState?.let { ... }

            val history by viewModel.history.collectAsState()

            if (history.isNotEmpty()) {
                Text(
                    "Histórico de Treinos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )

                history.forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Taxa: ${session.hourlyRateL.toString().take(4)} L/h")
                                Text("Duração: ${session.durationMin} min", style = MaterialTheme.typography.bodySmall)
                            }
                            // Podes formatar o timestamp aqui depois
                            Text("ID: ${session.id}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}