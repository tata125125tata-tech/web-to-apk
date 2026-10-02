package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.data.model.WebFileType
import com.example.ui.theme.SyntaxAttr
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxTag

class CodeSyntaxVisualTransformation(private val fileType: WebFileType) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val builder = AnnotatedString.Builder(raw)

        when (fileType) {
            WebFileType.HTML -> highlightHtml(raw, builder)
            WebFileType.CSS -> highlightCss(raw, builder)
            WebFileType.JAVASCRIPT -> highlightJs(raw, builder)
            WebFileType.JSON -> highlightJson(raw, builder)
            else -> highlightGeneric(raw, builder)
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    private fun highlightHtml(text: String, builder: AnnotatedString.Builder) {
        // Comments <!-- ... -->
        Regex("<!--[\\s\\S]*?-->").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxComment), m.range.first, m.range.last + 1)
        }
        // HTML Tags <div, </div, >
        Regex("</?[a-zA-Z0-9_-]+|/?>").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxTag, fontWeight = FontWeight.Bold), m.range.first, m.range.last + 1)
        }
        // Attributes class=, id=, src=
        Regex("\\b[a-zA-Z0-9_-]+(?==)").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxAttr), m.range.first, m.range.last + 1)
        }
        // Quoted strings "..." or '...'
        Regex("\"[^\"]*\"|'[^']*'").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxString), m.range.first, m.range.last + 1)
        }
    }

    private fun highlightCss(text: String, builder: AnnotatedString.Builder) {
        // Comments /* ... */
        Regex("/\\*[\\s\\S]*?\\*/").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxComment), m.range.first, m.range.last + 1)
        }
        // Selectors and properties
        Regex("\\b[a-zA-Z-]+(?=:)").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxAttr), m.range.first, m.range.last + 1)
        }
        // Values and units
        Regex("\\b\\d+(px|em|rem|%|vh|vw|s|ms)?\\b").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxNumber), m.range.first, m.range.last + 1)
        }
        // Hex colors #fff
        Regex("#[0-9a-fA-F]{3,8}\\b").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxKeyword), m.range.first, m.range.last + 1)
        }
    }

    private fun highlightJs(text: String, builder: AnnotatedString.Builder) {
        // Line & Block Comments
        Regex("//.*|/\\*[\\s\\S]*?\\*/").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxComment), m.range.first, m.range.last + 1)
        }
        // Keywords
        val keywords = "\\b(const|let|var|function|return|if|else|for|while|import|export|class|new|async|await|try|catch|document|window)\\b"
        Regex(keywords).findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), m.range.first, m.range.last + 1)
        }
        // Strings
        Regex("\"[^\"]*\"|'[^']*'|`[^`]*`").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxString), m.range.first, m.range.last + 1)
        }
        // Numbers
        Regex("\\b\\d+(\\.\\d+)?\\b").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxNumber), m.range.first, m.range.last + 1)
        }
    }

    private fun highlightJson(text: String, builder: AnnotatedString.Builder) {
        // Keys
        Regex("\"[^\"]+\"(?=\\s*:)").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxAttr, fontWeight = FontWeight.Bold), m.range.first, m.range.last + 1)
        }
        // String values
        Regex(":\\s*\"[^\"]*\"").findAll(text).forEach { m ->
            val quoteStart = m.value.indexOf('"')
            if (quoteStart >= 0) {
                builder.addStyle(SpanStyle(color = SyntaxString), m.range.first + quoteStart, m.range.last + 1)
            }
        }
        // Numbers
        Regex("\\b\\d+(\\.\\d+)?\\b").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxNumber), m.range.first, m.range.last + 1)
        }
        // Booleans & null
        Regex("\\b(true|false|null)\\b").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), m.range.first, m.range.last + 1)
        }
    }

    private fun highlightGeneric(text: String, builder: AnnotatedString.Builder) {
        Regex("\"[^\"]*\"|'[^']*'").findAll(text).forEach { m ->
            builder.addStyle(SpanStyle(color = SyntaxString), m.range.first, m.range.last + 1)
        }
    }
}
