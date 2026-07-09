package me.cniekirk.trainy.feature.stationdetails

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration

@Composable
internal fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val linkStyles =
        TextLinkStyles(
            style =
                SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                )
        )
    val annotatedString: AnnotatedString =
        remember(html, linkStyles) {
            AnnotatedString.fromHtml(
                htmlString = html,
                linkStyles = linkStyles,
            )
        }
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = color,
    )
}
