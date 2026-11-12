package com.example.petcare.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.R

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onCreateAccountClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF9F9FD)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                HeaderLogo()
                Spacer(modifier = Modifier.height(16.dp))
                CategoryPillBadge()
                Spacer(modifier = Modifier.height(24.dp))
                HeroIllustrationCard()
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.welcome_headline),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F0E47),
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.welcome_subtitle),
                    fontSize = 14.sp,
                    color = Color(0xFF585992),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onCreateAccountClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F0E47),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.create_account),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFDCDCE8)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0F0E47),
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.login_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.version_prototype),
                    fontSize = 12.sp,
                    color = Color(0xFF8686AC),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .width(134.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB0B0C8))
                )
            }
        }
    }
}

@Composable
fun HeaderLogo(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_petcare),
            contentDescription = stringResource(R.string.app_name),
            tint = Color.Unspecified,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F0E47)
                )
                Text(
                    text = "Care",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE59A72)
                )
            }
            Text(
                text = stringResource(R.string.animal_welfare),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8686AC),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun CategoryPillBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(2.dp, CircleShape),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F8))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE59A72))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.health_preventive_care),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF585992),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun HeroIllustrationCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x15000000), spotColor = Color(0x250F0E47)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, Color(0xFFE4E4F5))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawRect(color = Color(0xFFFAF7F2))

                drawCircle(
                    color = Color(0xFFFFD1C1),
                    radius = 36.dp.toPx(),
                    center = Offset(w * 0.32f, h * 0.28f)
                )

                drawArc(
                    color = Color(0xFFE1E0F5),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.22f, h * 0.22f),
                    size = Size(w * 0.25f, h * 0.35f)
                )

                drawArc(
                    color = Color(0xFFF7D1C9),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.58f, h * 0.30f),
                    size = Size(w * 0.16f, h * 0.25f)
                )

                val linePath = Path().apply {
                    moveTo(w * 0.25f, h * 0.30f)
                    cubicTo(w * 0.35f, h * 0.12f, w * 0.45f, h * 0.32f, w * 0.52f, h * 0.18f)
                }
                drawPath(
                    path = linePath,
                    color = Color(0xFF585992),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                drawCircle(
                    color = Color(0xFFE59A72),
                    radius = 12.dp.toPx(),
                    center = Offset(w * 0.74f, h * 0.22f)
                )

                drawRoundRect(
                    color = Color(0xFFF3F3FA),
                    topLeft = Offset(w * 0.78f, h * 0.25f),
                    size = Size(w * 0.22f, h * 0.55f),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF0F0E47),
                    topLeft = Offset(w * 0.83f, h * 0.32f),
                    size = Size(w * 0.12f, h * 0.08f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                drawRoundRect(
                    color = Color(0xFF585992),
                    topLeft = Offset(w * 0.08f, h * 0.26f),
                    size = Size(w * 0.10f, h * 0.35f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF8686AC),
                    radius = 18.dp.toPx(),
                    center = Offset(w * 0.13f, h * 0.22f)
                )

                val benchTopY = h * 0.52f
                val benchWidth = w * 0.62f
                val benchLeft = w * 0.18f
                val benchHeight = 22.dp.toPx()

                drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(benchLeft + 16.dp.toPx(), benchTopY + benchHeight),
                    size = Size(10.dp.toPx(), h * 0.25f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(benchLeft + benchWidth - 26.dp.toPx(), benchTopY + benchHeight),
                    size = Size(10.dp.toPx(), h * 0.25f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                drawRoundRect(
                    color = Color(0xFFE2B07E),
                    topLeft = Offset(benchLeft, benchTopY),
                    size = Size(benchWidth, benchHeight),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFFC79261),
                    topLeft = Offset(benchLeft, benchTopY + benchHeight - 6.dp.toPx()),
                    size = Size(benchWidth, 6.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                val dogCenterX = benchLeft + benchWidth * 0.40f
                val dogCenterY = benchTopY - 26.dp.toPx()

                drawOval(
                    color = Color(0xFFF7C873),
                    topLeft = Offset(dogCenterX - 34.dp.toPx(), dogCenterY - 18.dp.toPx()),
                    size = Size(68.dp.toPx(), 62.dp.toPx())
                )

                drawCircle(
                    color = Color(0xFFF7C873),
                    radius = 32.dp.toPx(),
                    center = Offset(dogCenterX, dogCenterY - 30.dp.toPx())
                )

                drawOval(
                    color = Color(0xFFD99B38),
                    topLeft = Offset(dogCenterX - 42.dp.toPx(), dogCenterY - 45.dp.toPx()),
                    size = Size(20.dp.toPx(), 38.dp.toPx())
                )
                drawOval(
                    color = Color(0xFFD99B38),
                    topLeft = Offset(dogCenterX + 22.dp.toPx(), dogCenterY - 45.dp.toPx()),
                    size = Size(20.dp.toPx(), 38.dp.toPx())
                )

                drawOval(
                    color = Color(0xFFFFF0D0),
                    topLeft = Offset(dogCenterX - 18.dp.toPx(), dogCenterY - 34.dp.toPx()),
                    size = Size(36.dp.toPx(), 28.dp.toPx())
                )

                drawOval(
                    color = Color(0xFF4A2E16),
                    topLeft = Offset(dogCenterX - 9.dp.toPx(), dogCenterY - 36.dp.toPx()),
                    size = Size(18.dp.toPx(), 12.dp.toPx())
                )

                val eyeLeft = Path().apply {
                    moveTo(dogCenterX - 16.dp.toPx(), dogCenterY - 38.dp.toPx())
                    quadraticTo(
                        dogCenterX - 12.dp.toPx(), dogCenterY - 44.dp.toPx(),
                        dogCenterX - 8.dp.toPx(), dogCenterY - 38.dp.toPx()
                    )
                }
                val eyeRight = Path().apply {
                    moveTo(dogCenterX + 8.dp.toPx(), dogCenterY - 38.dp.toPx())
                    quadraticTo(
                        dogCenterX + 12.dp.toPx(), dogCenterY - 44.dp.toPx(),
                        dogCenterX + 16.dp.toPx(), dogCenterY - 38.dp.toPx()
                    )
                }
                drawPath(eyeLeft, color = Color(0xFF332211), style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round))
                drawPath(eyeRight, color = Color(0xFF332211), style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round))

                drawOval(
                    color = Color(0xFFFF7A8A),
                    topLeft = Offset(dogCenterX - 6.dp.toPx(), dogCenterY - 24.dp.toPx()),
                    size = Size(12.dp.toPx(), 14.dp.toPx())
                )

                val catCenterX = benchLeft + benchWidth * 0.72f
                val catCenterY = benchTopY - 20.dp.toPx()

                drawOval(
                    color = Color(0xFFEDA597),
                    topLeft = Offset(catCenterX - 24.dp.toPx(), catCenterY - 14.dp.toPx()),
                    size = Size(48.dp.toPx(), 44.dp.toPx())
                )

                drawCircle(
                    color = Color(0xFFEDA597),
                    radius = 24.dp.toPx(),
                    center = Offset(catCenterX, catCenterY - 24.dp.toPx())
                )

                val catEarLeft = Path().apply {
                    moveTo(catCenterX - 22.dp.toPx(), catCenterY - 28.dp.toPx())
                    lineTo(catCenterX - 14.dp.toPx(), catCenterY - 46.dp.toPx())
                    lineTo(catCenterX - 4.dp.toPx(), catCenterY - 32.dp.toPx())
                    close()
                }
                val catEarRight = Path().apply {
                    moveTo(catCenterX + 4.dp.toPx(), catCenterY - 32.dp.toPx())
                    lineTo(catCenterX + 14.dp.toPx(), catCenterY - 46.dp.toPx())
                    lineTo(catCenterX + 22.dp.toPx(), catCenterY - 28.dp.toPx())
                    close()
                }
                drawPath(catEarLeft, color = Color(0xFFEDA597))
                drawPath(catEarRight, color = Color(0xFFEDA597))

                drawCircle(
                    color = Color(0xFF332211),
                    radius = 2.5f.dp.toPx(),
                    center = Offset(catCenterX - 8.dp.toPx(), catCenterY - 24.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF332211),
                    radius = 2.5f.dp.toPx(),
                    center = Offset(catCenterX + 8.dp.toPx(), catCenterY - 24.dp.toPx())
                )

                drawCircle(
                    color = Color(0xFFD67265),
                    radius = 3.dp.toPx(),
                    center = Offset(catCenterX, catCenterY - 18.dp.toPx())
                )

                drawRoundRect(
                    color = Color(0xFFD3D3E8),
                    topLeft = Offset(0f, h - 8.dp.toPx()),
                    size = Size(w, 8.dp.toPx())
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp, start = 12.dp)
                .shadow(6.dp, CircleShape),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFF0F0F8))
        ) {
            Row(
                modifier = Modifier.padding(end = 16.dp, top = 6.dp, bottom = 6.dp, start = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F0E47)),
                    contentAlignment = Alignment.Center
                ) {
                    SyringeIcon(
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.vaccines_up_to_date),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F0E47)
                    )
                    Text(
                        text = stringResource(R.string.preventive_control),
                        fontSize = 10.sp,
                        color = Color(0xFF8686AC)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp, end = 12.dp)
                .shadow(6.dp, CircleShape),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFF0F0F8))
        ) {
            Row(
                modifier = Modifier.padding(end = 16.dp, top = 6.dp, bottom = 6.dp, start = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF0EC)),
                    contentAlignment = Alignment.Center
                ) {
                    HeartIcon(
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFE59A72)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.optimal_health),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F0E47)
                    )
                    Text(
                        text = stringResource(R.string.synced_history),
                        fontSize = 10.sp,
                        color = Color(0xFF8686AC)
                    )
                }
            }
        }
    }
}

@Composable
fun SyringeIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.65f)
            lineTo(w * 0.65f, h * 0.35f)

            moveTo(w * 0.48f, h * 0.78f)
            lineTo(w * 0.78f, h * 0.48f)

            moveTo(w * 0.25f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.85f)

            moveTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.85f, h * 0.15f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun HeartIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFE59A72)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.8f)
            cubicTo(
                w * 0.1f, h * 0.5f,
                w * 0.1f, h * 0.2f,
                w * 0.32f, h * 0.2f
            )
            cubicTo(
                w * 0.44f, h * 0.2f,
                w * 0.5f, h * 0.32f,
                w * 0.5f, h * 0.32f
            )
            cubicTo(
                w * 0.5f, h * 0.32f,
                w * 0.56f, h * 0.2f,
                w * 0.68f, h * 0.2f
            )
            cubicTo(
                w * 0.9f, h * 0.2f,
                w * 0.9f, h * 0.5f,
                w * 0.5f, h * 0.8f
            )
            close()
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen()
}
