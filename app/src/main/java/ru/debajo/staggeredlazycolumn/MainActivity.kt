package ru.debajo.staggeredlazycolumn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val items = (0 until 4000).map {
            Color(
                Random.nextInt(255),
                Random.nextInt(255),
                Random.nextInt(255),
            ) to Random.nextInt(100, 200).dp
        }

        setContent {
            StaggeredLazyColumn(
                modifier = Modifier.fillMaxSize(),
                columns = 4,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                )
            ) {
                item {
                    Item(
                        color = Color.White,
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
