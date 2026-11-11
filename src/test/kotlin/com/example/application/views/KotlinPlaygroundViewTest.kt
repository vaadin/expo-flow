package com.example.application.views

import com.vaadin.browserless.SpringBrowserlessTest
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class KotlinPlaygroundViewTest : SpringBrowserlessTest() {

    @Test
    fun clickButton_showsGreeting() {
        navigate(KotlinPlaygroundView::class.java)

        val nameField = `$`(TextField::class.java).withPropertyValue(TextField::getPlaceholder, "Your name").single()
        val button = `$`(Button::class.java).withText("Say hello").single()

        test(nameField).setValue("Kotlin")
        test(button).click()

        val greeting = `$`(Paragraph::class.java).single()
        assertEquals("Hello Kotlin", test(greeting).getText())
    }
}
