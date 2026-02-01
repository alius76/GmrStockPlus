package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstockplus.data.getAuthRepository
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.presentation.screens.logic.LoginScreenLogic
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary

class LoginScreen(
    private val onLoginSuccess: (User) -> Unit
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val authRepository = remember { getAuthRepository() }
        val logic = remember { LoginScreenLogic(authRepository) }

        // Estados reactivos de la lógica
        val email by logic.email.collectAsState()
        val password by logic.password.collectAsState()
        val isLoading by logic.isLoading.collectAsState()
        val errorMessage by logic.errorMessage.collectAsState()
        val user by logic.user.collectAsState()
        val rememberMe by logic.rememberMe.collectAsState() // 👈 Nuevo estado

        var isPasswordVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            logic.checkExistingSession()
        }

        LaunchedEffect(user) {
            user?.let { onLoginSuccess(it) }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBFBFB)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 400.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                // ICONO DE CABECERA
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = PrimaryColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Recycling,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = PrimaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "GMR STOCK +",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryColor,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Gestión de Reciclaje P07 / P08",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(48.dp))

                // CAMPO EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = logic::updateEmail,
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = PrimaryColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryColor,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // CAMPO CONTRASEÑA
                OutlinedTextField(
                    value = password,
                    onValueChange = logic::updatePassword,
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (isPasswordVisible) PrimaryColor else Color.Gray
                            )
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryColor,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                // 🔘 CHECKBOX: RECORDAR CORREO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { logic.updateRememberMe(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryColor,
                            uncheckedColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "Recordar mi correo",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable { logic.updateRememberMe(!rememberMe) }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 16.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // BOTÓN ENTRAR
                Button(
                    onClick = { logic.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "ACCEDER AL SISTEMA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "v1.0.0 build 1 | GMR Stock Team",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        }
    }
}