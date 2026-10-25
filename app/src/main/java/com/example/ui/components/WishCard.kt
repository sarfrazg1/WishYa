package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BirthdayWish
import com.example.model.WishLanguage
import com.example.ui.theme.WishPinkSecondary

private val WhatsAppGreen = Color(0xFF25D366)
private val WhatsAppBg = Color(0xFFE8F9EE)
private val MessengerBlue = Color(0xFF0084FF)
private val MessengerBg = Color(0xFFE5F2FF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WishCard(
    wish: BirthdayWish,
    recipientName: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPersonalizeClick: () -> Unit,
    onWishViewed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showShareSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val personalizedText = wish.personalized(recipientName.ifEmpty { null })
    val textToShare = ShareHelper.formatWishForSharing(personalizedText)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wish_card_${wish.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Category, Language & Style Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Category Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = wish.category.shortName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Style Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = wish.style.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Language Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (wish.language == WishLanguage.ENGLISH) "EN" else "Roman Urdu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Favorite Button
                val heartColor by animateColorAsState(
                    targetValue = if (isFavorite) WishPinkSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(),
                    label = "fav_color"
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("wish_fav_btn_${wish.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = heartColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wish Text Body
            Text(
                text = personalizedText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action row: Personalize Name Button & Sharing Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Personalize Name Button
                OutlinedButton(
                    onClick = onPersonalizeClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("personalize_btn_${wish.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Personalize",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (recipientName.isNotEmpty()) recipientName else "Add Name",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Sharing Buttons Group: WhatsApp, Messenger/SMS, Copy, Share Sheet
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. WhatsApp Direct Share
                    FilledTonalIconButton(
                        onClick = {
                            ShareHelper.shareToWhatsApp(context, textToShare)
                            onWishViewed()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("whatsapp_btn_${wish.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = WhatsAppBg,
                            contentColor = WhatsAppGreen
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Share on WhatsApp",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 2. Messenger / SMS Direct Share
                    FilledTonalIconButton(
                        onClick = {
                            ShareHelper.shareToMessenger(context, textToShare)
                            onWishViewed()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("messenger_btn_${wish.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MessengerBg,
                            contentColor = MessengerBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "Share on Messenger or SMS",
                            tint = MessengerBlue,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 3. Copy to Clipboard
                    FilledTonalIconButton(
                        onClick = {
                            ShareHelper.copyToClipboard(context, personalizedText)
                            onWishViewed()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("copy_btn_${wish.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Wish",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 4. In-App Share Sheet with WhatsApp, Messenger & All Apps
                    FilledTonalIconButton(
                        onClick = {
                            showShareSheet = true
                            onWishViewed()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("share_btn_${wish.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Options",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        ShareWishBottomSheet(
            wishText = personalizedText,
            onDismiss = { showShareSheet = false }
        )
    }
}
