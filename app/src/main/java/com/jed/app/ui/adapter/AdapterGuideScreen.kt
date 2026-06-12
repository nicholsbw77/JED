@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.jed.app.ui.adapter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jed.app.adapter.AdapterAdvisor
import com.jed.app.adapter.AdapterRecommendation
import com.jed.app.adapter.RecommendationTier
import com.jed.app.data.VehicleCatalog
import com.jed.app.ui.theme.BlueCool
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.RedAlert
import com.jed.app.ui.theme.YellowWarn

@Composable
fun AdapterGuideScreen(onClose: () -> Unit) {
    var make by remember { mutableStateOf("Ford") }
    var year by remember { mutableStateOf("2010") }
    var makeExpanded by remember { mutableStateOf(false) }

    val guide = remember(make, year) {
        AdapterAdvisor.recommendFor(make, year.toIntOrNull() ?: 2010)
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = OrangeAccent, unfocusedBorderColor = MediumGray,
        focusedTextColor = OffWhite, unfocusedTextColor = OffWhite,
        cursorColor = OrangeAccent, focusedLabelColor = OrangeAccent, unfocusedLabelColor = LightGray
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Charcoal)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OffWhite)
            }
            Text("ADAPTER GUIDE", color = OrangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        Text(
            "Tell us your vehicle and we'll suggest the best adapter to buy.",
            color = LightGray, fontSize = 13.sp
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(
                expanded = makeExpanded,
                onExpandedChange = { makeExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = make, onValueChange = {}, readOnly = true,
                    label = { Text("Make") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = makeExpanded) },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = makeExpanded, onDismissRequest = { makeExpanded = false }) {
                    VehicleCatalog.MAKES.forEach { m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = { make = m; makeExpanded = false })
                    }
                }
            }
            OutlinedTextField(
                value = year, onValueChange = { year = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Year") }, colors = textFieldColors,
                modifier = Modifier.width(110.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(guide.headline, color = OffWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        guide.notes.forEach { note ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text("•", color = OrangeAccent, modifier = Modifier.width(16.dp))
                Text(note, color = LightGray, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("RECOMMENDATIONS", color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        guide.recommendations.forEach { rec ->
            RecommendationCard(rec)
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RecommendationCard(rec: AdapterRecommendation) {
    val tierColor = when (rec.tier) {
        RecommendationTier.BEST -> GreenGood
        RecommendationTier.GOOD -> BlueCool
        RecommendationTier.BUDGET -> YellowWarn
        RecommendationTier.AVOID -> RedAlert
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(10.dp))
            .border(1.dp, tierColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(rec.product, color = OffWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(rec.tier.label.uppercase(), color = tierColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            rec.links.joinToString("  ·  ") { it.displayName } + "    " + rec.approxPrice,
            color = LightGray, fontSize = 11.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(rec.why, color = OffWhite.copy(alpha = 0.85f), fontSize = 13.sp)
    }
}
