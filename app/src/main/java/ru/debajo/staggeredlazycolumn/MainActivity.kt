package ru.debajo.staggeredlazycolumn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Unfortunately, when StaggeredLazyColumn access to List<T> - this causes many recompositions and recalculations, so that we need to use Stable state
        val items: List<Pair<Color, Dp>> by mutableStateOf(
            (0 until 4000).map {
                Color(
                    Random.nextInt(255),
                    Random.nextInt(255),
                    Random.nextInt(255),
                ) to Random.nextInt(100, 200).dp
            }
        )

        setContent {
            val layoutDirection = LocalLayoutDirection.current
            val topAppBarScrollState = rememberTopAppBarScrollState()
            val scrollBehavior = remember { TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarScrollState) }
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    SmallTopAppBar(
                        title = {
                            Text(
                                text = "Title",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                            )
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )
                }
            ) { contentPadding ->
                StaggeredLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    columns = StaggeredLazyColumnCells.Fixed(4),
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding() + 8.dp,
                        bottom = contentPadding.calculateBottomPadding() + 8.dp,
                        start = contentPadding.calculateStartPadding(layoutDirection) + 16.dp,
                        end = contentPadding.calculateEndPadding(layoutDirection) + 16.dp,
                    )
                ) {
                    item {
                        Item(
                            color = Color.Red,
                            height = 90.dp,
                            name = "Single item"
                        )
                    }

                    items(
                        count = items.size,
                        key = { it },
                        contentType = { "type" },
                        itemContent = { index ->
                            Item(
                                color = items[index].first,
                                height = items[index].second,
                                name = index.toString(),
                            )
                        }
                    )

                    items(
                        count = 5,
                        itemContent = { index ->
                            Item(
                                color = items[index].first,
                                height = items[index].second,
                                name = "Trailing item $index"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Item(color: Color, height: Dp, name: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(24.dp))
            .background(color)
    ) {
        Text(name, Modifier.align(Alignment.Center))
    }
}
