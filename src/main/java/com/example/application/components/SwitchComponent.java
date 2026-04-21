package com.example.application.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.function.SerializableConsumer;

// React adapter wrapper for MUI Switch with value change support
@NpmPackage(value = "@mui/material", version = "^7.1.0")
@NpmPackage(value = "@emotion/react", version = "^11.14.0")
@NpmPackage(value = "@emotion/styled", version = "^11.14.0")
@JsModule("./components/react-switch.tsx")
@Tag("react-switch")
public class SwitchComponent extends ReactAdapterComponent {

    public Boolean getValue() {
        return getState("value", Boolean.class);
    }

    public void setValue(Boolean value) {
        setState("value", value);
    }

    public void addValueChangeListener(SerializableConsumer<Boolean> listener) {
        addStateChangeListener("value", Boolean.class, listener);
    }
}
