package com.zero.id.app.ui.screens.result

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import com.zero.id.app.model.UserProfile
import org.junit.Rule
import org.junit.Test

class ResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun resultScreen_Success_Over18_And_SalaryOver15k() {
        composeTestRule.setContent {
            ResultScreen(
                isSuccess = true,
                message = "Success",
                userProfile = UserProfile(birthYear = 2000, salary = 20000),
                onNavigateHome = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("Success!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Is Over 18").assertIsDisplayed()
        composeTestRule.onNodeWithText("Is Over 18").onParent().onChildren().filter(hasText("true")).assertCountEquals(1)

        composeTestRule.onNodeWithText("Is Salary Over 15k").assertIsDisplayed()
        composeTestRule.onNodeWithText("Is Salary Over 15k").onParent().onChildren().filter(hasText("true")).assertCountEquals(1)
    }

    @Test
    fun resultScreen_Success_Under18_And_SalaryUnder15k() {
        composeTestRule.setContent {
            ResultScreen(
                isSuccess = true,
                message = "Success",
                userProfile = UserProfile(birthYear = 2010, salary = 10000),
                onNavigateHome = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("Success!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Is Over 18").assertIsDisplayed()
        composeTestRule.onNodeWithText("Is Over 18").onParent().onChildren().filter(hasText("false")).assertCountEquals(1)
        composeTestRule.onNodeWithText("Is Salary Over 15k").assertIsDisplayed()
        composeTestRule.onNodeWithText("Is Salary Over 15k").onParent().onChildren().filter(hasText("false")).assertCountEquals(1)
    }

    @Test
    fun resultScreen_Failure() {
        composeTestRule.setContent {
            ResultScreen(
                isSuccess = false,
                message = "Verification failed",
                userProfile = null,
                onNavigateHome = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("Failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verification failed").assertIsDisplayed()
    }
}
