import { Component } from '@angular/core';
import { SelectedValue } from '../../../models/presentation/selected-value';
import { IDropDownValue } from '../../../interfaces/interfaces';

@Component({
  selector: 'app-user-core-hours',
  templateUrl: './user-core-hours.component.html',
  styleUrl: './user-core-hours.component.css'
})
export class UserCoreHoursComponent {

  dropDownValues: IDropDownValue[] = [
    { codeValues: 1, dropDownItem: 'Interviewer' },
    { codeValues: 2, dropDownItem: 'Resource Group' }
  ];
  selectedValues: SelectedValue[] = [
    new SelectedValue(1, { codeValues: 1, dropDownItem: 'Interviewer' })
  ];

  userRoleChange(event: any) {
    console.log(event);
  }

}
