package com.alius.gmrstockplus.ui.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import com.alius.gmrstockplus.data.ClientRepository
import com.alius.gmrstockplus.data.LoteRepository
import com.alius.gmrstockplus.domain.model.BigBags
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.domain.model.CertificadoStatus
import com.alius.gmrstockplus.domain.model.LoteModel
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.withContext
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LotesBottomSheetContent(
    loteRepository: LoteRepository,
    clientRepository: ClientRepository,
    databaseUrl: String,
    currentUserEmail: String,
    onViewBigBags: (List<BigBags>) -> Unit,
    onRemarkUpdated: (LoteModel) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var searchText by remember { mutableStateOf("") }
    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // BUSCADOR con debounce y filtro contains
    LaunchedEffect(searchText) {
        isLoading = true
        kotlinx.coroutines.delay(300)

        if (searchText.isBlank()) {
            lotes = emptyList()
            isLoading = false
            println("Lotes: Filtro vacío. Lista vaciada.")
            return@LaunchedEffect
        }


        val allLotes = withContext(Dispatchers.IO) {
            try {
                loteRepository.listarLotes("")
            } catch (e: Exception) {
                emptyList()
            }
        }


        lotes = allLotes.filter { it.number.contains(searchText, ignoreCase = true) }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {

        // 🔍 CAJA DE BÚSQUEDA (solo números)
        OutlinedTextField(
            value = searchText,
            onValueChange = { newValue ->
                val newSearchText = newValue.filter { it.isDigit() }
                searchText = newSearchText
            },
            placeholder = { Text("Buscar lote por número") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            shape = RoundedCornerShape(12.dp),


            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide() // Cierra el teclado
                }
            ),

            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterHorizontally),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    Modifier.fillMaxWidth().height(420.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            lotes.isEmpty() && searchText.isBlank() -> {
                Box(
                    Modifier.fillMaxWidth().height(420.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔎 Ingrese el número de lote para buscar.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            lotes.isEmpty() && searchText.isNotBlank() -> {
                Box(
                    Modifier.fillMaxWidth().height(420.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron lotes para \"$searchText\"",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            else -> {
                val pagerState = rememberPagerState(initialPage = 0) { lotes.size }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {

                    // PAGER
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 60.dp)
                    ) { index ->
                        val lote = lotes[index]
                        val cert: Certificado? = null
                        val certColor = when (cert?.status) {
                            CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
                            CertificadoStatus.CORRECTO -> PrimaryColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        val pageOffset = pagerState.currentPage - index + pagerState.currentPageOffsetFraction
                        val scale by animateFloatAsState(
                            targetValue = lerp(0.85f, 1f, 1f - abs(pageOffset)),
                            animationSpec = tween(300)
                        )
                        val alpha by animateFloatAsState(
                            targetValue = lerp(0.55f, 1f, 1f - abs(pageOffset)),
                            animationSpec = tween(300)
                        )
                        val translation by animateFloatAsState(
                            targetValue = pageOffset * 40f,
                            animationSpec = tween(300)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    translationY = translation
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            LoteCard(
                                lote = lote,
                                certificado = cert,
                                certificadoIconColor = certColor,
                                modifier = Modifier.fillMaxWidth(0.85f),
                                scope = scope,
                                snackbarHostState = snackbarHostState,
                                onViewBigBags = onViewBigBags,
                                databaseUrl = databaseUrl,
                                onRemarkUpdated = { updated ->
                                    lotes = lotes.map { if (it.id == updated.id) updated else it }
                                    onRemarkUpdated(updated)
                                },
                                clientRepository = clientRepository,
                                currentUserEmail = currentUserEmail
                            )
                        }
                    }

                    // BARRA VERTICAL DE PROGRESO
                    val barWidth = 4.dp
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(barWidth)
                            .align(Alignment.CenterEnd)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {

                        if (lotes.size > 1) {
                            val progress = (pagerState.currentPage + pagerState.currentPageOffsetFraction) / (lotes.size - 1).toFloat()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(progress.coerceIn(0f, 1f))
                                    .background(PrimaryColor, shape = CircleShape)
                            )
                        } else {

                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}