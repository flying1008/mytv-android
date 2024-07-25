package top.yogiczy.mytv.tv.ui.screens.epg

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.toPersistentList
import top.yogiczy.mytv.core.data.entities.channel.Channel
import top.yogiczy.mytv.core.data.entities.epg.Epg
import top.yogiczy.mytv.core.data.entities.epg.EpgProgramme
import top.yogiczy.mytv.core.data.entities.epg.EpgProgrammeList
import top.yogiczy.mytv.tv.ui.material.Drawer
import top.yogiczy.mytv.tv.ui.material.DrawerPosition
import top.yogiczy.mytv.tv.ui.screens.components.rememberScreenAutoCloseState
import top.yogiczy.mytv.tv.ui.screens.epg.components.EpgDayItemList
import top.yogiczy.mytv.tv.ui.screens.epg.components.EpgProgrammeItemList
import top.yogiczy.mytv.tv.ui.theme.MyTVTheme
import top.yogiczy.mytv.tv.ui.tooling.PreviewWithLayoutGrids
import top.yogiczy.mytv.tv.ui.utils.captureBackKey
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EpgScreen(
    modifier: Modifier = Modifier,
    epgProvider: () -> Epg = { Epg() },
    onEpgProgrammePlayback: (EpgProgramme) -> Unit = {},
    onEpgProgrammeReserve: (EpgProgramme) -> Unit = {},
    onClose: () -> Unit = {},
) {
    val screenAutoCloseState = rememberScreenAutoCloseState(onTimeout = onClose)

    val dateFormat = SimpleDateFormat("E MM-dd", Locale.getDefault())
    val epg = epgProvider()
    val programDayGroup = epg.programmeList.groupBy { dateFormat.format(it.startAt) }
    var currentDay by remember { mutableStateOf(dateFormat.format(System.currentTimeMillis())) }

    Drawer(
        modifier = modifier.captureBackKey { onClose() },
        onDismissRequest = onClose,
        position = DrawerPosition.Start,
        header = { Text("节目单") },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EpgProgrammeItemList(
                modifier = Modifier.width(268.dp),
                epgProgrammeListProvider = {
                    EpgProgrammeList(programDayGroup.getOrElse(currentDay) { emptyList() })
                },
                onPlayback = onEpgProgrammePlayback,
                onReserve = onEpgProgrammeReserve,
                onUserAction = { screenAutoCloseState.active() },
            )

            if (programDayGroup.size > 1) {
                EpgDayItemList(
                    modifier = Modifier.width(80.dp),
                    dayListProvider = { programDayGroup.keys.toPersistentList() },
                    currentDayProvider = { currentDay },
                    onDaySelected = { currentDay = it },
                    onUserAction = { screenAutoCloseState.active() },
                )
            }
        }
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun EpgScreenPreview() {
    MyTVTheme {
        PreviewWithLayoutGrids {
            EpgScreen(
                epgProvider = { Epg.example(Channel.EXAMPLE) },
            )
        }
    }
}