package com.example.application.views.quiz;

import com.example.application.data.entity.TShirtOrder;
import com.example.application.service.TShirtService;
import com.example.application.views.ComponentsView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.context.annotation.Scope;


@SpringComponent
@Scope("prototype")
public class QuizSubmitForm extends Dialog {

    private final TShirtService tShirtService;
    // Form and actions
    TextField name = new TextField("Name");
    EmailField email = new EmailField("Email");
    ComboBox<String> shirtSize = new ComboBox("T-shirt size") {{
        setItems("S", "M", "L", "XL", "XXL");
        setPlaceholder("Select size");
    }};
    BeanValidationBinder<TShirtOrder> binder = new BeanValidationBinder<>(TShirtOrder.class);

    Button submit = new Button("Submit your answers and participate!", e -> submit());

    public QuizSubmitForm(TShirtService tShirtService) {
        this.tShirtService = tShirtService;
    }

    void submit() {
        binder.validate();
        tShirtService.placeOrder(binder.getBean());
        close();
        UI.getCurrent().navigate(ComponentsView.class);
        Notification.show("Thank you for participating! We can't help you more with Roman history more, but any questions regarding Vaadin?", 0, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showResults(int extraPoints) {
        setHeaderTitle("Congratulations! You have completed the quiz!");

        add(new Markdown("""
                You earned **%d points** out of **%d** possible points for the raffle. 
                You'll anyways get a T-shirt for participating and one point for the raffle.
                If you are not happy with your score, consider sneaking in the source code
                and trying again by reloading.
                
                Now fill in your name and email to submit your answers and participate in the raffle.
                
                **By submitting, you agree to Vaadin privacy policy. Yes we'll remind you to try
                out Vaadin during the next couple of weeks, but you can unsubscribe any time you want.**
                """.formatted(extraPoints, 4)));

        add(new FormLayout(name, email, shirtSize));

        binder.bindInstanceFields(this);
        binder.setBean(new TShirtOrder());

        add(submit);
        open();
    }
}
