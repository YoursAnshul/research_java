  import { Component } from '@angular/core';
  import { SelectedValue } from '../../../models/presentation/selected-value';
  import { IDropDownValue } from '../../../interfaces/interfaces';

  @Component({
    selector: 'app-project-forecasting',
    templateUrl: './project-forecasting.component.html',
    styleUrl: './project-forecasting.component.css'
  })
  export class ProjectForecastingComponent {
    filterData: any[] = [];
    paginatedData: any[] = [];
    currentPage: number = 1;
    

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
