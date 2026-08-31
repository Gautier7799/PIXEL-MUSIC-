@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(onNavigateToSearch: () -> Unit = {}, onNavigateToSettings: () -> Unit = {}) {
    var showSourcesDialog by remember { mutableStateOf(false) }

    if (showSourcesDialog) {
        MusicSourcesDialog(onDismiss = { showSourcesDialog = false })
    }
// ... باقي الكود
