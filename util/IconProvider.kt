package com.achievemeaalk.freedjf.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.achievemeaalk.freedjf.R

object IconProvider {
    val allIcons: Map<String, ImageVector> = mapOf(
        "fastfood" to Icons.Default.Fastfood,
        "attach_money" to Icons.Default.AttachMoney,
        "directions_car" to Icons.Default.DirectionsCar,
        "work" to Icons.Default.Work,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "trending_up" to Icons.Default.TrendingUp,
        "new_releases" to Icons.Default.NewReleases,
        "auto_awesome" to Icons.Default.AutoAwesome,
        "paid" to Icons.Default.Paid,
        "pets" to Icons.Default.Pets,
        "sports_esports" to Icons.Default.SportsEsports,
        "fitness_center" to Icons.Default.FitnessCenter,
        "developer_mode" to Icons.Default.DeveloperMode,
        "music_note" to Icons.Default.MusicNote,
        "school" to Icons.Default.School,
        "hotel" to Icons.Default.Hotel,
        "local_hospital" to Icons.Default.LocalHospital,
        "local_bar" to Icons.Default.LocalBar,
        "local_gas_station" to Icons.Default.LocalGasStation,
        "local_laundry_service" to Icons.Default.LocalLaundryService,
        "phone" to Icons.Default.Phone,
        "flight" to Icons.Default.Flight,
        "train" to Icons.Default.Train,
        "restaurant" to Icons.Default.Restaurant
    )

    @Composable
    fun getIconPainter(name: String): Painter {
        val icon = allIcons[name]
        return when {
            icon is ImageVector -> androidx.compose.ui.graphics.vector.rememberVectorPainter(image = icon)
            else -> painterResource(id = R.drawable.ic_launcher)
        }
    }

    fun getIcon(name: String): ImageVector {
        return allIcons[name] ?: Icons.Default.Error
    }
} 