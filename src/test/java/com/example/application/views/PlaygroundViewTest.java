package com.example.application.views;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PlaygroundViewTest extends SpringBrowserlessTest {

    @Test
    void clickButton_showsGreeting() {
        navigate(PlaygroundView.class);

        var nameField = $(TextField.class).withPropertyValue(TextField::getPlaceholder, "Your name").single();
        var button = $(Button.class).withText("Say hello").single();

        test(nameField).setValue("Vaadin");
        test(button).click();

        var greeting = $(Paragraph.class).single();
        assertEquals("Hello Vaadin", test(greeting).getText());
    }

    @Test
    void clickWithEmptyName_showsEmptyGreeting() {
        navigate(PlaygroundView.class);

        var button = $(Button.class).withText("Say hello").single();
        test(button).click();

        var greeting = $(Paragraph.class).single();
        assertEquals("Hello ", test(greeting).getText());
    }
}
