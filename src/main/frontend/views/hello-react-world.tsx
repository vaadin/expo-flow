import {ViewConfig} from '@vaadin/hilla-file-router/types.js';
import {useSignal} from '@vaadin/hilla-react-signals';
import {Button, Grid, GridColumn, Notification, TextField} from '@vaadin/react-components';
import {HelloReactWorldService, PersonService} from "../generated/endpoints";
import Person from "../generated/com/example/application/data/entity/Person";
import {useEffect} from "react";
import person from "../generated/com/example/application/data/entity/Person";
import {AutoGrid} from "@vaadin/hilla-react-crud";
import PersonModel from "../generated/com/example/application/data/entity/PersonModel";

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
                    placeholder="Your name"
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
