import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { IAnyFilter, IDropDownValue } from '../../interfaces/interfaces';

@Component({
  selector: 'app-any-select',
  templateUrl: './any-select.component.html',
  styleUrls: ['./any-select.component.css']
})
export class AnySelectComponent implements OnInit {
  @Input() fieldLabel: string = "";
  @Input() anyLabel: string = "";
  @Input() dropDownValues!: IDropDownValue[];
  @Output() filterChange = new EventEmitter<FormControl>();

  @Input() selectFilter: FormControl = new FormControl(['0']);
  anyToggle: boolean = false;

  constructor() { }

  ngOnInit(): void {
  }

  //apply filters to the current calendar view
  public applyAnyFilter(): void {
    //unset defaults if other options are selected
    let anyFilter: IAnyFilter = this.anyFilter(this.selectFilter, this.anyToggle);
    this.selectFilter.setValue(anyFilter.filterControl.value);
    this.anyToggle = anyFilter.anyToggle;
  }

  //handle select, deselect and auto-select of the "any" values in multi-selects
  public anyFilter(filter: FormControl, anyToggle: boolean, anyValue: string = '0'): IAnyFilter {
    //if we have any values selected
    if (filter.value.length > 0) {
      //check if the any toggle is set and we have the any value (default of "0") selected. If so, set the toggle to false and filter to only (default of "0") selected
      if (anyToggle && (filter.value.includes(anyValue))) {
        anyToggle = false;
        filter.setValue(filter.value.filter((x: string) => x === anyValue));
        //otherwise check if the any toggle is false and set the toggle to true and filter to where all selctions but the any value (default of "0") is selected
        //also make sure that there isn't only 1 value or we can end up clearing all values if the toggle is false but we do have any set (those toggles are tricksy)
      } else if (!anyToggle && filter.value.length > 1) {
        //but this isn't necessary unless we have the any value (default of "0") selected
        if (filter.value.includes(anyValue)) {
          anyToggle = true;
          filter.setValue(filter.value.filter((x: string) => x !== anyValue));
        }
      }
      //if no values selected, set to default of any value (default of "0")
    } else {
      anyToggle = false;
      filter.setValue([anyValue]);
    }

    //return an interface with both the form control and the any toggle
    return <IAnyFilter>{
      filterControl: filter,
      anyToggle: anyToggle
    };
  }

  selectionChange(): void {
    this.applyAnyFilter();
    this.filterChange.emit(this.selectFilter);
  }

}
