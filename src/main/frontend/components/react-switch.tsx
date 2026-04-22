// React adapter element for MUI Switch with value change support
// Java counterpart: SwitchComponent.java
import { Switch } from '@mui/material';
import React, { ReactElement } from 'react';
import {ReactAdapterElement, RenderHooks} from "../generated/flow/ReactAdapter";
import './react-switch.css';

class ReactSwitchElement extends ReactAdapterElement {
  protected render(hooks: RenderHooks): ReactElement | null {
    // Use local state for the switch
    const [checked, setChecked] = hooks.useState<boolean>("value");

    return (
      <Switch
        checked={checked}
        onChange={(e) => setChecked(e.target.checked)}
        className="react-switch"
      />
    );
  }
}

customElements.define('react-switch', ReactSwitchElement);
