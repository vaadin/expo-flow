import React, { useState } from 'react';
import { TextField } from '@vaadin/react-components/TextField';
import { Button } from '@vaadin/react-components/Button';
import { Notification } from '@vaadin/react-components/Notification';
import { HorizontalLayout } from '@vaadin/react-components/HorizontalLayout';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';


export const config: ViewConfig = {
  title: 'Fsdfs',
  
};

export default function Fsdfs() {
  const [name, setName] = useState('');

  return (
    <>
      <HorizontalLayout className="items-baseline gap-m">
        <TextField
          label="Your name"
          helperText="Write your name here"
          onValueChanged={(e) => {
            setName(e.detail.value);
          }}
        />
        <Button
          onClick={() => {
            Notification.show(`Hello copilot`);
          }}>
          Say hello
        </Button>
      </HorizontalLayout>
    </>
  );
}
