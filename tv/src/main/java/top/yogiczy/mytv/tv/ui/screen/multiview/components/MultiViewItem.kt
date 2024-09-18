package top.yogiczy.mytv.tv.ui.screen.multiview.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import top.yogiczy.mytv.core.data.entities.channel.Channel
import top.yogiczy.mytv.core.data.entities.channel.ChannelGroupList
import top.yogiczy.mytv.core.data.entities.epg.EpgList
import top.yogiczy.mytv.tv.ui.material.SimplePopup
import top.yogiczy.mytv.tv.ui.screen.channels.ChannelsScreen
import top.yogiczy.mytv.tv.ui.screen.search.SearchScreen
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.VideoPlayerScreen
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.rememberVideoPlayerState
import top.yogiczy.mytv.tv.ui.utils.handleKeyEvents

@Composable
fun MultiViewItem(
    modifier: Modifier = Modifier,
    channelGroupListProvider: () -> ChannelGroupList = { ChannelGroupList() },
    epgListProvider: () -> EpgList = { EpgList() },
    channelProvider: () -> Channel = { Channel() },
    viewIndexProvider: () -> Int = { 0 },
    viewCountProvider: () -> Int = { 0 },
    zoomInIndexProvider: () -> Int? = { null },
    onAddChannel: (Channel) -> Unit = {},
    onRemoveChannel: (Channel) -> Unit = {},
    onChangeChannel: (Channel) -> Unit = {},
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
    onMoveTo: (Int) -> Unit = {},
) {
    val channel = channelProvider()

    var actionsVisible by remember { mutableStateOf(false) }
    var moveVisible by remember { mutableStateOf(false) }
    var addChannelVisible by remember { mutableStateOf(false) }
    var searchAndAddChannelVisible by remember { mutableStateOf(false) }
    var changeChannelVisible by remember { mutableStateOf(false) }

    val videoPlayerState = rememberVideoPlayerState()
    LaunchedEffect(channel) {
        // TODO 获取最优线路
        videoPlayerState.prepare(channel.lineList.first())
    }

    Surface(
        modifier = modifier
            .handleKeyEvents(
                onSelect = { actionsVisible = true },
                onLongSelect = { moveVisible = true },
            ),
        onClick = {},
        colors = ClickableSurfaceDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                BorderStroke(4.dp, MaterialTheme.colorScheme.onSurface),
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        VideoPlayerScreen(state = videoPlayerState)

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (!videoPlayerState.isPlaying) {
                Icon(
                    Icons.Filled.PauseCircle,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                )
            }

            if (videoPlayerState.volume == 0f) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                )
            }
        }
    }

    SimplePopup(
        visibleProvider = { actionsVisible },
        onDismissRequest = { actionsVisible = false },
    ) {
        MultiViewItemActions(
            onDismissRequest = { actionsVisible = false },
            viewIndexProvider = viewIndexProvider,
            viewCountProvider = viewCountProvider,
            isZoomInProvider = { zoomInIndexProvider() == viewIndexProvider() },
            isPlayingProvider = { videoPlayerState.isPlaying },
            isMutedProvider = { videoPlayerState.volume == 0f },
            onAddChannel = {
                addChannelVisible = true
                actionsVisible = false
            },
            onSearchAndAddChannel = {
                searchAndAddChannelVisible = true
                actionsVisible = false
            },
            onChangeChannel = {
                changeChannelVisible = true
                actionsVisible = false
            },
            onRemoveChannel = {
                onRemoveChannel(channel)
                actionsVisible = false
            },
            onViewZoomIn = {
                onZoomIn()
                actionsVisible = false
            },
            onViewZoomOut = {
                onZoomOut()
                actionsVisible = false
            },
            onVideoPlayerPlay = {
                videoPlayerState.play()
                actionsVisible = false
            },
            onVideoPlayerPause = {
                videoPlayerState.pause()
                actionsVisible = false
            },
            onVideoPlayerMute = {
                videoPlayerState.volume = 0f
                actionsVisible = false
            },
            onVideoPlayerUnMute = {
                videoPlayerState.volume = 1f
                actionsVisible = false
            },
            onVideoPlayerReload = {
                // TODO 获取最优线路
                videoPlayerState.prepare(channel.lineList.first())
                actionsVisible = false
            },
        )
    }

    SimplePopup(
        visibleProvider = { moveVisible },
        onDismissRequest = { moveVisible = false },
    ) {
        MultiViewItemMove(
            onDismissRequest = { moveVisible = false },
            viewCountProvider = viewCountProvider,
            viewIndexProvider = viewIndexProvider,
            zoomInIndexProvider = zoomInIndexProvider,
            onMoveTo = {
                onMoveTo(it)
                moveVisible = false
            },
        )
    }

    SimplePopup(
        visibleProvider = { addChannelVisible },
        onDismissRequest = { addChannelVisible = false },
    ) {
        ChannelsScreen(
            channelGroupListProvider = channelGroupListProvider,
            onChannelSelected = {
                onAddChannel(it)
                addChannelVisible = false
            },
            epgListProvider = epgListProvider,
            onBackPressed = { addChannelVisible = false },
        )
    }

    SimplePopup(
        visibleProvider = { searchAndAddChannelVisible },
        onDismissRequest = { searchAndAddChannelVisible = false },
    ) {
        SearchScreen(
            channelGroupListProvider = channelGroupListProvider,
            onChannelSelected = {
                onAddChannel(it)
                searchAndAddChannelVisible = false
            },
            epgListProvider = epgListProvider,
            onBackPressed = { searchAndAddChannelVisible = false },
        )
    }

    SimplePopup(
        visibleProvider = { changeChannelVisible },
        onDismissRequest = { changeChannelVisible = false },
    ) {
        ChannelsScreen(
            channelGroupListProvider = channelGroupListProvider,
            onChannelSelected = {
                onChangeChannel(it)
                changeChannelVisible = false
            },
            epgListProvider = epgListProvider,
            onBackPressed = { changeChannelVisible = false },
        )
    }
}