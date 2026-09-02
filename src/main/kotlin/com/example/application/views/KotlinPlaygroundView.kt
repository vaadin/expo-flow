package com.example.application.views

import com.example.application.data.repository.PersonRepository
import com.example.application.data.repository.TShirtOrderRepository
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.vaadin.lineawesome.LineAwesomeIconUrl

@PageTitle("Kotlin Playground")
@Route("kotlin-playground")
@Menu(title = "Kotlin Playground", icon = LineAwesomeIconUrl.FILE_CODE_SOLID, order = 8.0)
class KotlinPlaygroundView(repository: PersonRepository) : VerticalLayout() {

    init {
        val name = TextField().apply {
            placeholder = "Your name"
        }

        val button = Button("Say hello").apply {
            addClickShortcut(Key.ENTER)
        }

        button.addClickListener {
            add(Paragraph("Hello ${name.value}"))
            name.clear()
        }

        add(HorizontalLayout(name, button))
    }
}
