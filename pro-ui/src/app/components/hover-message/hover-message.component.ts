import { Component, Input } from '@angular/core';
import { HoverMessage } from '../../models/presentation/hover-message';

@Component({
  selector: 'app-hover-message',
  templateUrl: './hover-message.component.html',
  styleUrl: './hover-message.component.css'
})
export class HoverMessageComponent {

  @Input() hoverMessage: HoverMessage | undefined = new HoverMessage();

  constructor() { }

  ngOnInit(): void {
    
  }

  public hoverMessageStyle(): any {
    if (!this.hoverMessage) {
      return {};
    } else {
      return {
        'top': this.hoverMessage.styleTop,
        'left': this.hoverMessage.styleLeft,
        'margin-top': this.hoverMessage.styleMarginTop
      };
    }
  }

}
