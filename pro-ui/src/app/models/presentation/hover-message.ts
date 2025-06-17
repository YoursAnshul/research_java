
export class HoverMessage {

    public guid: string | undefined = undefined;
    public showHoverMessage: boolean = false;
    public htmlMessage: string = '';
    public styleTop: string = '';
    public styleLeft: string = '';
    public styleMarginTop: string = '';

    constructor() { }

    //set the guid for the hover message
    public setGuid(): void {
        this.guid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }

    //set the hover message style and show it
    public setAndShow(event: any, htmlMessage: string): void {
    
        this.setGuid();

        this.htmlMessage = htmlMessage;
    
        this.showHoverMessage = true;
    
        this.styleTop = (event.clientY) + 'px';
        this.styleLeft = event.clientX + 'px';
        this.styleMarginTop = '-70px';
    }

    //hide the hover message
    public hide(): void {
        this.guid = undefined;
        setTimeout(() => {
        if (this.guid === undefined) {
            this.showHoverMessage = false;
        }
        }, 100);
    }
    
};
