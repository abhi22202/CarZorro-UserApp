package com.example.carzorrouserside.ui.theme.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.homescreen.ApiPackage
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.theme.viewmodel.PackageType
import com.example.carzorrouserside.ui.theme.viewmodel.PackageUiState
import com.example.carzorrouserside.ui.theme.viewmodel.PackageViewModel
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    navController: NavController,
    viewModel: PackageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf(PackageType.POPULAR) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(lazyListState, uiState.isSearching) {
        if (!uiState.isSearching) {
            snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
                .collect { visibleItems ->
                    if (visibleItems.isNotEmpty() && !uiState.isLoading && !uiState.endReached && !uiState.isPaginating) {
                        val lastVisibleItemIndex = visibleItems.last().index
                        if (lastVisibleItemIndex >= uiState.packages.size - 2) {
                            viewModel.loadPackages(selectedTab)
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Our Packages") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabSelector(
                selectedTab = selectedTab,
                onTabSelected = { newTab ->
                    selectedTab = newTab
                    searchQuery = ""
                    viewModel.loadPackages(newTab, isRefresh = true)
                }
            )

            SearchPackagesBar(
                query = searchQuery,
                onQueryChange = { newQuery ->
                    searchQuery = newQuery
                    viewModel.onSearchQueryChanged(newQuery)
                }
            )

            PackageListContent(
                uiState = uiState,
                lazyListState = lazyListState,
                navController = navController,
                searchQuery = searchQuery,
                onRetry = { viewModel.loadPackages(selectedTab, isRefresh = true) }
            )
        }
    }
}

@Composable
fun PackageListContent(
    uiState: PackageUiState,
    lazyListState: LazyListState,
    navController: NavController,
    searchQuery: String,
    onRetry: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.packages.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null && uiState.packages.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
            uiState.packages.isEmpty() && !uiState.isLoading -> {
                Text(
                    text = if (searchQuery.isNotBlank()) "No results found for \"$searchQuery\"" else "No packages found.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(
                        items = uiState.packages,
                        key = { _, item -> item.id }
                    ) { _, packageItem ->
                        ApiPackageCard(
                            packageItem = packageItem,
                            onClick = {
                                navController.navigate(Routes.packageDetailScreen(packageItem.id))
                            }
                        )
                    }

                    if (uiState.isPaginating && !uiState.isSearching) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SearchPackagesBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search packages") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon"
            )
        },
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        singleLine = true
    )
}

@Composable
fun TabSelector(selectedTab: PackageType, onTabSelected: (PackageType) -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            TabItem(
                text = "Popular",
                isSelected = selectedTab == PackageType.POPULAR,
                onClick = { onTabSelected(PackageType.POPULAR) }
            )
            TabItem(
                text = "All",
                isSelected = selectedTab == PackageType.ALL,
                onClick = { onTabSelected(PackageType.ALL) }
            )
        }
    }
}

@Composable
fun RowScope.TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(300),
        label = "tab background color"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "tab text color"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ApiPackageCard(packageItem: ApiPackage, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = packageItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Text(" ${"%.1f".format(packageItem.rating ?: 0.0)} (${packageItem.reviewCount ?: 0} Reviews)", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Image(
                    painter = painterResource(id = packageItem.imageRes),
                    contentDescription = packageItem.name,
                    modifier = Modifier
                        .size(100.dp, 75.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = packageItem.description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${packageItem.displayPrice.roundToInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    packageItem.originalPrice?.let { original ->
                        Text("₹${original.roundToInt()}", style = MaterialTheme.typography.bodyMedium, textDecoration = TextDecoration.LineThrough, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Button(onClick = { onClick() }, shape = RoundedCornerShape(8.dp)) {
                    Text(text = "Select")
                }
            }
        }
    }
}