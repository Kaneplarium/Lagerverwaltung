package kaneplarium.lagerverwaltung.ui.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kaneplarium.lagerverwaltung.data.Article
import kaneplarium.lagerverwaltung.ui.components.NumericKeypad
import kaneplarium.lagerverwaltung.ui.theme.LagerverwaltungTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToEdit: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val articles by viewModel.articles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { stream ->
                viewModel.exportArticlesToCsv(stream)
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { stream ->
                viewModel.importArticlesFromCsv(stream)
            }
        }
    }

    val versionName = "v2026.08.30"

    DashboardScreenContent(
        articles = articles,
        searchQuery = searchQuery,
        versionName = versionName,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onDeleteArticle = { viewModel.deleteArticle(it) },
        onToggleLockStatus = { viewModel.toggleLockStatus(it) },
        onExport = { exportLauncher.launch("Lagerbestand.csv") },
        onImport = { importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/octet-stream")) },
        onNavigateToEdit = onNavigateToEdit,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreenContent(
    articles: List<Article>,
    searchQuery: String,
    versionName: String,
    onSearchQueryChange: (String) -> Unit,
    onDeleteArticle: (String) -> Unit,
    onToggleLockStatus: (Article) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onNavigateToEdit: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var articleForOptions by remember { mutableStateOf<Article?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAddArticleDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteConfirmCode by remember { mutableStateOf("") }
    var deleteConfirmInput by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (showAddArticleDialog) {
        ActionConfirmDialog(
            title = "Artikel hinzufügen?",
            message = "Die Kiste $searchQuery wurde nicht gefunden. Möchten Sie diese neu hinzufügen?",
            confirmLabel = "HINZUFÜGEN",
            onDismiss = {
                showAddArticleDialog = false
            },
            onConfirm = {
                showAddArticleDialog = false
                onNavigateToEdit(searchQuery)
                onSearchQueryChange("")
            }
        )
    }

    if (showConfirmDialog) {
        ActionConfirmDialog(
            title = "Optionen öffnen",
            message = "Möchten Sie die Optionen für Kiste ${articleForOptions?.id} wirklich öffnen?",
            onDismiss = {
                showConfirmDialog = false
                articleForOptions = null
            },
            onConfirm = {
                showConfirmDialog = false
                showOptionsDialog = true
            }
        )
    }

    if (showDeleteConfirmDialog && articleForOptions != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                articleForOptions = null
                deleteConfirmInput = ""
            },
            title = { Text("Löschen bestätigen") },
            text = {
                Column {
                    Text("Bitte geben Sie zur Bestätigung folgende Nummer ein:")
                    Text(
                        text = deleteConfirmCode,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedTextField(
                        value = deleteConfirmInput,
                        onValueChange = { deleteConfirmInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Code eingeben") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deleteConfirmInput == deleteConfirmCode) {
                            val articleId = articleForOptions?.id
                            if (articleId != null) {
                                onDeleteArticle(articleId)
                            }
                            showDeleteConfirmDialog = false
                            articleForOptions = null
                            deleteConfirmInput = ""
                        }
                    },
                    enabled = deleteConfirmInput == deleteConfirmCode,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Green)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        articleForOptions = null
                        deleteConfirmInput = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (showOptionsDialog && articleForOptions != null) {
        AlertDialog(
            onDismissRequest = {
                showOptionsDialog = false
                articleForOptions = null
            },
            title = { Text("Optionen") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Kisten-ID: ${articleForOptions?.id}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                val articleId = articleForOptions?.id
                                showOptionsDialog = false
                                articleForOptions = null
                                onNavigateToEdit(articleId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                        ) {
                            Text("Bearbeiten", fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = {
                                val article = articleForOptions
                                showOptionsDialog = false
                                if (article != null) {
                                    onToggleLockStatus(article)
                                }
                                articleForOptions = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Green, MaterialTheme.shapes.small),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Green)
                        ) {
                            Text(
                                if (articleForOptions?.isLocked == true) "Entsperren" else "Sperren",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = {
                                deleteConfirmCode = (1000..9999).random().toString()
                                showOptionsDialog = false
                                showDeleteConfirmDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Red, MaterialTheme.shapes.small),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Löschen", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showOptionsDialog = false
                        articleForOptions = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Schließen")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LAGERVERWALTUNG",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = versionName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onExport) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Export")
                        }
                        IconButton(onClick = onImport) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Import")
                        }
                    }
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Kisten-ID suchen...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    readOnly = true,
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        bottomBar = {
            NumericKeypad(
                onNumberClick = { number ->
                    onSearchQueryChange(searchQuery + number)
                },
                onDeleteClick = {
                    if (searchQuery.isNotEmpty()) {
                        onSearchQueryChange(searchQuery.dropLast(1))
                    }
                },
                onEnterClick = {
                    if (searchQuery.isNotEmpty()) {
                        val articleExists = articles.any { it.id == searchQuery }
                        if (articleExists) {
                            onNavigateToEdit(searchQuery)
                            onSearchQueryChange("")
                        } else {
                            showAddArticleDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        ArticleList(
            articles = articles,
            onArticleClick = { /* No action */ },
            onShowOptions = { article ->
                articleForOptions = article
                showConfirmDialog = true
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
fun ArticleList(
    articles: List<Article>,
    onArticleClick: (Article) -> Unit,
    onShowOptions: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(articles) { article ->
            ArticleItem(
                article = article,
                onClick = { onArticleClick(article) },
                onSettingsClick = { onShowOptions(article) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleItem(
    article: Article,
    onClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var lockTapCount by remember(article.id) { mutableStateOf(0) }

    Card(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = { /* Disabled long click */ }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (article.isLocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kisten-ID: ${article.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Regal: ${article.shelfNumber}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Platznummer: ${article.compartmentNumber}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (article.umschlagFarbe.isNotEmpty() || article.umschlagGroesse.isNotEmpty()) {
                    Text(
                        text = "Umschlag: ${article.umschlagFarbe} (${article.umschlagGroesse})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (article.isLocked) {
                    IconButton(onClick = {
                        lockTapCount++
                        if (lockTapCount >= 3) {
                            lockTapCount = 0
                            onSettingsClick()
                        }
                    }) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Optionen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Bestätigen",
    dismissLabel: String = "Abbrechen",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Green)
            ) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text(dismissLabel, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardScreenPreview() {
    DashboardScreenContentPreview()
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardScreenContentPreview() {
    LagerverwaltungTheme {
        DashboardScreenContent(
            articles = listOf(
                Article(id = "123456", shelfNumber = "Regal 1", compartmentNumber = "Platz A", umschlagFarbe = "Blau", umschlagGroesse = "C5"),
                Article(id = "789012", shelfNumber = "Regal 2", compartmentNumber = "Platz B", isLocked = true)
            ),
            searchQuery = "123",
            versionName = "v2026.08.30",
            onSearchQueryChange = {},
            onDeleteArticle = {},
            onToggleLockStatus = {},
            onExport = {},
            onImport = {},
            onNavigateToEdit = {}
        )
    }
}
