package com.fazli.vispar.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object CustomIcons {
    val Movie: ImageVector
        get() {
            if (_movie != null) {
                return _movie!!
            }
            _movie = ImageVector.Builder(
                name = "Movie",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 64f,
                viewportHeight = 64f
            ).apply {
                // Background gradient effect
                path(
                    fill = SolidColor(Color(0xFFB69DF8)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(4f, 20f)
                    curveTo(4f, 12.26f, 10.26f, 6f, 18f, 6f)
                    horizontalLineTo(46f)
                    curveTo(53.74f, 6f, 60f, 12.26f, 60f, 20f)
                    verticalLineTo(44f)
                    curveTo(60f, 51.74f, 53.74f, 58f, 46f, 58f)
                    horizontalLineTo(18f)
                    curveTo(10.26f, 58f, 4f, 51.74f, 4f, 44f)
                    verticalLineTo(20f)
                    close()
                }
                // Film frame
                path(
                    fill = SolidColor(Color(0xFFEADDFF)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(14f, 16f)
                    horizontalLineTo(50f)
                    curveTo(52.21f, 16f, 54f, 17.79f, 54f, 20f)
                    verticalLineTo(36f)
                    curveTo(54f, 38.21f, 52.21f, 40f, 50f, 40f)
                    horizontalLineTo(14f)
                    curveTo(11.79f, 40f, 10f, 38.21f, 10f, 36f)
                    verticalLineTo(20f)
                    curveTo(10f, 17.79f, 11.79f, 16f, 14f, 16f)
                    close()
                }
                // Play button
                path(
                    fill = SolidColor(Color(0xFF4F378B)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(28f, 24f)
                    lineTo(42f, 32f)
                    lineTo(28f, 40f)
                    close()
                }
            }.build()
            return _movie!!
        }

    val Series: ImageVector
        get() {
            if (_series != null) {
                return _series!!
            }
            _series = ImageVector.Builder(
                name = "Series",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 64f,
                viewportHeight = 64f
            ).apply {
                // Background
                path(
                    fill = SolidColor(Color(0xFFB69DF8)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(4f, 20f)
                    curveTo(4f, 12.26f, 10.26f, 6f, 18f, 6f)
                    horizontalLineTo(46f)
                    curveTo(53.74f, 6f, 60f, 12.26f, 60f, 20f)
                    verticalLineTo(44f)
                    curveTo(60f, 51.74f, 53.74f, 58f, 46f, 58f)
                    horizontalLineTo(18f)
                    curveTo(10.26f, 58f, 4f, 51.74f, 4f, 44f)
                    verticalLineTo(20f)
                    close()
                }
                // TV body
                path(
                    fill = SolidColor(Color(0xFFEADDFF)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(14f, 16f)
                    horizontalLineTo(50f)
                    curveTo(52.21f, 16f, 54f, 17.79f, 54f, 20f)
                    verticalLineTo(36f)
                    curveTo(54f, 38.21f, 52.21f, 40f, 50f, 40f)
                    horizontalLineTo(14f)
                    curveTo(11.79f, 40f, 10f, 38.21f, 10f, 36f)
                    verticalLineTo(20f)
                    curveTo(10f, 17.79f, 11.79f, 16f, 14f, 16f)
                    close()
                }
                // Antennas
                path(
                    fill = SolidColor(Color(0xFF4F378B)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(22f, 12f)
                    lineTo(16f, 6f)
                    moveTo(42f, 12f)
                    lineTo(48f, 6f)
                    moveTo(26f, 48f)
                    curveTo(26f, 48f, 32f, 48f, 38f, 48f)
                }
            }.build()
            return _series!!
        }

    val Search: ImageVector
        get() {
            if (_search != null) {
                return _search!!
            }
            _search = ImageVector.Builder(
                name = "Search",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 64f,
                viewportHeight = 64f
            ).apply {
                // Background
                path(
                    fill = SolidColor(Color(0xFFB69DF8)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(4f, 20f)
                    curveTo(4f, 12.26f, 10.26f, 6f, 18f, 6f)
                    horizontalLineTo(46f)
                    curveTo(53.74f, 6f, 60f, 12.26f, 60f, 20f)
                    verticalLineTo(44f)
                    curveTo(60f, 51.74f, 53.74f, 58f, 46f, 58f)
                    horizontalLineTo(18f)
                    curveTo(10.26f, 58f, 4f, 51.74f, 4f, 44f)
                    verticalLineTo(20f)
                    close()
                }
                // Magnifying glass
                path(
                    fill = SolidColor(Color(0xFFEADDFF)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(28f, 28f)
                    curveTo(28f, 33.52f, 33.48f, 39f, 39f, 39f)
                    curveTo(44.52f, 39f, 50f, 33.52f, 50f, 28f)
                    curveTo(50f, 22.48f, 44.52f, 17f, 39f, 17f)
                    curveTo(33.48f, 17f, 28f, 22.48f, 28f, 28f)
                    close()
                }
                // Handle
                path(
                    fill = SolidColor(Color(0xFF4F378B)),
                    stroke = SolidColor(Color(0xFF4F378B)),
                    strokeLineWidth = 3f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(46f, 46f)
                    lineTo(54f, 54f)
                }
            }.build()
            return _search!!
        }

    val Settings: ImageVector
        get() {
            if (_settings != null) {
                return _settings!!
            }
            _settings = ImageVector.Builder(
                name = "Settings",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 64f,
                viewportHeight = 64f
            ).apply {
                // Background
                path(
                    fill = SolidColor(Color(0xFFB69DF8)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(4f, 20f)
                    curveTo(4f, 12.26f, 10.26f, 6f, 18f, 6f)
                    horizontalLineTo(46f)
                    curveTo(53.74f, 6f, 60f, 12.26f, 60f, 20f)
                    verticalLineTo(44f)
                    curveTo(60f, 51.74f, 53.74f, 58f, 46f, 58f)
                    horizontalLineTo(18f)
                    curveTo(10.26f, 58f, 4f, 51.74f, 4f, 44f)
                    verticalLineTo(20f)
                    close()
                }
                // Gear outer circle
                path(
                    fill = SolidColor(Color(0xFFEADDFF)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(32f, 20f)
                    curveTo(25.37f, 20f, 20f, 25.37f, 20f, 32f)
                    curveTo(20f, 38.63f, 25.37f, 44f, 32f, 44f)
                    curveTo(38.63f, 44f, 44f, 38.63f, 44f, 32f)
                    curveTo(44f, 25.37f, 38.63f, 20f, 32f, 20f)
                    close()
                }
                // Gear inner circle
                path(
                    fill = SolidColor(Color(0xFF4F378B)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(32f, 26f)
                    curveTo(34.21f, 26f, 36f, 27.79f, 36f, 30f)
                    curveTo(36f, 32.21f, 34.21f, 34f, 32f, 34f)
                    curveTo(29.79f, 34f, 28f, 32.21f, 28f, 30f)
                    curveTo(28f, 27.79f, 29.79f, 26f, 32f, 26f)
                    close()
                }
                // Gear teeth
                path(
                    fill = SolidColor(Color(0xFF4F378B)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(32f, 14f)
                    lineTo(32f, 18f)
                    moveTo(32f, 46f)
                    lineTo(32f, 50f)
                    moveTo(14f, 32f)
                    lineTo(18f, 32f)
                    moveTo(46f, 32f)
                    lineTo(50f, 32f)
                    moveTo(19.5f, 19.5f)
                    lineTo(22.5f, 22.5f)
                    moveTo(41.5f, 41.5f)
                    lineTo(44.5f, 44.5f)
                    moveTo(19.5f, 44.5f)
                    lineTo(22.5f, 41.5f)
                    moveTo(41.5f, 22.5f)
                    lineTo(44.5f, 19.5f)
                }
            }.build()
            return _settings!!
        }

    private var _movie: ImageVector? = null
    private var _series: ImageVector? = null
    private var _search: ImageVector? = null
    private var _settings: ImageVector? = null
}