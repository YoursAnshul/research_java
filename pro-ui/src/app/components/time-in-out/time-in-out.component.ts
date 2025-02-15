import { Component, EventEmitter, Input, Output } from '@angular/core';
import { IActionButton, IAuthenticatedUser, ICurrentUser, IDateTimeCards, ITimeCard } from '../../interfaces/interfaces';
import { HttpClient } from '@angular/common/http';
import { UsersService } from '../../services/users/users.service';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { LogsService } from '../../services/logs/logs.service';
import { Utils } from '../../classes/utils';

@Component({
  selector: 'app-time-in-out',
  templateUrl: './time-in-out.component.html',
  styleUrl: './time-in-out.component.css'
})
export class TimeInOutComponent {
  @Input() public currentTimecard: ITimeCard = {} as ITimeCard;
  @Output() public currentTimecardChange: EventEmitter<ITimeCard> = new EventEmitter<ITimeCard>();

  public authenticatedUser: IAuthenticatedUser = {} as IAuthenticatedUser;
  public currentUser: ICurrentUser = {} as ICurrentUser;
  public currentTimecards: ITimeCard[] = [];
  public errorMessage!: string;
  public ipAddress: string = '';

  public showHistory: boolean = false;

  public dateTimeCards: IDateTimeCards[] = [];

  constructor(private http: HttpClient,
    private usersService: UsersService,
    private authenticationService: AuthenticationService,
    private globalsService: GlobalsService,
    private logsService: LogsService) {

    //get ip address
    this.getIpAddress();

    //get current user
    this.authenticationService.authenticatedUser.subscribe(currentUser => {
      this.authenticatedUser = currentUser;
      this.getCurrentUserData(this.authenticatedUser.netID);
    });

    this.setCurrentTimeCards();
  }

  ngOnInit(): void {
  }


  getCurrentUserData(netId: string): void {
    this.authenticationService.getCurrentUser(netId).subscribe((tcs) => {
      this.currentUser = tcs.Subject;
      if (this.currentUser.timecards?.length > 0) {
        this.currentTimecard = this.currentUser.timecards[0];
        this.currentTimecardChange.emit(this.currentTimecard);
        //use new Date() to set the stored UTC datetime to local browser datetime
        if (this.currentTimecard.datetimein) {
          this.currentTimecard.datetimein = new Date(this.currentTimecard.datetimein);
        }
        if (this.currentTimecard.datetimeout) {
          this.currentTimecard.datetimeout = new Date(this.currentTimecard.datetimeout);
        }
      } else if (this.authenticationService.authenticatedUser) {
        this.currentTimecard = this.blankTimeCard(this.authenticatedUser.netID);
        this.currentTimecardChange.emit(this.currentTimecard);
      }

      this.setCurrentTimeCards();

    });
  }




  currentTimecardIsToday(): boolean {
    //if we don't have a timecard, we have to return false
    if (this.currentTimecard) {
      //timeout
      if (this.currentTimecard.datetimeout) {
        //check if the timeout date is equal to the current date, adjusted for the 4 am cutoff
        if (this.currentDateAdjusted().getTime() == (Utils.formatDateOnly(new Date(this.currentTimecard.datetimeout)) || new Date()).getTime()) {
          return true;
        } else {
          return false;
        }
      //timein
      } else {
        //check if the timein date is equal to the current date, adjusted for the 4 am cutoff
        if (this.currentDateAdjusted().getTime() == (Utils.formatDateOnly(new Date(this.currentTimecard.datetimein || '')) || new Date()).getTime()) {
          return true;
        } else {
          return false;
        }
      }
    } else {
      return false;
    }
  }

  //return today's date adjusted for the 4 am cutoff
  private currentDateAdjusted(): Date {
    let todaysDate: Date = Utils.formatDateOnly(new Date()) || new Date();

    // let currentTime = new Date();

    // let today4Am: Date = Utils.formatDateOnly(new Date()) || new Date();
    // today4Am.setHours(4);

    // //return today's date - 1 if the current time is between midnight and 4:00 am of the current day
    // if (currentTime < today4Am && currentTime >= todaysDate) {
    //   todaysDate.setDate(todaysDate.getDate() - 1);
    //   return todaysDate;
    // //return the input date if the above conditions aren't met
    // } else {
      return todaysDate;
    // }
  }

  blankTimeCard(netId: string): ITimeCard {
    return {
      timeCardId: 0,
      dempoid: netId,
      entryBy: netId,
      entryDt: new Date(),
      modBy: netId,
      modDt: new Date(),
      timeIn: null,
      timeOut: null,
      datetimein: null,
      datetimeout: null,
      externalIp: null
    };
  }

  //confirmation before timing in if IP not on duke network
  confirmTimeIn(): void {

    let timeInFunction: Function = (): void => {
      this.timeIn();
    }

    let inDukeNetwork: boolean = false;;

    if (this.ipAddress.length > 0) {
      //regex to check if IP addres in range 152.3.*.* or 152.16.*.*
      if (/152\.3\.[0-9]*\.[0-9]*/.test(this.ipAddress)
        || /152\.16\.[0-9]*\.[0-9]*/.test(this.ipAddress)) {
        inDukeNetwork = true;
      }
    } else {
      //if we failed to get the ip address, we probably shouldn't tell them we think they aren't on the duke network
      inDukeNetwork = true;
    }

    if (!inDukeNetwork) {

      timeInFunction = (): void => {
        this.timeIn(this.ipAddress);
      }

      let timeInButton: IActionButton = {
        label: 'Time In Anyway',
        callbackFunction: timeInFunction
      }

      let noTimeInButton: IActionButton = {
        label: "Don't Time In",
        callbackFunction: (): void => { }
      }

      this.globalsService.displayPopupMessage(Utils.generatePopupMessageWithCallbacks('Timing in outside of Duke network', '<p>It appears you are trying to time in while not on the Duke network or connected to the Duke VPN.</p><p>You may continue to time in, but your manager will be notified.</p>', [timeInButton, noTimeInButton], true));

    } else {
      this.timeIn();
    }

  }

  timeIn(ipAddress: string | null = null): void {
    //create a deep copy of the current time card to add into the timecards array
    let timeCopy: ITimeCard = <ITimeCard>JSON.parse(JSON.stringify(this.currentTimecard));

    //create new timecard if we don't already have a blank one (already defaults to empty timecard for new users)
    if (!this.currentTimecard) {
      this.currentTimecard = this.blankTimeCard(this.authenticatedUser.netID);
      this.currentTimecardChange.emit(this.currentTimecard);
      this.currentUser.timecards = [this.currentTimecard, ...this.currentUser.timecards];
    } else if (this.currentTimecard.datetimeout) {
      this.currentTimecard = this.blankTimeCard(this.authenticatedUser.netID);
      this.currentTimecardChange.emit(this.currentTimecard);
      this.currentUser.timecards = [this.currentTimecard, ...this.currentUser.timecards];
    }
    
    //set timeine date/time to current
    this.currentTimecard.datetimein = new Date();
    this.currentTimecard.externalIp = ipAddress;

    //pass to timeinout api to time in
    this.usersService.timeInOut(this.currentTimecard).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let timeCardIds: number[] = this.currentUser?.timecards?.map(x => x.timeCardId);
          //if the copy of the previous timecard is null, add it to the history so we don't pull from the db again
          if (!timeCopy.timeCardId || !timeCardIds.includes(timeCopy.timeCardId)) {
            if (this?.currentUser?.timecards) {
              this.currentUser.timecards = [timeCopy, ...this.currentUser.timecards];
            } else {
              this.currentUser.timecards = [timeCopy];
            }
          }

          this.setCurrentTimeCards();

          //refresh timecards in current user
          let subject: ITimeCard = <ITimeCard>response.Subject;
          this.currentTimecard.timeCardId = subject.timeCardId;

        } else {
          //set timein to null on fail
          this.currentTimecard.datetimein = undefined;
        }
      },
      error => {
        this.currentTimecard.datetimein = undefined;
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  timeOut(): void {
    //add current time to timeout field in current timecard
    this.currentTimecard.datetimeout = new Date();

    //pass current time card to timeinout api to time out
    this.usersService.timeInOut(this.currentTimecard).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          //refresh timecards in current user

        } else {
          //on failure, wipe out the timeout field
          this.currentTimecard.datetimeout = undefined;
        }
      }
    );
    
  }

  formatDateOnlyWithMonthNameToString(inputDate: Date | null | undefined): string | null {
    if (!inputDate) {
      return null;
    }

    return Utils.formatDateOnlyWithMonthNameToString(inputDate);
  }

  formatTime(inputDate: Date | null | undefined): string | null {
    if (!inputDate) {
      return null;
    }

    return Utils.formatDateToTimeString(inputDate);
  }

  displayHoverMessage(event: any): void {
    if (this.currentUser) {
      if (this.currentUser.timecards) {
        if (this.currentUser.timecards.length > 0) {
          let htmlMessage: string = '';
          //htmlMessage = htmlMessage + '<p class="hover-message-title">Time In/Out History</p>';

          for (var i = 0; i < this.currentTimecards.length; i++) {
            let timecardInstance: ITimeCard = this.currentTimecards[i];
            if (this.formatDateOnlyWithMonthNameToString(timecardInstance?.datetimein) == this.formatDateOnlyWithMonthNameToString(timecardInstance?.datetimeout)
              || (timecardInstance?.datetimein && !timecardInstance?.datetimeout)) {
              htmlMessage = htmlMessage + '<p class="hover-message-title">' + this.formatDateOnlyWithMonthNameToString(timecardInstance?.datetimein) + '<p>';
            } else {
              htmlMessage = htmlMessage + '<p class="hover-message-title">' + this.formatDateOnlyWithMonthNameToString(timecardInstance?.datetimein) + ' – ' + (timecardInstance?.datetimeout ? this.formatDateOnlyWithMonthNameToString(timecardInstance?.datetimeout) : '–') + '<p>';
            }
            htmlMessage = htmlMessage + '<p><font class="bold" style="margin-left: 10px;">Time In:</font> <font class="italic">' + this.formatTime(timecardInstance?.datetimein) + '</font><p>';
            htmlMessage = htmlMessage + '<p><font class="bold" style="margin-left: 10px;">Time Out:</font> <font class="italic">' + (timecardInstance?.datetimeout ? this.formatTime(timecardInstance?.datetimeout) : '–') + '</font><p>';
          }

          let hoverMessage: HTMLElement = <HTMLElement>document.getElementById('hover-message');
          hoverMessage.innerHTML = htmlMessage;

          this.globalsService.showHoverMessage.next(true);

          hoverMessage.style.top = (event.screenY - 100) + 'px';
          hoverMessage.style.left = event.clientX + 'px';
        }
      }
    }

  }

  setCurrentTimeCards(): void {
    if (this.currentUser) {
      if (this.currentUser.timecards) {
        this.currentTimecards = this.currentUser.timecards.filter(x => (Utils.formatDateOnlyToString(x.datetimein) == Utils.formatDateOnlyToString(new Date()) || Utils.formatDateOnlyToString(x.datetimeout) == Utils.formatDateOnlyToString(new Date())));

        const uniqueDates = new Set(this.currentTimecards.map(tc => Utils.formatDateOnlyToString(tc.datetimein)));
        this.dateTimeCards = Array.from(uniqueDates).map(date => ({
          date: date ? new Date(date) : new Date(),
          timeCards: []
        }));

        for (var i = 0; i < this.dateTimeCards.length; i++) {
          this.dateTimeCards[i].timeCards = this.currentTimecards.filter(x => Utils.formatDateOnlyToString(x.datetimein) == Utils.formatDateOnlyToString(this.dateTimeCards[i].date));
        }

      }
    }
  }

  hideHoverMessage(): void {
    this.globalsService.showHoverMessage.next(false);
  }

  getIpAddress(): void {
    this.http.get('https://api.ipify.org/?format=json').subscribe(
      (response: any) => {
        this.ipAddress = response.ip;
        console.log('IP Address: ' + this.ipAddress);
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }
  
  isLastIteration(array: any[], index: number): boolean {

    return index === array.length - 1;

  }
}
