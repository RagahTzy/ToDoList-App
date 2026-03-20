package com.example.todolist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun MarkdownGuideDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()

    ImmersiveDialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("CLOSE", color = NeonCyan) }
            },
            title = {
                Text("MARKDOWN GUIDE", color = NeonCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            },
            text = {
                val scrollState = rememberScrollState()
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        // HEADINGS
                        GuideSection(title = "HEADINGS") {
                            GuideRow(syntax = "# Heading 1", result = "Largest heading")
                            GuideRow(syntax = "## Heading 2", result = "Large heading")
                            GuideRow(syntax = "### Heading 3", result = "Medium heading")
                            GuideRow(syntax = "#### Heading 4", result = "Small heading")
                            GuideRow(syntax = "##### Heading 5", result = "Smaller heading")
                            GuideRow(syntax = "###### Heading 6", result = "Smallest heading")
                            GuideNote("Put a space between # and the heading text.")
                        }

                        GuideDivider()

                        // TEXT FORMATTING
                        GuideSection(title = "TEXT FORMATTING") {
                            GuideRow(syntax = "**bold text**", result = "bold text")
                            GuideRow(syntax = "*italic text*", result = "italic text")
                            GuideRow(syntax = "_italic text_", result = "italic text (alternative)")
                            GuideRow(syntax = "***bold italic***", result = "bold italic text")
                            GuideRow(syntax = "~~strikethrough~~", result = "strikethrough text")
                            GuideRow(syntax = "`inline code`", result = "inline code")
                            GuideNote("Wrap text with the same symbol on both sides.")
                        }

                        GuideDivider()

                        // LISTS
                        GuideSection(title = "LISTS") {
                            GuideCodeBlock(
                                """Unordered list:
- Item one
- Item two
- Item three

Alternative (also works):
* Item one
* Item two"""
                            )
                            GuideCodeBlock(
                                """Ordered list:
1. First item
2. Second item
3. Third item"""
                            )
                            GuideCodeBlock(
                                """Nested list:
- Parent item
  - Child item
  - Child item
    - Grandchild item"""
                            )
                            GuideNote("Use 2 spaces for each level of indentation in nested lists.")
                        }

                        GuideDivider()

                        // TASK LIST
                        GuideSection(title = "TASK LIST") {
                            GuideCodeBlock(
                                """- [x] Completed task
- [ ] Incomplete task
- [x] Another completed task"""
                            )
                            GuideNote("Use [x] for checked and [ ] for unchecked items.")
                        }

                        GuideDivider()

                        // LINKS
                        GuideSection(title = "LINKS") {
                            GuideRow(syntax = "[Link text](https://url.com)", result = "Clickable link")
                            GuideRow(syntax = "[Google](https://google.com)", result = "Link with label")
                            GuideCodeBlock(
                                """Reference-style link:
[link text][reference]

[reference]: https://url.com"""
                            )
                            GuideNote("Always include https:// for external links.")
                        }

                        GuideDivider()

                        // IMAGES
                        GuideSection(title = "IMAGES") {
                            GuideRow(syntax = "![alt text](https://url.com/img.jpg)", result = "Display image from URL")
                            GuideCodeBlock(
                                """Example:
![My Screenshot](https://i.imgur.com/example.png)

![Logo](https://upload.wikimedia.org/example.svg)"""
                            )
                            GuideNote("Images are loaded from the internet. Make sure the URL points directly to an image file (jpg, png, gif, svg, webp).")
                        }

                        GuideDivider()

                        // BLOCKQUOTE
                        GuideSection(title = "BLOCKQUOTE") {
                            GuideCodeBlock(
                                """> This is a blockquote.
> It can span multiple lines.

> Nested blockquote:
>> This is nested inside."""
                            )
                            GuideNote("Start a line with > to create a blockquote.")
                        }

                        GuideDivider()

                        // CODE BLOCKS
                        GuideSection(title = "CODE BLOCKS") {
                            GuideCodeBlock("```\nPlain code block\nNo syntax highlighting\n```")
                            GuideCodeBlock("```kotlin\nfun hello() {\n    println(\"Hello World\")\n}\n```")
                            GuideCodeBlock("```python\ndef hello():\n    print(\"Hello World\")\n```")
                            GuideCodeBlock("```javascript\nconsole.log(\"Hello World\");\n```")
                            GuideNote("Specify the language after the opening ``` for syntax highlighting (kotlin, python, javascript, java, xml, json, etc).")
                        }

                        GuideDivider()

                        // HORIZONTAL RULE
                        GuideSection(title = "HORIZONTAL RULE / DIVIDER") {
                            GuideRow(syntax = "---", result = "Horizontal line")
                            GuideRow(syntax = "***", result = "Horizontal line (alternative)")
                            GuideRow(syntax = "___", result = "Horizontal line (alternative)")
                            GuideNote("The line must be on its own line with a blank line above and below for best results.")
                        }

                        GuideDivider()

                        // TABLES
                        GuideSection(title = "TABLES") {
                            GuideCodeBlock(
                                """| Column 1 | Column 2 | Column 3 |
|----------|----------|----------|
| Cell 1   | Cell 2   | Cell 3   |
| Cell 4   | Cell 5   | Cell 6   |"""
                            )
                            GuideCodeBlock(
                                """Column alignment:
| Left     | Center   | Right    |
|:---------|:--------:|---------:|
| text     | text     | text     |"""
                            )
                            GuideNote("Use : in the separator row to control alignment. :--- = left, :---: = center, ---: = right.")
                        }

                        GuideDivider()

                        // LINE BREAKS & PARAGRAPHS
                        GuideSection(title = "LINE BREAKS & PARAGRAPHS") {
                            GuideCodeBlock(
                                """Paragraph 1 text here.

Paragraph 2 starts after a blank line.

Line break within paragraph:
First line (add 2 spaces at end)  
Second line continues here."""
                            )
                            GuideNote("A blank line creates a new paragraph. Two spaces at the end of a line create a line break within the same paragraph.")
                        }

                        GuideDivider()

                        // ESCAPING
                        GuideSection(title = "ESCAPING SPECIAL CHARACTERS") {
                            GuideCodeBlock(
                                """Use backslash \\ to escape:
\*not italic\*
\**not bold\**
\# not a heading
\- not a list item
\[not a link\]"""
                            )
                            GuideNote("Escapable characters: \\ ` * _ { } [ ] ( ) # + - . !")
                        }

                        GuideDivider()

                        // FULL EXAMPLE
                        GuideSection(title = "FULL EXAMPLE") {
                            GuideCodeBlock(
                                """# Project Notes

## Overview
This note covers the **main objectives** and *key points* for the project.

---

## Tasks
- [x] Research the topic
- [x] Create an outline
- [ ] Write the first draft
- [ ] Review and edit

## Code Reference
\`\`\`kotlin
fun calculateProgress(done: Int, total: Int): Float {
    return done.toFloat() / total.toFloat()
}
\`\`\`

## Resources
- [Official Docs](https://developer.android.com)
- [Stack Overflow](https://stackoverflow.com)

## Screenshot
![App Preview](https://i.imgur.com/example.png)

---
> **Note:** Always commit your changes before submitting."""
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    ScrollArrowsOverlay(
                        canScrollBackward = scrollState.canScrollBackward,
                        canScrollForward = scrollState.canScrollForward,
                        onUpClick = { scope.launch { scrollState.animateScrollTo(0) } },
                        onDownClick = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } }
                    )
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxHeight(0.9f)
        )
    }
}

@Composable
private fun GuideSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
        content()
    }
}

@Composable
private fun GuideRow(syntax: String, result: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = syntax,
            color = NeonMagenta,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = result,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GuideCodeBlock(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f))
    ) {
        Text(
            text = code,
            color = NeonMagenta.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(12.dp),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun GuideNote(note: String) {
    Text(
        text = "💡 $note",
        color = NeonYellow.copy(alpha = 0.7f),
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
}

@Composable
private fun GuideDivider() {
    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
}