package com.appventurez.bmwms.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appventurez.bmwms.R

@Composable
fun LoginTypeScreen(
    onCbwtfClick: () -> Unit,
    onHcfClick: () -> Unit,
    onGuidelineClick: () -> Unit,
    onYoutubeClick: () -> Unit
) {
    val appColorDark = Color(0xFFFA8717)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.uplogogpb),
                contentDescription = "Header Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Bio Medical Waste Management System",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp)
            )

            Text(
                text = "Choose account type from CBWTF and HCF",
                color = appColorDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onCbwtfClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColorDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "CBWTF Operator Login",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onHcfClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColorDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "HCF Login",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Guidelines (Hidden in XML, so keeping it commented or hidden here)
            /*
            Text(
                text = stringResource(id = R.string.u_guidelines_of_cpcb_and_bmwm_rules_u),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clickable { onGuidelineClick() }
            )
            */

            /*Row(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 20.dp)
                    .clickable { onYoutubeClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_smart_display_24),
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(id = R.string.how_it_works),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }*/
        }
    }
}
