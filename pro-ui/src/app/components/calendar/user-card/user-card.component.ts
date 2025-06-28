import { Component, Input, OnInit } from '@angular/core';
import { User } from '../../../models/data/user';

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.css']
})
export class UserCardComponent implements OnInit {

  @Input() user!: User;
  languages!: string[];

  constructor() { }

  ngOnInit(): void {
    this.languages = this.user?.language?.split('|');
  }

}
