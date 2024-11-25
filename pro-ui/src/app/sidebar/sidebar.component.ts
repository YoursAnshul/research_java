import { Component } from '@angular/core';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication/authentication.service';
import { GlobalsService } from '../services/globals/globals.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
  animations: [
    trigger('slideInOut', [
      state('in', style({
        width: '250px'
      })),
      state('out', style({
        width: '70px'
      })),
      transition('in <=> out', animate('300ms ease-in-out'))
    ])
  ]
})
export class SidebarComponent {
  showHoverMessage: boolean = false;
  menuItems = [
    [
      { id: 1, icon: 'home', label: 'Home', route: '/home' },
      { id: 2, icon: 'calendar_clock', label: 'Scheduling', route: '/scheduling' },
      { id: 3, icon: 'list_alt', label: 'Assignments', route: '/assignments' },
      { id: 4, icon: 'group', label: 'Participants', route: '/participants' },
      { id: 5, icon: 'stacks', label: 'Projects', route: '/projects' },
      { id: 6, icon: 'forum', label: 'Communications', route: '/communications' },
      { id: 7, icon: 'description', label: 'Reports', route: '/reports' }
    ]
    // Add more menu items as needed
  ];

  selectedItemId: number | null = null;
  isCollapsed = false;
  showSchedule: boolean = false;
  isInterviewer = true;
  showParticipantModal: boolean = false;
  constructor(private router: Router, private authenticationService: AuthenticationService, private globalsService: GlobalsService) {
    this.globalsService.showHoverMessage.subscribe(
      showHoverMessage => {
        this.showHoverMessage = showHoverMessage;
      }
    );
    this.globalsService.showScheduler.subscribe(
      showSchedule => {
        this.showSchedule = showSchedule;
      }
    );
    this.globalsService.showParticipantModal.subscribe(
      showParticipantModal => {
        this.showParticipantModal = showParticipantModal;
      }
    );
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        if (!authenticatedUser?.interviewer) {
          this.isInterviewer = false;
          this.menuItems.push(
            [
              { id: 8, icon: 'shield_person', label: 'Users', route: '/users' },
              { id: 9, icon: 'calendar_month', label: 'Forecasting', route: '/forecasting' },
              { id: 10, icon: 'chat', label: 'Requests', route: '/requests' },
              { id: 11, icon: 'settings', label: 'Configuration', route: '/configuration' },
            ])
        }
      }
    );
  }

  toggleNav() {
    this.isCollapsed = !this.isCollapsed;
  }

  selectItem(id: number) {
    this.selectedItemId = id;
    const selectedItem = this.findItemById(id);
    if (selectedItem) {
      this.router.navigate([selectedItem.route]);
    }
  }

  findItemById(itemId: number) {
    for (let group of this.menuItems) {
      for (let item of group) {
        if (item.id === itemId) {
          return item;
        }
      }
    }
    return null;
  }
}
