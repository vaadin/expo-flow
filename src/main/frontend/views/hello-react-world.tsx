import {ViewConfig} from '@vaadin/hilla-file-router/types.js';
import {useSignal} from '@vaadin/hilla-react-signals';
import {Button, HorizontalLayout, TextField, VerticalLayout} from '@vaadin/react-components';
import {HelloReactWorldService} from '../generated/endpoints';

export const config: ViewConfig = {
    menu: {order: 7, icon: 'line-awesome/svg/globe-solid.svg'},
    title: 'React Playground',
};

export default function HelloReactWorldView() {
    const name = useSignal('');
    const greetings = useSignal<string[]>([]);

    const sayHello = async () => {
        const serverResponse = await HelloReactWorldService.sayCiao(name.value);
        greetings.value = [...greetings.value, serverResponse];
        name.value = '';
    };

    return (
        <VerticalLayout theme="padding spacing">
            <HorizontalLayout theme="spacing">
                <TextField
                    placeholder="Your name"
                    value={name.value}
                    onValueChanged={(e) => {
                        name.value = e.detail.value;
                    }}
                    onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                            void sayHello();
                        }
                    }}
                />
                <Button onClick={sayHello}>Say hello</Button>
            </HorizontalLayout>
            {greetings.value.map((greeting, index) => (
                <p key={index}>{greeting}</p>
            ))}
        </VerticalLayout>
    );
}
