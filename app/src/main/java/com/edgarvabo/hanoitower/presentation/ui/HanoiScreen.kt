package com.edgarvabo.hanoitower.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edgarvabo.hanoitower.domain.model.HanoiSolution
import com.edgarvabo.hanoitower.presentation.HanoiUiState
import com.edgarvabo.hanoitower.presentation.HanoiViewModel
import kotlinx.coroutines.delay

@Composable
fun HanoiScreen(
    viewModel: HanoiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Hanoi Tower Cloud Solver",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when (val state = uiState) {
                    is HanoiUiState.Idle -> {
                        HanoiSelector(onSolve = { viewModel.solveTower(it) })
                    }

                    is HanoiUiState.Loading -> CircularProgressIndicator()
                    is HanoiUiState.Success -> {
                        HanoiVisualizer(
                            solution = state.solution,
                            onBack = { viewModel.resetToIdle() }
                        )
                    }

                    is HanoiUiState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Error: ${state.message}",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.resetToIdle() }) { Text("Reintentar") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HanoiSelector(onSolve: (Int) -> Unit) {
    var diskCount by remember { mutableIntStateOf(3) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Selecciona Discos (3 - 50)", color = Color.Gray)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            FilledIconButton(
                onClick = { if (diskCount > 3) diskCount-- },
                enabled = diskCount > 3
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                diskCount.toString(),
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            FilledIconButton(
                onClick = { if (diskCount < 50) diskCount++ },
                enabled = diskCount < 50
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 20, 50).forEach { level ->
                OutlinedButton(
                    onClick = { diskCount = level },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (diskCount == level) MaterialTheme.colorScheme.primary else Color.DarkGray
                    )
                ) { Text(level.toString()) }
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onSolve(diskCount) },
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.7f)
        ) {
            Text("Resolver $diskCount Discos")
        }
    }
}


@Composable
fun HanoiVisualizer(solution: HanoiSolution, onBack: () -> Unit) {
    BackHandler { onBack() }

    var showStepsDialog by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }
    val isTooComplex = solution.disks > 10

    // Estado de los postes
    var rods by remember {
        mutableStateOf(mapOf(
            "A" to (solution.disks downTo 1).toList(),
            "B" to emptyList<Int>(),
            "C" to emptyList<Int>()
        ))
    }

    var selectedRod by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("Toca un poste para jugar") }
    var userMoves by remember { mutableIntStateOf(0) }
    var isVictory by remember { mutableStateOf(false) }

    // Animación con soporte para DETENER
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            isVictory = false
            rods = mapOf("A" to (solution.disks downTo 1).toList(), "B" to emptyList(), "C" to emptyList())

            for (move in solution.moves) {
                if (!isAnimating) break // Se detiene si el usuario pulsa "Detener"

                delay(500)
                val fromList = rods[move.fromRod].orEmpty().toMutableList()
                val toList = rods[move.toRod].orEmpty().toMutableList()

                if (fromList.isNotEmpty()) {
                    val disk = fromList.removeAt(fromList.lastIndex)
                    toList.add(disk)
                    rods = rods.toMutableMap().apply {
                        this[move.fromRod] = fromList
                        this[move.toRod] = toList
                    }
                }
            }
            if (isAnimating) {
                isAnimating = false
                isVictory = true
                message = "IA: ¡Torre resuelta!"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. HEADER
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            IconButton(onClick = onBack, enabled = !isAnimating) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }

        Text(
            text = if (isAnimating) "La IA está trabajando..." else message,
            color = if (isVictory) Color.Green else Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 2. CUERPO (TORRES O INFO)
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (isTooComplex) {
                // Modo Alto Rendimiento
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("Renderizado deshabilitado (>10)", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                // REPRESENTACIÓN GRÁFICA DE LAS TORRES
                Row(
                    modifier = Modifier.fillMaxWidth().height(320.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    listOf("A", "B", "C").forEach { name ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    enabled = !isAnimating && !isVictory
                                ) {
                                    if (selectedRod == null) {
                                        if (rods[name]?.isNotEmpty() == true) {
                                            selectedRod = name
                                            message = "Elegido poste $name"
                                        }
                                    } else {
                                        val from = selectedRod!!
                                        val disk = rods[from]?.lastOrNull()
                                        val target = rods[name].orEmpty()
                                        if (disk != null && (target.isEmpty() || disk < target.last())) {
                                            rods = rods.toMutableMap().apply {
                                                this[from] = rods[from]!!.toMutableList().apply { removeAt(lastIndex) }
                                                this[name] = target.toMutableList().apply { add(disk) }
                                            }
                                            userMoves++
                                            message = "Movimiento $userMoves"
                                            if (name == "C" && rods[name]?.size == solution.disks) {
                                                isVictory = true
                                                message = "¡Ganaste en $userMoves pasos!"
                                            }
                                        } else message = "❌ Movimiento inválido"
                                        selectedRod = null
                                    }
                                },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            RodComponent(name, rods[name].orEmpty(), solution.disks, selectedRod == name)
                        }
                    }
                }
            }
        }

        // 3. FOOTER (BOTONES)
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isTooComplex) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (isAnimating) {
                        Button(
                            onClick = { isAnimating = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                            modifier = Modifier.weight(1f).padding(4.dp)
                        ) { Text("Detener") }
                    } else {
                        Button(
                            onClick = { isAnimating = true },
                            modifier = Modifier.weight(1f).padding(4.dp)
                        ) { Text("Animar") }
                    }

                    OutlinedButton(
                        onClick = {
                            rods = mapOf("A" to (solution.disks downTo 1).toList(), "B" to emptyList(), "C" to emptyList())
                            isVictory = false; userMoves = 0; message = "Reiniciado"
                        },
                        enabled = !isAnimating,
                        modifier = Modifier.weight(1f).padding(4.dp)
                    ) { Text("Reset") }
                }
            }

            Button(
                onClick = { showStepsDialog = true },
                enabled = !isAnimating,
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) { Text("Mostrar Pasos") }
        }
    }

    // DIÁLOGO DE PASOS (Se mantiene igual)
    if (showStepsDialog) {
        AlertDialog(
            onDismissRequest = { showStepsDialog = false },
            title = { Text("Solución: ${solution.totalMoves} pasos") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    itemsIndexed(solution.moves) { index, move ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("${index + 1}.", Modifier.width(40.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Poste ${move.fromRod} ⮕ ${move.toRod}")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showStepsDialog = false }) { Text("Cerrar") } }
        )
    }
}
@Composable
fun RodComponent(name: String, disks: List<Int>, maxDisks: Int, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            // Poste un poco más grueso (10.dp) para soportar visualmente 10 discos
            Box(
                Modifier
                    .width(10.dp)
                    .height(240.dp) // Un poco más alto
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                        RoundedCornerShape(4.dp)
                    )
            )
            Column(
                modifier = Modifier.height(240.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                disks.reversed().forEach { DiskComponent(it, maxDisks) }
            }
        }
        Text(
            name,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier.padding(top = 8.dp),
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun DiskComponent(size: Int, maxDisks: Int) {
    // 1. Reducimos el ancho base para que los discos pequeños (1, 2, 3) no sean tan grandes.
    val minWidth = 35.dp

    // 2. Calculamos el incremento dinámico.
    // Para 10 discos, un incremento de 8-9dp por nivel es ideal para no salirse del poste.
    val step = if (maxDisks > 7) 9.dp else 20.dp

    // 3. El ancho total ahora escala mejor.
    val diskWidth = minWidth + (size.dp * step.value)

    val diskHeight = if (maxDisks > 7) 18.dp else 26.dp
    val fontSize = if (maxDisks > 8) 10.sp else 12.sp

    val colors = listOf(
        Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFFFA726),
        Color(0xFFAB47BC), Color(0xFF26C6DA), Color(0xFFEF5350)
    )

    Box(
        modifier = Modifier
            .width(diskWidth)
            .height(diskHeight)
            .padding(vertical = 1.dp)
            .background(colors[size % colors.size], RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Mostramos el número solo si no es demasiado pequeño el disco
        Text(
            text = size.toString(),
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}
