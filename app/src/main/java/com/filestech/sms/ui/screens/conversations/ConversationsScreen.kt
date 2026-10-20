package com.filestech.sms.ui.screens.conversations

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filestech.sms.R
import com.filestech.sms.domain.model.Conversation
import com.filestech.sms.ui.components.ContactIntents
import com.filestech.sms.ui.components.ConversationRow
import com.filestech.sms.ui.components.MessagesFloatingNavigation
import com.filestech.sms.ui.components.SmsTechSnackbarHost
import com.filestech.sms.ui.components.showError
import kotlinx.coroutines.launch

enum class SearchCategory(val label: String, val icon: ImageVector) {
    UNREAD("Unread", Icons.Outlined.MarkChatUnread),
    KNOWN("Known", Icons.Outlined.AccountCircle),
    UNKNOWN("Unknown", Icons.Outlined.PersonOff),
    STARRED("Starred", Icons.Outlined.StarOutline),
    IMAGES("Images", Icons.Outlined.Image),
    VIDEOS("Videos", Icons.Outlined.Movie),
    PLACES("Places", Icons.Outlined.Place),
    LINKS("Links", Icons.Outlined.Link),
}

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConversationsScreen(
    archived: Boolean = false,
    onOpenThread: (Long) -> Unit,
    onCompose: () -> Unit,
    onComposeGroup: () -> Unit = {},
    onOpenContacts: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenArchived: () -> Unit,
    onOpenBlocked: () -> Unit,
    onOpenAbout: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    viewModel: ConversationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isPanicDecoy by viewModel.isPanicDecoy.collectAsStateWithLifecycle()
    val messageHits by viewModel.messageHits.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    var selectedFilters by remember { mutableStateOf(setOf<SearchCategory>()) }
    var searchExpanded by remember { mutableStateOf(false) }
    var activeCategoryFilter by remember { mutableStateOf<SearchCategory?>(null) }

    val defaultAppLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.refreshDefaultStatus() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val smsPermsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.requestSyncNow() }
    androidx.compose.runtime.LaunchedEffect(state.isDefaultSmsApp) {
        if (state.isDefaultSmsApp) {
            val needed = listOf(
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.RECEIVE_MMS,
                android.Manifest.permission.READ_CONTACTS,
            ).filter {
                androidx.core.content.ContextCompat.checkSelfPermission(context, it) !=
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            if (needed.isNotEmpty()) {
                smsPermsLauncher.launch(needed.toTypedArray())
            } else {
                viewModel.requestSyncNow()
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDefaultStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var overflowOpen by remember { mutableStateOf(false) }
    var showMarkAllReadConfirm by remember { mutableStateOf(false) }

    var pendingDeleteBlocked by remember {
        mutableStateOf<Conversation?>(null)
    }
    var blockedActionsFor by remember {
        mutableStateOf<Conversation?>(null)
    }
    val snackbarHost = remember { androidx.compose.material3.SnackbarHostState() }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val messagesDErreur = MessagesDErreur(
        blocageEchoue = stringResource(R.string.snack_block_failed),
        suppressionCopieSysteme = stringResource(R.string.conversations_delete_vault_kept_system),
        suppressionLocale = stringResource(R.string.conversations_delete_kept_local),
    )

    val blockPartialFormat = stringResource(R.string.snack_block_partial)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ConversationsViewModel.Event.MovedToVault -> {
                    val msg = if (event.count <= 1) ctx.getString(R.string.vault_move_in_done)
                    else ctx.resources.getQuantityString(
                        R.plurals.vault_bulk_move_in_done, event.count, event.count,
                    )
                    snackbarHost.showSnackbar(msg)
                }
                is ConversationsViewModel.Event.MoveToVaultFailed ->
                    snackbarHost.showError(ctx.getString(R.string.vault_move_in_failed))
                is ConversationsViewModel.Event.Erreur ->
                    snackbarHost.showError(messagesDErreur.pour(event))
                is ConversationsViewModel.Event.BlockPartial ->
                    snackbarHost.showError(
                        String.format(
                            java.util.Locale.getDefault(),
                            blockPartialFormat,
                            event.blocked,
                            event.total,
                        ),
                    )
                is ConversationsViewModel.Event.IAmOkDoneWithSms -> {
                    if (event.sent > 0) {
                        snackbarHost.showSnackbar(ctx.getString(R.string.emergency_i_am_ok_done))
                    } else {
                        snackbarHost.showError(ctx.getString(R.string.emergency_i_am_ok_send_failed))
                    }
                }
                is ConversationsViewModel.Event.IAmOkDoneNoSms ->
                    snackbarHost.showSnackbar(ctx.getString(R.string.emergency_i_am_ok_reset_only))
            }
        }
    }

    // In BackHandler:
    BackHandler(enabled = searchExpanded) {
        if (selectedFilters.isNotEmpty()) {
            selectedFilters = emptySet()
        } else {
            searchExpanded = false
            viewModel.clearQuery()
        }
    }
    BackHandler(enabled = state.selectionMode && !searchExpanded) {
        viewModel.clearSelection()
    }

    Scaffold(
        snackbarHost = { SmsTechSnackbarHost(snackbarHost) },
        topBar = {
            if (state.selectionMode && !isPanicDecoy) {
                TopAppBar(
                    title = {
                        Text(
                            text = androidx.compose.ui.res.pluralStringResource(
                                id = R.plurals.conversations_selection_count,
                                count = state.selectedIds.size,
                                state.selectedIds.size,
                            ),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.action_cancel),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.bulkMoveSelectedToVault() }) {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = stringResource(R.string.bulk_vault_move_in),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface),
                )
            }
        },
        floatingActionButton = {
            if (!state.selectionMode && !searchExpanded) {
                FloatingActionButton(
                    onClick = onCompose,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.action_new_message),
                    )
                }
            }
        },
    ) { padding ->
        val (activeConversations, blockedConversations) =
            state.conversations.partition { !it.blocked }
        val duplicateDisplayNames = state.duplicateDisplayNames

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
        ) {
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val listTopPadding = if (!state.selectionMode) statusBarTop + 68.dp else 8.dp

            when {
                state.isImporting -> ImportingPlaceholder(count = state.importedCount)
                state.isLoading -> Unit
                state.sansAucunResultat(messageHits) && !searchExpanded -> EmptyState(
                    archived = archived,
                    filtered = state.filtered,
                    onCompose = onCompose,
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = listTopPadding,
                        bottom = 112.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    if (!state.isDefaultSmsApp && !archived) {
                        item(key = "banner-default-app") {
                            DefaultAppBanner(onSetDefault = {
                                viewModel.buildChangeDefaultIntent()?.let { intent ->
                                    defaultAppLauncher.launch(intent)
                                }
                            })
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    if (state.showIAmOkChip && !isPanicDecoy) {
                        item(key = "banner-iamok") {
                            BandeauJeVaisBien(
                                visible = true,
                                onConfirmer = viewModel::triggerIAmOk,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    itemsIndexed(
                        activeConversations,
                        key = { _, it -> it.id },
                        contentType = { _, _ -> "row" },
                    ) { index, conv ->
                        val isSelected = state.selectedIds.contains(conv.id)
                        val rowSubtitleNumber = conv.displayName
                            ?.takeIf { it in duplicateDisplayNames && conv.addresses.size == 1 }
                            ?.let { conv.addresses.firstOrNull()?.raw }
                        SwipeableConversationRow(
                            conversation = conv,
                            index = index,
                            count = activeConversations.size,
                            subtitleNumber = rowSubtitleNumber,
                            showAvatars = state.settings.conversations.showAvatars,
                            previewLines = state.settings.conversations.previewLines.coerceIn(1, 2),
                            onOpenThread = {
                                if (state.selectionMode) viewModel.toggleSelection(conv.id)
                                else onOpenThread(conv.id)
                            },
                            onDelete = { viewModel.delete(conv.id) },
                            onBlock = { viewModel.block(conv) },
                            onArchive = { viewModel.toggleArchived(conv) },
                            onMoveToVault = if (isPanicDecoy || state.selectionMode) null else {
                                { viewModel.moveConversationToVault(conv.id) }
                            },
                            rowActions = if (isPanicDecoy || state.selectionMode) {
                                null
                            } else {
                                ConversationRowActions(
                                    onTogglePin = { viewModel.togglePinned(conv) },
                                    onToggleArchive = { viewModel.toggleArchived(conv) },
                                    onToggleMute = { viewModel.toggleMuted(conv) },
                                    onRename = { nom -> viewModel.rename(conv, nom) },
                                )
                            },
                            onSelectMultiple = if (isPanicDecoy) null else {
                                { viewModel.toggleSelection(conv.id) }
                            },
                            selected = isSelected,
                            selectionMode = state.selectionMode,
                        )
                    }

                    if (blockedConversations.isNotEmpty()) {
                        item(key = "header-blocked", contentType = "header") {
                            Text(
                                text = stringResource(R.string.conversations_section_blocked),
                                style = MaterialTheme.typography.labelLarge,
                                color = com.filestech.sms.ui.theme.BrandBlocked,
                                modifier = Modifier.padding(
                                    start = 4.dp,
                                    end = 4.dp,
                                    top = 20.dp,
                                    bottom = 8.dp,
                                ),
                            )
                        }
                        itemsIndexed(
                            blockedConversations,
                            key = { _, it -> it.id },
                            contentType = { _, _ -> "blocked-row" },
                        ) { index, conv ->
                            ConversationRow(
                                conversation = conv,
                                index = index,
                                count = blockedConversations.size,
                                showAvatars = state.settings.conversations.showAvatars,
                                previewLines = state.settings.conversations.previewLines.coerceIn(1, 2),
                                onClick = { onOpenThread(conv.id) },
                                onLongClick = { blockedActionsFor = conv },
                                onDelete = { pendingDeleteBlocked = conv },
                            )
                        }
                    }

                    messageHitsSection(
                        hits = messageHits,
                        showAvatars = state.settings.conversations.showAvatars,
                        selectionMode = state.selectionMode,
                        dividerColor = cs.outlineVariant.copy(alpha = 0.2f),
                        onOpenThread = onOpenThread,
                    )
                }
            }

            // Material 3 Expandable SearchBar
            if (!state.selectionMode) {
                val searchHorizontalPadding by animateDpAsState(
                    targetValue = if (searchExpanded) 0.dp else 16.dp,
                    label = "searchBarPadding",
                )

                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = viewModel.searchInput,
                            onQueryChange = {
                                activeCategoryFilter = null
                                viewModel.setQuery(it)
                            },
                            onSearch = { focusManager.clearFocus() },
                            expanded = searchExpanded,
                            onExpandedChange = { searchExpanded = it },
                            placeholder = {
                                Text(
                                    text = if (archived) stringResource(R.string.tab_archived)
                                    else stringResource(R.string.conversations_search_hint),
                                )
                            },
                            leadingIcon = {
                                if (searchExpanded) {
                                    IconButton(onClick = {
                                        searchExpanded = false
                                        activeCategoryFilter = null
                                        viewModel.clearQuery()
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                            contentDescription = stringResource(R.string.action_back),
                                        )
                                    }
                                } else if (archived && onBack != null) {
                                    IconButton(onClick = onBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                            contentDescription = stringResource(R.string.action_back),
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = cs.onSurfaceVariant,
                                    )
                                }
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (viewModel.searchInput.isNotEmpty() || selectedFilters.isNotEmpty()) {
                                        IconButton(onClick = {
                                            selectedFilters = emptySet()
                                            viewModel.clearQuery()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Outlined.Close,
                                                contentDescription = stringResource(R.string.action_close),
                                            )
                                        }
                                    }

                                    if (!searchExpanded) {
                                        Box {
                                            IconButton(onClick = { overflowOpen = true }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.MoreVert,
                                                    contentDescription = stringResource(R.string.action_more),
                                                    tint = cs.onSurfaceVariant,
                                                )
                                            }

                                            // Popover menu
                                            DropdownMenu(
                                                expanded = overflowOpen,
                                                onDismissRequest = { overflowOpen = false },
                                                containerColor = Color.Transparent,
                                                shadowElevation = 0.dp,
                                                tonalElevation = 0.dp,
                                                border = null,
                                                modifier = Modifier.widthIn(min = 230.dp, max = 270.dp),
                                            ) {
                                                val primaryActions = remember(archived, isPanicDecoy, viewModel.searchInput, selectedFilters) {
                                                    val isSearching = viewModel.searchInput.isNotBlank() || selectedFilters.isNotEmpty()
                                                    buildList {
                                                        if (!archived) {
                                                            if (!isPanicDecoy) {
                                                                add(Triple(Icons.Outlined.Lock, R.string.tab_vault, onOpenVault))
                                                            }
                                                            add(Triple(Icons.Outlined.Inventory2, R.string.tab_archived, onOpenArchived))
                                                        }
                                                        if (!isSearching) {
                                                            add(Triple(Icons.Outlined.Check, R.string.action_mark_all_read) {
                                                                showMarkAllReadConfirm = true
                                                            })
                                                        }
                                                        add(Triple(Icons.Outlined.PersonAdd, R.string.action_create_contact) {
                                                            if (!ContactIntents.createContact(ctx)) {
                                                                scope.launch {
                                                                    snackbarHost.showError(ctx.getString(R.string.phone_action_no_contacts))
                                                                }
                                                            }
                                                        })
                                                        add(Triple(Icons.Outlined.Group, R.string.action_new_group, onComposeGroup))
                                                        add(Triple(Icons.Outlined.Settings, R.string.settings_title, onOpenSettings))
                                                        add(Triple(Icons.Outlined.Info, R.string.about_title, onOpenAbout))
                                                    }
                                                }

                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                                ) {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                                                    ) {
                                                        primaryActions.forEachIndexed { index, (icon, titleRes, action) ->
                                                            SegmentedListItem(
                                                                onClick = {
                                                                    overflowOpen = false
                                                                    action()
                                                                },
                                                                shapes = ListItemDefaults.segmentedShapes(
                                                                    index = index,
                                                                    count = primaryActions.size,
                                                                ),
                                                                colors = ListItemDefaults.segmentedColors(
                                                                    containerColor = cs.surfaceContainerHigh,
                                                                ),
                                                                leadingContent = {
                                                                    Icon(
                                                                        imageVector = icon,
                                                                        contentDescription = null,
                                                                        tint = cs.onSurfaceVariant,
                                                                        modifier = Modifier.size(20.dp),
                                                                    )
                                                                },
                                                                content = {
                                                                    Text(
                                                                        text = stringResource(titleRes),
                                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                                            fontWeight = FontWeight.Medium,
                                                                        ),
                                                                        color = cs.onSurface,
                                                                    )
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                                                modifier = Modifier.fillMaxWidth(),
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = stringResource(R.string.sort_section).uppercase(),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 0.8.sp,
                                                        ),
                                                        color = cs.onSurfaceVariant,
                                                        modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 4.dp),
                                                    )

                                                    val isDateSelected = state.settings.conversations.sortMode == com.filestech.sms.domain.settings.SortMode.DATE
                                                    val isUnreadSelected = state.settings.conversations.sortMode == com.filestech.sms.domain.settings.SortMode.UNREAD_FIRST

                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                                                    ) {
                                                        SegmentedListItem(
                                                            onClick = {
                                                                overflowOpen = false
                                                                viewModel.setSortMode(com.filestech.sms.domain.settings.SortMode.DATE)
                                                            },
                                                            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                                                            colors = ListItemDefaults.segmentedColors(
                                                                containerColor = if (isDateSelected) cs.secondaryContainer
                                                                else cs.surfaceContainerHigh,
                                                            ),
                                                            leadingContent = {
                                                                if (isDateSelected) {
                                                                    Icon(
                                                                        imageVector = Icons.Outlined.Check,
                                                                        contentDescription = null,
                                                                        tint = cs.onSecondaryContainer,
                                                                        modifier = Modifier.size(18.dp),
                                                                    )
                                                                } else {
                                                                    Spacer(modifier = Modifier.size(18.dp))
                                                                }
                                                            },
                                                            content = {
                                                                Text(
                                                                    text = stringResource(R.string.sort_date),
                                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                                        fontWeight = if (isDateSelected) FontWeight.Bold else FontWeight.Normal,
                                                                    ),
                                                                    color = if (isDateSelected) cs.onSecondaryContainer else cs.onSurface,
                                                                )
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                                            modifier = Modifier.fillMaxWidth(),
                                                        )

                                                        SegmentedListItem(
                                                            onClick = {
                                                                overflowOpen = false
                                                                viewModel.setSortMode(com.filestech.sms.domain.settings.SortMode.UNREAD_FIRST)
                                                            },
                                                            shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                                                            colors = ListItemDefaults.segmentedColors(
                                                                containerColor = if (isUnreadSelected) cs.secondaryContainer
                                                                else cs.surfaceContainerHigh,
                                                            ),
                                                            leadingContent = {
                                                                if (isUnreadSelected) {
                                                                    Icon(
                                                                        imageVector = Icons.Outlined.Check,
                                                                        contentDescription = null,
                                                                        tint = cs.onSecondaryContainer,
                                                                        modifier = Modifier.size(18.dp),
                                                                    )
                                                                } else {
                                                                    Spacer(modifier = Modifier.size(18.dp))
                                                                }
                                                            },
                                                            content = {
                                                                Text(
                                                                    text = stringResource(R.string.sort_unread),
                                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                                        fontWeight = if (isUnreadSelected) FontWeight.Bold else FontWeight.Normal,
                                                                    ),
                                                                    color = if (isUnreadSelected) cs.onSecondaryContainer else cs.onSurface,
                                                                )
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                                            modifier = Modifier.fillMaxWidth(),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                        )
                    },
                    expanded = searchExpanded,
                    onExpandedChange = { searchExpanded = it },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .then(if (!searchExpanded) Modifier.statusBarsPadding() else Modifier)
                        .padding(horizontal = searchHorizontalPadding)
                        .padding(bottom = if (searchExpanded) 0.dp else 8.dp),
                    colors = SearchBarDefaults.colors(
                        containerColor = cs.surfaceContainerHigh,
                    ),
                    shadowElevation = if (searchExpanded) 0.dp else 6.dp,
                ) {
                    // Full-screen search content
                    val filteredConversations = remember(selectedFilters, viewModel.searchInput, activeConversations) {
                        var list = activeConversations

                        if (SearchCategory.UNREAD in selectedFilters) {
                            list = list.filter { it.unreadCount > 0 }
                        }
                        if (SearchCategory.KNOWN in selectedFilters) {
                            list = list.filter { !it.displayName.isNullOrBlank() }
                        }
                        if (SearchCategory.UNKNOWN in selectedFilters) {
                            list = list.filter { it.displayName.isNullOrBlank() }
                        }
                        if (SearchCategory.STARRED in selectedFilters) {
                            list = list.filter { it.pinned }
                        }
                        if (SearchCategory.IMAGES in selectedFilters) {
                            list = list.filter {
                                it.lastMessagePreview?.contains("image", ignoreCase = true) == true ||
                                        it.lastMessagePreview?.contains("photo", ignoreCase = true) == true ||
                                        it.lastMessagePreview?.contains("mms", ignoreCase = true) == true
                            }
                        }
                        if (SearchCategory.VIDEOS in selectedFilters) {
                            list = list.filter {
                                it.lastMessagePreview?.contains("video", ignoreCase = true) == true ||
                                        it.lastMessagePreview?.contains("movie", ignoreCase = true) == true
                            }
                        }
                        if (SearchCategory.PLACES in selectedFilters) {
                            list = list.filter {
                                it.lastMessagePreview?.contains("maps.google", ignoreCase = true) == true ||
                                        it.lastMessagePreview?.contains("location", ignoreCase = true) == true ||
                                        it.lastMessagePreview?.contains("place", ignoreCase = true) == true
                            }
                        }
                        if (SearchCategory.LINKS in selectedFilters) {
                            list = list.filter {
                                it.lastMessagePreview?.contains("http://", ignoreCase = true) == true ||
                                        it.lastMessagePreview?.contains("https://", ignoreCase = true) == true ||
                                        it.lastMessagePreview?.contains("www.", ignoreCase = true) == true
                            }
                        }

                        val query = viewModel.searchInput.trim()
                        if (query.isNotEmpty()) {
                            list = list.filter { conv ->
                                conv.title.contains(query, ignoreCase = true) ||
                                        conv.lastMessagePreview?.contains(query, ignoreCase = true) == true ||
                                        conv.addresses.any { it.raw.contains(query) }
                            }
                        }

                        list
                    }


                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    ) {
                        item(key = "search-filter-grid") {
                            SearchFilterGrid(
                                selectedCategories = selectedFilters,
                                onToggleCategory = { category ->
                                    selectedFilters = if (category in selectedFilters) {
                                        selectedFilters - category
                                    } else {
                                        selectedFilters + category
                                    }
                                },
                            )
                        }

                        if (selectedFilters.isEmpty() && viewModel.searchInput.isBlank()) {
                            item(key = "search-hint") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 100.dp, bottom = 60.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(R.string.conversations_search_hint),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = cs.onSurfaceVariant.copy(alpha = 0.6f),
                                    )
                                }
                            }
                        } else if (filteredConversations.isEmpty() && state.sansAucunResultat(messageHits)) {
                            item(key = "search-no-match") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 100.dp, bottom = 60.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(R.string.conversations_no_match),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = cs.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(
                                filteredConversations,
                                key = { _, it -> "search-conv-${it.id}" },
                            ) { index, conv ->
                                val rowSubtitleNumber = conv.displayName
                                    ?.takeIf { it in duplicateDisplayNames && conv.addresses.size == 1 }
                                    ?.let { conv.addresses.firstOrNull()?.raw }

                                val isSelected = state.selectedIds.contains(conv.id)
                                SwipeableConversationRow(
                                    conversation = conv,
                                    index = index,
                                    count = filteredConversations.size,
                                    subtitleNumber = rowSubtitleNumber,
                                    showAvatars = state.settings.conversations.showAvatars,
                                    previewLines = 2,
                                    onOpenThread = {
                                        if (state.selectionMode) {
                                            viewModel.toggleSelection(conv.id)
                                        } else {
                                            searchExpanded = false
                                            selectedFilters = emptySet()
                                            onOpenThread(conv.id)
                                        }
                                    },
                                    onDelete = { viewModel.delete(conv.id) },
                                    onBlock = { viewModel.block(conv) },
                                    onArchive = { viewModel.toggleArchived(conv) },
                                    onMoveToVault = if (isPanicDecoy || state.selectionMode) null else {
                                        { viewModel.moveConversationToVault(conv.id) }
                                    },
                                    rowActions = if (isPanicDecoy || state.selectionMode) {
                                        null
                                    } else {
                                        ConversationRowActions(
                                            onTogglePin = { viewModel.togglePinned(conv) },
                                            onToggleArchive = { viewModel.toggleArchived(conv) },
                                            onToggleMute = { viewModel.toggleMuted(conv) },
                                            onRename = { nom -> viewModel.rename(conv, nom) },
                                        )
                                    },
                                    onSelectMultiple = if (isPanicDecoy) null else {
                                        { viewModel.toggleSelection(conv.id) }
                                    },
                                    selected = isSelected,
                                    selectionMode = state.selectionMode,
                                )
                            }

                            if (viewModel.searchInput.isNotBlank()) {
                                messageHitsSection(
                                    hits = messageHits,
                                    showAvatars = state.settings.conversations.showAvatars,
                                    selectionMode = false,
                                    dividerColor = cs.outlineVariant.copy(alpha = 0.2f),
                                    onOpenThread = { id ->
                                        searchExpanded = false
                                        selectedFilters = emptySet()
                                        onOpenThread(id)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Floating Bottom Navigation
            if (!archived && !state.selectionMode && !searchExpanded) {
                val unreadCount = remember(state.conversations) {
                    state.conversations.count { it.unreadCount > 0 }
                }
                MessagesFloatingNavigation(
                    selectedIndex = 0,
                    onItemSelected = { index ->
                        if (index == 0) {
                            scope.launch { listState.animateScrollToItem(0) }
                        } else if (index == 1) {
                            onOpenContacts()
                        }
                    },
                    unreadConversations = unreadCount,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                )
            }
        }
    }

    BlockedConversationDialogs(
        pendingDelete = pendingDeleteBlocked,
        actionsFor = blockedActionsFor,
        onDismissDelete = { pendingDeleteBlocked = null },
        onDismissActions = { blockedActionsFor = null },
        onConfirmDelete = { id -> pendingDeleteBlocked = null; viewModel.delete(id) },
        onUnblock = { conv -> blockedActionsFor = null; viewModel.unblock(conv) },
        onRequestDelete = { conv -> blockedActionsFor = null; pendingDeleteBlocked = conv },
    )

    if (showMarkAllReadConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMarkAllReadConfirm = false },
            title = { Text(stringResource(R.string.action_mark_all_read)) },
            text = { Text(stringResource(R.string.mark_all_read_confirm_body)) },
            confirmButton = {
                androidx.compose.material3.FilledTonalButton(
                    onClick = {
                        viewModel.markAllAsRead()
                        showMarkAllReadConfirm = false
                    },
                ) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showMarkAllReadConfirm = false },
                ) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

/**
 * 2-Column Search Category Filter Grid supporting multi-selection and animated pill morphing.
 */
@Composable
private fun SearchFilterGrid(
    selectedCategories: Set<SearchCategory>,
    onToggleCategory: (SearchCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember { SearchCategory.entries }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { item ->
                    val isSelected = item in selectedCategories
                    SearchFilterPill(
                        category = item,
                        isSelected = isSelected,
                        onClick = { onToggleCategory(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchFilterPill(
    category: SearchCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    // Smoothly animate the corner radius to a full pill shape when selected
    val animatedRadius by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isSelected) 28.dp else 16.dp,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 240),
        label = "pillCornerMorph",
    )

    // Distinct lighter surface when unselected, secondaryContainer when selected
    val cardBackground = if (isSelected) {
        cs.surfaceBright
    } else {
        cs.secondaryContainer
    }

    val contentColor = if (isSelected) cs.onSecondaryContainer else cs.onSurfaceVariant
    val iconTint = if (isSelected) cs.onSecondaryContainer else cs.onSurface

    Surface(
        shape = RoundedCornerShape(animatedRadius),
        color = cardBackground,
        shadowElevation = if (isSelected) 2.dp else 1.dp,
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(animatedRadius))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SwipeableConversationRow(
    conversation: Conversation,
    index: Int = 0,
    count: Int = 1,
    subtitleNumber: String? = null,
    showAvatars: Boolean,
    previewLines: Int,
    onOpenThread: () -> Unit,
    onDelete: () -> Unit,
    onBlock: () -> Unit,
    onArchive: () -> Unit,
    onMoveToVault: (() -> Unit)? = null,
    onSelectMultiple: (() -> Unit)? = null,
    rowActions: ConversationRowActions? = null,
    selected: Boolean = false,
    selectionMode: Boolean = false,
) {
    val cs = MaterialTheme.colorScheme
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var pendingDelete by remember { mutableStateOf(false) }
    var pendingBlock by remember { mutableStateOf(false) }
    var actionsSheetOpen by remember { mutableStateOf(false) }
    var renameOpen by remember { mutableStateOf(false) }

    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { target ->
            when (target) {
                androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd -> {
                    pendingDelete = true
                    false
                }
                androidx.compose.material3.SwipeToDismissBoxValue.EndToStart -> {
                    onArchive()
                    false
                }
                androidx.compose.material3.SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { distance -> distance * 0.35f },
    )

    val segmentedShapes = ListItemDefaults.segmentedShapes(index = index, count = count)

    androidx.compose.material3.SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !selectionMode,
        enableDismissFromEndToStart = !selectionMode,
        modifier = Modifier.clip(segmentedShapes.shape),
        backgroundContent = {
            val dir = dismissState.dismissDirection
            val (bg, icon, align) = when (dir) {
                androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd ->
                    Triple(com.filestech.sms.ui.theme.BrandDanger, Icons.Outlined.Delete, Alignment.CenterStart)
                androidx.compose.material3.SwipeToDismissBoxValue.EndToStart ->
                    Triple(cs.secondary, Icons.Outlined.Inventory2, Alignment.CenterEnd)
                else -> Triple(cs.surface, Icons.Outlined.Delete, Alignment.Center)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(horizontal = 24.dp),
                contentAlignment = align,
            ) {
                if (dir != androidx.compose.material3.SwipeToDismissBoxValue.Settled) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        },
    ) {
        Box {
            ConversationRow(
                conversation = conversation,
                index = index,
                count = count,
                shapes = segmentedShapes,
                colors = ListItemDefaults.segmentedColors(
                    containerColor = if (selected) cs.primaryContainer.copy(alpha = 0.5f)
                    else cs.surfaceContainerLow,
                ),
                subtitleNumber = subtitleNumber,
                onClick = onOpenThread,
                showAvatars = showAvatars,
                previewLines = previewLines,
                onLongClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    if (selectionMode && onSelectMultiple != null) onSelectMultiple()
                    else actionsSheetOpen = true
                },
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(cs.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.selected),
                        tint = cs.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }

    if (actionsSheetOpen) {
        ConversationActionsSheet(
            onDismiss = { actionsSheetOpen = false },
            onBlockRequested = {
                actionsSheetOpen = false
                pendingBlock = true
            },
            onDeleteRequested = {
                actionsSheetOpen = false
                pendingDelete = true
            },
            onMoveToVaultRequested = onMoveToVault?.let { handler ->
                {
                    actionsSheetOpen = false
                    handler()
                }
            },
            onSelectMultipleRequested = onSelectMultiple?.let { handler ->
                {
                    actionsSheetOpen = false
                    handler()
                }
            },
            onRenameRequested = rowActions?.takeIf { conversation.isGroup }?.let {
                {
                    actionsSheetOpen = false
                    renameOpen = true
                }
            },
            pinned = conversation.pinned,
            archived = conversation.archived,
            muted = conversation.muted,
            onTogglePinRequested = rowActions?.let { a ->
                {
                    actionsSheetOpen = false
                    a.onTogglePin()
                }
            },
            onToggleArchiveRequested = rowActions?.let { a ->
                {
                    actionsSheetOpen = false
                    a.onToggleArchive()
                }
            },
            onToggleMuteRequested = rowActions?.let { a ->
                {
                    actionsSheetOpen = false
                    a.onToggleMute()
                }
            },
        )
    }

    if (renameOpen) {
        com.filestech.sms.ui.components.RenameGroupDialog(
            current = conversation.customName,
            onDismiss = { renameOpen = false },
            onConfirm = { nom ->
                rowActions?.onRename?.invoke(nom)
                renameOpen = false
            },
        )
    }

    if (pendingDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDelete = false },
            title = { Text(stringResource(R.string.conversations_delete_confirm_title)) },
            text = { Text(stringResource(R.string.conversations_delete_confirm_body)) },
            confirmButton = {
                androidx.compose.material3.FilledTonalButton(
                    onClick = {
                        pendingDelete = false
                        onDelete()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = com.filestech.sms.ui.theme.BrandDanger,
                        contentColor = Color.White,
                    ),
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { pendingDelete = false },
                ) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    if (pendingBlock) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingBlock = false },
            title = { Text(stringResource(R.string.conversation_block_confirm_title)) },
            text = { Text(stringResource(R.string.conversation_block_confirm_body)) },
            confirmButton = {
                androidx.compose.material3.FilledTonalButton(
                    onClick = {
                        pendingBlock = false
                        onBlock()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = com.filestech.sms.ui.theme.BrandBlocked,
                        contentColor = com.filestech.sms.ui.theme.onBrandContainer(
                            com.filestech.sms.ui.theme.BrandBlocked,
                        ),
                    ),
                ) { Text(stringResource(R.string.action_block)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { pendingBlock = false },
                ) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    androidx.compose.runtime.LaunchedEffect(pendingDelete, pendingBlock, dismissState.currentValue) {
        if (!pendingDelete && !pendingBlock &&
            dismissState.currentValue != androidx.compose.material3.SwipeToDismissBoxValue.Settled
        ) {
            dismissState.reset()
        }
    }
}

private data class SheetActionItem(
    val titleRes: Int,
    val icon: ImageVector,
    val tint: Color? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConversationActionsSheet(
    onDismiss: () -> Unit,
    onBlockRequested: () -> Unit,
    onDeleteRequested: () -> Unit,
    onMoveToVaultRequested: (() -> Unit)? = null,
    onSelectMultipleRequested: (() -> Unit)? = null,
    onRenameRequested: (() -> Unit)? = null,
    pinned: Boolean = false,
    archived: Boolean = false,
    muted: Boolean = false,
    onTogglePinRequested: (() -> Unit)? = null,
    onToggleArchiveRequested: (() -> Unit)? = null,
    onToggleMuteRequested: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val standardActions = remember(
        onMoveToVaultRequested,
        onRenameRequested,
        onTogglePinRequested,
        onToggleArchiveRequested,
        onToggleMuteRequested,
        onSelectMultipleRequested,
        pinned,
        archived,
        muted,
    ) {
        buildList {
            if (onMoveToVaultRequested != null) {
                add(
                    SheetActionItem(
                        titleRes = R.string.vault_move_in,
                        icon = Icons.Outlined.Lock,
                        onClick = onMoveToVaultRequested,
                    )
                )
            }
            if (onRenameRequested != null) {
                add(
                    SheetActionItem(
                        titleRes = R.string.action_rename_group,
                        icon = Icons.Outlined.Edit,
                        onClick = onRenameRequested,
                    )
                )
            }
            if (onTogglePinRequested != null) {
                add(
                    SheetActionItem(
                        titleRes = if (pinned) R.string.action_unpin else R.string.action_pin,
                        icon = Icons.Outlined.PushPin,
                        onClick = onTogglePinRequested,
                    )
                )
            }
            if (onToggleArchiveRequested != null) {
                add(
                    SheetActionItem(
                        titleRes = if (archived) R.string.action_unarchive else R.string.action_archive,
                        icon = Icons.Outlined.Inventory2,
                        onClick = onToggleArchiveRequested,
                    )
                )
            }
            if (onToggleMuteRequested != null) {
                add(
                    SheetActionItem(
                        titleRes = if (muted) R.string.action_unmute else R.string.action_mute,
                        icon = Icons.Outlined.NotificationsOff,
                        onClick = onToggleMuteRequested,
                    )
                )
            }
            if (onSelectMultipleRequested != null) {
                add(
                    SheetActionItem(
                        titleRes = R.string.bulk_select_multiple,
                        icon = Icons.Outlined.Check,
                        onClick = onSelectMultipleRequested,
                    )
                )
            }
        }
    }

    val dangerActions = remember(onBlockRequested, onDeleteRequested) {
        listOf(
            SheetActionItem(
                titleRes = R.string.action_block,
                icon = Icons.Outlined.Block,
                tint = com.filestech.sms.ui.theme.BrandBlocked,
                onClick = onBlockRequested,
            ),
            SheetActionItem(
                titleRes = R.string.action_delete,
                icon = Icons.Outlined.Delete,
                isDestructive = true,
                onClick = onDeleteRequested,
            ),
        )
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.conversation_actions_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = cs.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            )

            if (standardActions.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    standardActions.forEachIndexed { index, item ->
                        SegmentedListItem(
                            onClick = item.onClick,
                            shapes = ListItemDefaults.segmentedShapes(index = index, count = standardActions.size),
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = cs.surfaceContainerLow,
                            ),
                            leadingContent = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = cs.onSurfaceVariant,
                                )
                            },
                            content = {
                                Text(
                                    text = stringResource(item.titleRes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = cs.onSurface,
                                )
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                dangerActions.forEachIndexed { index, item ->
                    val itemColor = item.tint ?: if (item.isDestructive) cs.error else cs.onSurface
                    SegmentedListItem(
                        onClick = item.onClick,
                        shapes = ListItemDefaults.segmentedShapes(index = index, count = dangerActions.size),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = cs.surfaceContainerLow,
                        ),
                        leadingContent = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = itemColor,
                            )
                        },
                        content = {
                            Text(
                                text = stringResource(item.titleRes),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (item.isDestructive) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = itemColor,
                            )
                        },
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Stable
class ConversationRowActions(
    val onTogglePin: () -> Unit,
    val onToggleArchive: () -> Unit,
    val onToggleMute: () -> Unit,
    val onRename: (String?) -> Unit,
)

@Composable
private fun BandeauJeVaisBien(visible: Boolean, onConfirmer: () -> Unit) {
    if (!visible) return
    var confirmationOuverte by remember { mutableStateOf(false) }
    IAmOkBanner(onClick = { confirmationOuverte = true })
    HorizontalDivider()
    if (!confirmationOuverte) return
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { confirmationOuverte = false },
        title = { Text(stringResource(R.string.emergency_i_am_ok_confirm_title)) },
        text = { Text(stringResource(R.string.emergency_i_am_ok_confirm_body)) },
        confirmButton = {
            Button(onClick = {
                confirmationOuverte = false
                onConfirmer()
            }) { Text(stringResource(R.string.emergency_i_am_ok_chip_action)) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = { confirmationOuverte = false },
            ) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun IAmOkBanner(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val a11yTitle = stringResource(R.string.emergency_i_am_ok_chip_title)
    val a11yAction = stringResource(R.string.emergency_i_am_ok_chip_action)
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(16.dp),
        color = cs.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$a11yTitle. $a11yAction"
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = com.filestech.sms.ui.theme.BrandDanger,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.emergency_i_am_ok_chip_title),
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurface,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(8.dp))
            androidx.compose.material3.FilledTonalButton(
                onClick = onClick,
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary,
                ),
            ) { Text(stringResource(R.string.emergency_i_am_ok_chip_action)) }
        }
    }
}

@Composable
private fun DefaultAppBanner(onSetDefault: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(16.dp),
        color = cs.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.error_not_default_app),
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.size(10.dp))
            androidx.compose.material3.FilledTonalButton(
                onClick = onSetDefault,
                modifier = Modifier.align(Alignment.End),
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary,
                ),
            ) { Text(stringResource(R.string.settings_set_default)) }
        }
    }
}

@Composable
private fun ImportingPlaceholder(count: Int) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.material3.CircularProgressIndicator(color = cs.primary, strokeWidth = 3.dp)
        Text(
            text = stringResource(R.string.conversations_importing_title),
            style = MaterialTheme.typography.titleMedium,
            color = cs.onSurface,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = if (count > 0) stringResource(R.string.conversations_importing_count, count)
            else stringResource(R.string.conversations_importing_body),
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun EmptyState(archived: Boolean, filtered: Boolean, onCompose: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val emblemIcon = when {
        filtered -> Icons.Outlined.SearchOff
        archived -> Icons.Outlined.Inventory2
        else -> Icons.Outlined.ChatBubbleOutline
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(cs.primary.copy(alpha = 0.06f)),
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(cs.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = emblemIcon,
                    contentDescription = null,
                    tint = cs.onPrimaryContainer,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Spacer(Modifier.size(24.dp))
        Text(
            text = when {
                filtered -> stringResource(R.string.conversations_no_match)
                archived -> stringResource(R.string.conversations_archived_empty)
                else -> stringResource(R.string.conversations_empty_title)
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = cs.onSurface,
            textAlign = TextAlign.Center,
        )
        if (!archived && !filtered) {
            Spacer(Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.conversations_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(20.dp))
            androidx.compose.material3.FilledTonalButton(onClick = onCompose) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.action_new_message))
            }
        }
    }
}

@Composable
private fun BlockedConversationDialogs(
    pendingDelete: Conversation?,
    actionsFor: Conversation?,
    onDismissDelete: () -> Unit,
    onDismissActions: () -> Unit,
    onConfirmDelete: (Long) -> Unit,
    onUnblock: (Conversation) -> Unit,
    onRequestDelete: (Conversation) -> Unit,
) {
    pendingDelete?.let { conv ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.conversations_delete_confirm_title)) },
            text = { Text(stringResource(R.string.conversations_delete_confirm_body)) },
            confirmButton = {
                androidx.compose.material3.FilledTonalButton(
                    onClick = { onConfirmDelete(conv.id) },
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = com.filestech.sms.ui.theme.BrandDanger,
                        contentColor = Color.White,
                    ),
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    actionsFor?.let { conv ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissActions,
            title = { Text(stringResource(R.string.conversation_actions_title)) },
            text = { Text(stringResource(R.string.conversation_blocked_label)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { onUnblock(conv) }) {
                    Text(stringResource(R.string.action_unblock))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { onRequestDelete(conv) }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
        )
    }
}