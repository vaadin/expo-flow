// React adapter element for MUI Switch with value change support
// Java counterpart: SwitchComponent.java
import { Switch } from '@mui/material';
import React, { ReactElement } from 'react';
import {ReactAdapterElement, RenderHooks} from "../generated/flow/ReactAdapter";

class ReactSwitchElement extends ReactAdapterElement {
  protected render(hooks: RenderHooks): ReactElement | null {
    // Use local state for the switch
    const [checked, setChecked] = hooks.useState<Boolean>("value");

    return (
      <Switch value={checked} onChange={(e) => setChecked(e.target.checked)} />
    );
  }
}

customElements.define('react-switch', ReactSwitchElement);
