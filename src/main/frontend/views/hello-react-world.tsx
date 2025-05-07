import {ViewConfig} from '@vaadin/hilla-file-router/types.js';
import {useSignal} from '@vaadin/hilla-react-signals';
import {Button, Notification, TextField} from '@vaadin/react-components';
import {HelloReactWorldService} from "../generated/endpoints";

export const config: ViewConfig = {
    menu: {order: 7, icon: 'line-awesome/svg/globe-solid.svg'},
    title: 'React Playground',
};

export default function HelloReactWorldView() {
    const name = useSignal('');

    return (
        <>
            <section className="flex p-m gap-m items-end">
                <TextField
                    label="Your name"
                    onValueChanged={(e) => {
                        name.value = e.detail.value;
                    }}
                />
                <Button
                    onClick={async () => {
                        const serverResponse = await HelloReactWorldService.sayHello(name.value);
                        Notification.show(serverResponse);
                    }}
                >Say hello</Button>
            </section>
        </>
    );
}
