package dk.itu.adventurequest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ConversationScreen(navController: NavController, locationName: String?) {
    val topics = remember {
        listOf(
            "What is a small victory you had this week?",
            "How do you usually manage your energy on days like today?",
            "What is your favorite memory of spending time outdoors?",
            "What is one thing that made you smile recently?",
            "If you could travel anywhere in the world tomorrow, where would you go?",
            "Look around; what is the most beautiful or interesting thing you see right now?",
            "What is a piece of advice that has really stayed with you over the years?",
            "What’s a small 'life hack' or a new way of doing something that you’ve found surprisingly helpful lately?",
            "When you think about the 'you' from ten years ago, what’s your favorite quality that has stayed exactly the same?",
            "What is one way your friends or family have supported you recently that made you feel really understood?",
            "What is one thing about your daily routine that brings you a strong sense of calm or control?"
        )
    }

    val currentTopic = remember { topics.random() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You reached $locationName!",
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Take a break and here's a little conversation starter for you: ",
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary), // High-contrast border
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = currentTopic,
                fontSize = 22.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(32.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Close the screen
        Button(
            onClick = { navController.popBackStack("map_screen", false)},
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Finish & Return to Map", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}