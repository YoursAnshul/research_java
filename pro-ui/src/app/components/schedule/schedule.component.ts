import { Component, OnInit } from '@angular/core';

import { GlobalsService } from '../../services/globals/globals.service';

@Component({
  selector: 'app-schedule',
  templateUrl: './schedule.component.html',
  styleUrls: ['./schedule.component.css']
})
export class ScheduleComponent implements OnInit {


  constructor(
    private globalsService: GlobalsService) {

   
  }
 ngOnInit(): void {
   
 }

  //-----------------------
  //button actions
  //-----------------------

  //close the scheduler popup
  closeScheduler(): void {
    let leaveFunction: Function = (): void => {
      this.globalsService.closeSchedulePopup();
     }

   
      leaveFunction();
    
    //if (this.userSchedulesMonth.filter(x => x.changed).length > 0
    //  && this.userSchedulesMonth.filter(x => x.changed).length > this.userSchedulesMonth.filter(x => x.addInitial).length) {
    //  if (!confirm('You have unsaved changed, are you sure you want to close the scheduler and lose all unsaved work?')) {
    //    return;
    //  }
    //}

    //this.globalsService.closeSchedulePopup();
    //this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(this.selectedDate, true, true, true));
  };

  
}
