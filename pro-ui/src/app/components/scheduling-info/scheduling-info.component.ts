import { Component } from '@angular/core';

@Component({
  selector: 'app-scheduling-info',
  templateUrl: './scheduling-info.component.html',
  styleUrl: './scheduling-info.component.css'
})
export class SchedulingInfoComponent {
    htmlMessage: string = `
      <p class="ft700">Scheduling Level 1</p>
        <ul>
          <li>A shift schedule should be at least 4 hours in length.</li>
          <li class="mt5">A shift schedule should be no more than 7 hours in length.</li>
          <li class="mt5">A shift schedule for a weekday, Monday thru Friday, should begin at or after 1 PM.</li>
          <li class="mt5">A shift schedule for Saturday should begin at or after 9 AM.</li>
          <li class="mt5">A shift schedule for Sunday should begin at or after 12 noon.</li>
          <li class="mt5">An Interviewer's weekly schedule should at a minimum match their core hours total.</li>
          <li class="mt5">An Interviewer's weekly schedule should not exceed 20 hours total.</li>
          <li class="mt5">An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>
          <li class="mt5">An Interviewer's schedule should include 1 weekend shift every other week.</li>
          <ul>
            <li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>
            <li class="mt5">A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li>
          </ul>
        </ul>
      <br />
      <p class="ft700">Scheduling Level 2</p>
        <ul>
          <li>A shift schedule should be at least 4 hours in length.</li>
          <li class="mt5">A shift schedule cannot be exactly 8 hours in length.</li>
          <li class="mt5">An Interviewer's weekly schedule should at a minimum match their core hours total.</li>
          <li class="mt5">An Interviewer's weekly schedule should not exceed 40 hours total.</li>
          <li class="mt5">An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>
          <li class="mt5">An Interviewer's schedule should include 1 weekend shift every other week.</li>
          <ul>
            <li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>
            <li class="mt5">A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li>
          </ul>
        </ul>
      <br />
      <p class="ft700">Scheduling Level 3</p>
        <ul>
          <li>A shift schedule cannot be exactly 8 hours in length.</li>
          <li class="mt5">An Interviewer's weekly schedule should at a minimum match their core hours total.</li>
          <li class="mt5">An Interviewer's weekly schedule should not exceed 40 hours total.</li>
        </ul>
    `;  
}
