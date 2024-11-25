import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { Utils } from '../../classes/utils';
import { IPopupMessage } from '../../interfaces/interfaces';
import { UserSchedulesService } from '../userSchedules/user-schedules.service';

@Injectable({
  providedIn: 'root'
})
export class GlobalsService {
  public selectedPage: BehaviorSubject<string> = new BehaviorSubject<string>('');
  public showScheduler: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public showBlackFilter: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public showScheduleBlackFilter: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public showPopupMessage: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public currentChanges: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public popupMessage: BehaviorSubject<IPopupMessage> = new BehaviorSubject<IPopupMessage>({} as IPopupMessage);
  public showHoverMessage: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public contextNetId: BehaviorSubject<string | null> = new BehaviorSubject<string | null>('');
  public contextProjectName: BehaviorSubject<string | null> = new BehaviorSubject<string | null>('');
  public scheduleTabIndex: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  public sessionEndReminderSent: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public showParticipantModal: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  constructor(private userSchedulesService: UserSchedulesService) { }

  public openSchedulePopup(): void {
    this.scheduleTabIndex.next(0);
    this.showScheduler.next(true);
    this.showScheduleBlackFilter.next(true);
  }

  public closeSchedulePopup(): void {
    this.showScheduler.next(false);
    this.showScheduleBlackFilter.next(false);

    //clear context variables
    this.scheduleTabIndex.next(0);
    this.contextNetId.next(null);
    this.contextProjectName.next(null);
  }

  showContextualPopup(scheduleTabIndex: number,
    contextNetId: string | null,
    contextProjectName: string | null,
    contextDate: Date | null): void {

    this.scheduleTabIndex.next(scheduleTabIndex);

    if (contextNetId) {
      this.contextNetId.next(contextNetId);
    }

    if (contextProjectName) {
      this.contextProjectName.next(contextProjectName);
    }

    if (contextDate) {
      this.userSchedulesService.selectedDate.next(contextDate);
      this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(contextDate, true, true, true));
    }

    this.showScheduler.next(true);
    this.showScheduleBlackFilter.next(true);

  }

  displayBlackFilter(): void {
    this.showBlackFilter.next(true);
  }

  hideBlackFilter(): void {
    this.showBlackFilter.next(false);
  }

  displayPopupMessage(popupMessage: IPopupMessage): void {
    this.popupMessage.next(popupMessage);
    this.showPopupMessage.next(true);
  }

  hidePopupMessage(): void {
    this.popupMessage.next({} as IPopupMessage);
    this.showPopupMessage.next(false);
  }

  displayHoverMessage(popupMessage: IPopupMessage): void {
    this.showHoverMessage.next(true);
  }

  hideHoverMessage(): void {
    this.showHoverMessage.next(false);
  }

  public openParticipantModal(): void {
    this.showParticipantModal.next(true);
  }

  public closeParticipantModal(): void {
    this.showParticipantModal.next(false);
  }

}
