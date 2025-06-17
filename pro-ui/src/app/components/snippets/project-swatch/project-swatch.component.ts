import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-project-swatch',
  templateUrl: './project-swatch.component.html',
  styleUrls: ['./project-swatch.component.css']
})
export class ProjectSwatchComponent implements OnInit {
  @Input() projectColor: string = '';
  @Input() projectAbbr: string | undefined = undefined;
  @Input() size: string = '';
  @Input() customStyle: any = {};
  constructor() { }

  ngOnInit(): void {
    //project color
    if (this.projectColor) {
      if (!this.customStyle.hasOwnProperty('background-color')) {
        this.customStyle['background-color'] = this.projectColor;
      }
      if (this.size) {
        this.customStyle['height'] = this.size;
      }
    }
  }

}
