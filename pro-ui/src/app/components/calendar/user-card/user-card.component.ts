import { Component, Input, OnInit } from '@angular/core';
import { IUser, IUserMin } from '../../../interfaces/interfaces';

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.css']
})
export class UserCardComponent implements OnInit {

  @Input() user!: IUserMin;
  languages!: string[];

  constructor() { }

  ngOnInit(): void {
    this.languages = this.user?.language?.split('|');
  }

}
