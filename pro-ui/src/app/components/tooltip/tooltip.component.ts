import { Component, Input, ElementRef, Renderer2 } from '@angular/core';

@Component({
  selector: 'app-tooltip',
  templateUrl: './tooltip.component.html',
  styleUrls: ['./tooltip.component.css']
})
export class TooltipComponent {
  @Input() text: string = '';
  @Input() position: 'top' | 'bottom' | 'left' | 'right' = 'right';
  show = false;
  tooltipStyle: { [key: string]: string } = {};

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  showTooltip(event: MouseEvent) {
    this.show = true;
    this.setTooltipPosition(event);
  }

  hideTooltip() {
    this.show = false;
  }

  moveTooltip(event: MouseEvent) {
    // No-op: Tooltip position is anchored to the element, not the cursor
  }

  private setTooltipPosition(event: MouseEvent) {
    const offset = 10;
    const target = event.target as HTMLElement;
    const rect = target.getBoundingClientRect();
    let top = 0;
    let left = 0;
    let transform = '';
    switch (this.position) {
      case 'top':
        if (this.position === 'top') {
          top = rect.top - rect.height - offset;
        } else {
          top = rect.top - offset;
        }
        left = rect.left + rect.width / 2;
        transform = 'translateX(-50%)';
        break;
      case 'bottom':
        top = rect.bottom + offset;
        left = rect.left + rect.width / 2;
        transform = 'translateX(-50%)';
        break;
      case 'left':
        top = rect.top + rect.height / 2;
        left = rect.left - offset;
        transform = 'translateY(-50%)';
        break;
      case 'right':
      default:
        top = rect.top + rect.height / 2;
        left = rect.right + offset;
        transform = 'translateY(-50%)';
        break;
    }
    this.tooltipStyle = {
      top: `${top}px`,
      left: `${left}px`,
      transform: transform
    };
  }
} 