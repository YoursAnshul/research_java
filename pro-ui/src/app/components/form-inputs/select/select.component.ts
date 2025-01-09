import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { IDropDownValue } from '../../../interfaces/interfaces';
import { SelectedValue } from '../../../models/presentation/selected-value';
import { Utils } from '../../../classes/utils';

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrl: './select.component.css'
})
export class SelectComponent {

  @Input() fieldLabel: string = "";
  @Input() anyLabel: string = "";
  @Input() anyValue: string = '0';
  @Input() multiSelect: boolean = false;
  @Input() hideInput: boolean = false;
  @Input() dropDownValues!: IDropDownValue[];
  @Input() selectedValues: SelectedValue[] = [];
  @Output() selectedValuesChange = new EventEmitter<any[]>();
  @Input() showDropdown: boolean = false;
  @Output() showDropdownChange = new EventEmitter<boolean>();
  @Input() disabled: boolean = false;

  @Input() anyValueSelected: boolean = false;
  @Output() anyValueSelectedChange = new EventEmitter<boolean>();

  public guid: string = '';

  constructor() { }

  ngOnInit(): void {
    if ((this.anyLabel || '').length > 0 && (this.selectedValues.length === 0 || this.selectedValues[0].value === this.anyValue)) {
      this.anyValueSelected = true;
    }
    this.guid = Utils.generateGUID();
  }

  //apply filters to the current calendar view
  public applyAnyFilter(): void {
    if (this.anyValueSelected) {
      this.selectedValues = [new SelectedValue(this.anyValue)];
    } else {
      this.selectedValues = this.selectedValues.filter((x: SelectedValue) => x.value !== this.anyValue);
    }
    
    // this.selectedValues = [...this.selectedValues];
    this.selectedValuesChange.emit(this.selectedValues);
  }

  //toggle the dropdown menu
  public toggleDropdown(): void {
    if (this.disabled) {
      return;
    }

    this.showDropdown = !this.showDropdown;
    this.showDropdownChange.emit(this.showDropdown);
  }

  //get the selected values as a string
  public getSelectedValues(): string {
    if ((this.anyLabel || '').length > 0
      && (this.selectedValues.length === 0 || this.anyValueSelected)) {
      return `Any ${this.anyLabel}`;
    } else if (this.selectedValues.length === 0) {
      return `${this.fieldLabel}`;
    }
    
    return this.selectedValues.map(x => (x.item?.dropDownItem || '')).join(', ');
  }

  //action to take when the user clicks the 'Any' option
  public selectAny(event: any = undefined): void {

    if (event) {
      if (this.anyValueSelected) {
        this.selectedValues = [new SelectedValue(this.anyValue)];
        event.preventDefault();
      }
    }

    this.anyValueSelected = true;
    this.anyValueSelectedChange.emit(this.anyValueSelected);
    this.applyAnyFilter();
  }

  //action to take when the user clicks a value
  public toggleSelectedValue(dv: IDropDownValue): void {
    //if this is a single select dropdown, clear the other values
    if (!this.multiSelect) {
      this.selectedValues = [new SelectedValue(dv.codeValues, dv)];
      this.showDropdown = false;
      this.showDropdownChange.emit(this.showDropdown);
      this.selectedValuesChange.emit(this.selectedValues);
      return;
    } else {
    
      //if the value is already selected, deselect it
      if (this.selectedValues.map(x => x.value).includes(dv.codeValues)) {
        this.selectedValues = this.selectedValues.filter((x: SelectedValue) => x.value !== dv.codeValues);
      //otherwise, select it
      } else {
        this.selectedValues.push(new SelectedValue(dv.codeValues, dv));
      }

      if (this.selectedValues.length === 0) {
        this.anyValueSelected = true;
      } else {
        this.anyValueSelected = false;
      }
      this.anyValueSelectedChange.emit(this.anyValueSelected);

    }

    this.applyAnyFilter();
  }

  //check if the value is selected
  public isValueSelected(dv: IDropDownValue): boolean { 

    if (this.selectedValues.map(x => x.value).includes(dv.codeValues)) {
      return true;
    } else {
      return false;
    }

  }

  //close the dropdown menu when clicking outside this component
  // @HostListener('document:click', ['$event'])
  // handleClickOutside(event: Event) {
  //   const target = event.target as HTMLElement;
  //   if (!target.closest('app-select') && !target.closest('.filter-toggle')) {
  //     this.showDropdown = false;
  //     this.showDropdownChange.emit(this.showDropdown);
  //   }
  // }
  @HostListener('document:click', ['$event'])
  handleClickOutside(event: Event) {
  const target = event.target as HTMLElement;
  const clickedComponent = target.closest('.app-select');
  if (clickedComponent) {
    const clickedGuid = clickedComponent.getAttribute('data-guid');
    if (clickedGuid !== this.guid) {
      this.showDropdown = false;
      this.showDropdownChange.emit(this.showDropdown);
    }
  } else if (!target.closest('.filter-toggle')) {
    this.showDropdown = false;
    this.showDropdownChange.emit(this.showDropdown);
  }
}

  //close the dropdown menu when the user presses escapte
  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      this.showDropdown = false;
      this.showDropdownChange.emit(this.showDropdown);
    }
  }

}
