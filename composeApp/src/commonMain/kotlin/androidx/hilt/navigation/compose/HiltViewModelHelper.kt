package androidx.hilt.navigation.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.compose.koinInject

@Composable
inline fun <reified VM : ViewModel> hiltViewModel(): VM {
    return koinInject()
}
