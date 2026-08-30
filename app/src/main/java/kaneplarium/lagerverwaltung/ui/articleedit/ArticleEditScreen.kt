package kaneplarium.lagerverwaltung.ui.articleedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kaneplarium.lagerverwaltung.ui.components.NumericKeypad
import kaneplarium.lagerverwaltung.ui.theme.LagerverwaltungTheme

enum class FocusedField {
    NONE, ID, SHELF, COMPARTMENT, UMSCHLAG_FARBE, UMSCHLAG_GROESSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleEditScreen(
    viewModel: ArticleEditViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var focusedField by remember { mutableStateOf(FocusedField.ID) }

    val handleNumberClick = { number: String ->
        when (focusedField) {
            FocusedField.ID -> viewModel.onIdChange(uiState.id + number)
            FocusedField.SHELF -> viewModel.onShelfNumberChange(uiState.shelfNumber + number)
            FocusedField.COMPARTMENT -> viewModel.onCompartmentNumberChange(uiState.compartmentNumber + number)
            FocusedField.UMSCHLAG_FARBE -> viewModel.onUmschlagFarbeChange(uiState.umschlagFarbe + number)
            FocusedField.UMSCHLAG_GROESSE -> viewModel.onUmschlagGroesseChange(uiState.umschlagGroesse + number)
            FocusedField.NONE -> {}
        }
    }

    val handleDeleteClick = {
        when (focusedField) {
            FocusedField.ID -> if (uiState.id.isNotEmpty()) viewModel.onIdChange(
                uiState.id.dropLast(
                    1
                )
            )

            FocusedField.SHELF -> if (uiState.shelfNumber.isNotEmpty()) viewModel.onShelfNumberChange(
                uiState.shelfNumber.dropLast(1)
            )

            FocusedField.COMPARTMENT -> if (uiState.compartmentNumber.isNotEmpty()) viewModel.onCompartmentNumberChange(
                uiState.compartmentNumber.dropLast(1)
            )

            FocusedField.UMSCHLAG_FARBE -> if (uiState.umschlagFarbe.isNotEmpty()) viewModel.onUmschlagFarbeChange(
                uiState.umschlagFarbe.dropLast(1)
            )

            FocusedField.UMSCHLAG_GROESSE -> if (uiState.umschlagGroesse.isNotEmpty()) viewModel.onUmschlagGroesseChange(
                uiState.umschlagGroesse.dropLast(1)
            )

            FocusedField.NONE -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Artikel bearbeiten" else "Neuer Artikel") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NumericKeypad(
                onNumberClick = handleNumberClick,
                onDeleteClick = handleDeleteClick,
                onEnterClick = {
                    when (focusedField) {
                        FocusedField.ID -> if (!uiState.isEditing) focusedField = FocusedField.SHELF else focusedField = FocusedField.UMSCHLAG_FARBE
                        FocusedField.SHELF -> focusedField = FocusedField.COMPARTMENT
                        FocusedField.COMPARTMENT -> focusedField = FocusedField.UMSCHLAG_FARBE
                        FocusedField.UMSCHLAG_FARBE -> focusedField = FocusedField.UMSCHLAG_GROESSE
                        else -> viewModel.saveArticle(onSuccess = onNavigateBack)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ArticleEditForm(
                viewModel = viewModel,
                onSaveSuccess = onNavigateBack,
                focusedField = focusedField,
                onFocusChange = { focusedField = it },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun ArticleEditForm(
    viewModel: ArticleEditViewModel,
    onSaveSuccess: () -> Unit,
    focusedField: FocusedField = FocusedField.NONE,
    onFocusChange: (FocusedField) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                EditField(
                    value = uiState.id,
                    label = "Kisten-ID",
                    icon = Icons.Default.Numbers,
                    isError = uiState.idError != null,
                    supportingText = uiState.idError,
                    enabled = !uiState.isEditing,
                    isFocused = focusedField == FocusedField.ID,
                    onClick = { onFocusChange(FocusedField.ID) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EditField(
                        value = uiState.shelfNumber,
                        label = "Regal",
                        icon = Icons.Default.Archive,
                        isFocused = focusedField == FocusedField.SHELF,
                        modifier = Modifier.weight(1f),
                        onClick = { onFocusChange(FocusedField.SHELF) }
                    )

                    EditField(
                        value = uiState.compartmentNumber,
                        label = "Platz",
                        icon = Icons.Default.Place,
                        isFocused = focusedField == FocusedField.COMPARTMENT,
                        modifier = Modifier.weight(1f),
                        onClick = { onFocusChange(FocusedField.COMPARTMENT) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EditField(
                        value = uiState.umschlagFarbe,
                        label = "Umschlagfarbe",
                        icon = Icons.Default.Archive, // Using Archive as placeholder icon
                        isFocused = focusedField == FocusedField.UMSCHLAG_FARBE,
                        modifier = Modifier.weight(2f),
                        onClick = { onFocusChange(FocusedField.UMSCHLAG_FARBE) }
                    )

                    EditField(
                        value = uiState.umschlagGroesse,
                        label = "Größe",
                        icon = Icons.Default.Numbers,
                        isFocused = focusedField == FocusedField.UMSCHLAG_GROESSE,
                        modifier = Modifier.weight(1f),
                        onClick = { onFocusChange(FocusedField.UMSCHLAG_GROESSE) }
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.saveArticle(onSuccess = onSaveSuccess) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isSaving,
            shape = MaterialTheme.shapes.large
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(24.dp)
                        .width(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                if (uiState.isEditing) "Aktualisieren" else "Speichern",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun EditField(
    value: String,
    label: String,
    icon: ImageVector,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { if (it.isFocused) onClick() },
        leadingIcon = { Icon(icon, contentDescription = null) },
        isError = isError,
        supportingText = { supportingText?.let { Text(it) } },
        enabled = enabled,
        readOnly = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            focusedLabelColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ArticleEditScreenPreview() {
    LagerverwaltungTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Artikel hinzufügen") },
                    navigationIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = "ART123",
                    onValueChange = {},
                    label = { Text("Kisten-ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "Shelf 1",
                    onValueChange = {},
                    label = { Text("Regal") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "Comp 2",
                    onValueChange = {},
                    label = { Text("Platznummer") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Artikel speichern")
                }
            }
        }
    }
}
