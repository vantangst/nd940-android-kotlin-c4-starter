package com.udacity.project4.util

import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_TOAST
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

fun onToast(
    text: Int,
    @androidx.annotation.IntRange(from = 1) maximumRetries: Int = 3
): ViewInteraction = onView(withText(text)).inRoot(ToastMatcher(maximumRetries))

class ToastMatcher(private val maximumRetries: Int) : TypeSafeMatcher<Root>() {

    private var currentFailures: Int = 0

    override fun describeTo(description: Description?) {
        description?.appendText("no toast found after")
    }

    override fun matchesSafely(item: Root?): Boolean {
        val type: Int? = item?.windowLayoutParams?.get()?.type

        if (TYPE_TOAST == type || TYPE_APPLICATION_OVERLAY == type) {
            val windowToken = item.decorView.windowToken
            val appToken = item.decorView.applicationWindowToken

            if (windowToken == appToken) {
                return true
            }
        }

        return ++currentFailures >= maximumRetries
    }
}