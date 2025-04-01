import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  private scheduleData = new BehaviorSubject<any>(null);  
  private selectedUser = new BehaviorSubject<any>(null); 
  private scheduleEditData = new BehaviorSubject<any>(null);  

  private tab = new BehaviorSubject<any>(null); 


  setSchedule(schedule: any): void {
    this.scheduleData.next(schedule);
  }

  getSchedule() {
    return this.scheduleData.asObservable();
  }

  clearSchedule(): void {
    this.scheduleData.next(null);
  }
  setUser(user: any): void {
    this.selectedUser.next(user);
  }

  getUser() {
    return this.selectedUser.asObservable();
  }

  clearUser(): void {
    this.selectedUser.next(null);
  }
  setTab(tab: any): void {
    this.tab.next(tab);
  }

  getTab() {
    return this.tab.asObservable();
  }

  clearTab(): void {
    this.tab.next(null);
  }
  setScheduleEditData(schedule: any): void {
    this.scheduleEditData.next(schedule);
  }

  getScheduleEditData() {
    return this.scheduleEditData.asObservable();
  }

  clearScheduleEditData(): void {
    this.scheduleEditData.next(null);
  }
}
