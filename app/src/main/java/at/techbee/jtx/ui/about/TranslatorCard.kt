/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.Typography


@Composable
fun TranslatorCard(
    name: String,
    modifier: Modifier = Modifier
) {

    ElevatedCard(modifier = modifier) {

        Row(
            modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(end = 8.dp)) {
                Text(
                    stringResource(id = R.string.about_translations_thanks_to),
                    style = Typography.bodyMedium, 
                    modifier = Modifier.alpha(0.7f)
                )
                Text(
                    name,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(Icons.Outlined.Translate, null, modifier = Modifier
                .size(50.dp)
                .alpha(0.1f)
                .padding(end = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TranslatorCard_Preview() {
    MaterialTheme {
        TranslatorCard("Patrick")
    }
}
