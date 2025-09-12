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
          <li class="mt5">A shift schedule for Saturday should begin at or after 9 AM.</li>
          <li class="mt5">A shift schedule for Sunday should begin at or after 12 noon.</li>
          <li class="mt5">An interviewer's weekly schedule should match their core hours.</li>
          <li class="mt5">An Interviewer's weekly schedule should not exceed 20 hours total.</li>
          <li class="mt5">An interviewer's schedule should include at least 2 night shifts, until at or after 9 p.m., each month.</li>
          <li class="mt5">An interviewer's schedule should include at least 2 weekend shifts each month.</li>
          <li class="mt5">An interviewer cannot schedule before 1 p.m. Monday through Friday (greyed out).</li>
          <li class="mt5 ml-5">A Saturday or Sunday shift schedule should be 4 hours minimum.</li>
        </ul>
      <br />
      <p class="ft700">Scheduling Level 2</p>
        <ul>
          <li>A shift schedule should be at least 4 hours in length.</li>
          <li class="mt5">A shift schedule cannot be exactly 8 hours in length.</li>
          <li class="mt5">An interviewer's weekly schedule should match their core hours total (40 if 40, 15 if 15)</li>
          <li class="mt5">An interviewer's weekly schedule should not exceed 40 hours total.</li>
          <li class="mt5">An interviewer's schedule should include at least 2 weekend shifts each month</li>
          <li class="mt5 ml-5">A Saturday or Sunday shift schedule should be 4 hours minimum</li> 
        </ul>
      <br />
      <p class="ft700">Scheduling Level 3</p>
        <ul>
          <li>A shift schedule cannot be exactly 8 hours in length.</li>
          <li class="mt5">An interviewer's weekly schedule should match their core hours total.</li>
          <li class="mt5">An Interviewer's weekly schedule should not exceed 40 hours total.</li>
        </ul>
    `;  
}
