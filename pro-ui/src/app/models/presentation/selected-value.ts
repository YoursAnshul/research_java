import { IDropDownValue } from "../../interfaces/interfaces";

export class SelectedValue {

    public value: any;
    public item: IDropDownValue | undefined;

    constructor(value: any, item: IDropDownValue | undefined = undefined) {
        this.value = value;
        this.item = item;
    }

};
