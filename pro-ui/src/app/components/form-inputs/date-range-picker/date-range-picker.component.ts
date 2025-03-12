import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-date-range-picker',
  templateUrl: './date-range-picker.component.html',
  styleUrl: './date-range-picker.component.css'
})
export class DateRangePickerComponent implements OnInit {
  dateRangeForm: FormGroup;
  showSelectedRange = false;
  
  // Calendar variables
  currentDate = new Date();
  calendarDays: Date[] = [];
  daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  
  // Range selection variables
  startDate: Date | null = null;
  endDate: Date | null = null;
  isSelectingRange = false;
  
  @Input() weeklySelectionMode = false;
  @Output() dateRangeSelected = new EventEmitter<{startDate: string, endDate: string}>();
  
  constructor(private fb: FormBuilder) {
    this.dateRangeForm = this.fb.group({});
  }
  
  get currentMonthYear(): string {
    return this.currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  }
  
  ngOnInit(): void {
    this.generateCalendarDays();
    this.setQuickRange('month');
  }
  
  generateCalendarDays(): void {
    this.calendarDays = [];
    
    // Get first day of the month
    const firstDay = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), 1);
    
    // Get the last day of the month
    const lastDay = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 0);
    
    // Get the day of the week for the first day (0 = Sunday, 6 = Saturday)
    const firstDayOfWeek = firstDay.getDay();
    
    // Add empty days for padding from previous month
    const prevMonthLastDay = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), 0).getDate();
    for (let i = 0; i < firstDayOfWeek; i++) {
      const day = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, prevMonthLastDay - firstDayOfWeek + i + 1);
      this.calendarDays.push(day);
    }
    
    // Add days of current month
    for (let i = 1; i <= lastDay.getDate(); i++) {
      const day = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), i);
      this.calendarDays.push(day);
    }
    
    // Add days from next month to complete the calendar grid (42 days total - 6 weeks)
    const remainingDays = 42 - this.calendarDays.length;
    for (let i = 1; i <= remainingDays; i++) {
      const day = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, i);
      this.calendarDays.push(day);
    }
  }
  
  getDayClasses(day: Date): string {
    const isToday = this.isSameDay(day, new Date());
    const isCurrentMonth = day.getMonth() === this.currentDate.getMonth();
    const isStartDate = this.startDate !== null && this.isSameDay(day, this.startDate);
    const isEndDate = this.endDate !== null && this.isSameDay(day, this.endDate);
    const isInRange = this.isDateInRange(day);
    
    let classNames = [
      'text-center py-1 cursor-pointer select-none',
      isCurrentMonth ? 'text-gray-800' : 'text-gray-400'
    ];
    
    if (isToday) {
      classNames.push('ring-2 ring-blue-200 font-medium');
    }
    
    if (isStartDate || isEndDate) {
      classNames.push('bg-blue-500 text-white rounded-md font-medium');
    } else if (isInRange) {
      classNames.push('bg-blue-100 text-blue-800');
    } else {
      classNames.push('hover:bg-gray-100');
    }
    
    return classNames.join(' ');
  }
  
  isSameDay(date1: Date, date2: Date): boolean {
    return date1.getFullYear() === date2.getFullYear() &&
           date1.getMonth() === date2.getMonth() &&
           date1.getDate() === date2.getDate();
  }
  
  isDateInRange(date: Date): boolean {
    if (!this.startDate || !this.endDate) {
      return false;
    }
    
    return date > this.startDate && date < this.endDate;
  }
  
  selectDate(day: Date): void {
    if (this.weeklySelectionMode) {
      this.selectWeekFromDate(day);
      return;
    }
    
    if (!this.isSelectingRange || !this.startDate) {
      // Start new range selection
      this.startDate = new Date(day);
      this.endDate = new Date(day);
      this.isSelectingRange = true;
    } else {
      // Complete the range selection
      if (day < this.startDate) {
        // If selected date is before start date, swap them
        this.endDate = new Date(this.startDate);
        this.startDate = new Date(day);
      } else {
        this.endDate = new Date(day);
      }
      
      this.isSelectingRange = false;
      this.emitDateRange();
    }
    
    this.showSelectedRange = true;
  }
  
  selectWeekFromDate(date: Date): void {
    // Find the Monday of this week
    const day = date.getDay();
    const diff = date.getDate() - day + (day === 0 ? -6 : 1); // Adjust when day is Sunday
    const monday = new Date(date);
    monday.setDate(diff);
    
    // Find the Sunday
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    
    this.startDate = monday;
    this.endDate = sunday;
    
    // Update display and emit event
    this.showSelectedRange = true;
    this.emitDateRange();
  }
  
  previousMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.generateCalendarDays();
  }
  
  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.generateCalendarDays();
  }
  
  setQuickRange(range: string): void {
    const today = new Date();
    
    switch (range) {
      case 'today':
        this.startDate = new Date(today);
        this.endDate = new Date(today);
        break;
      case 'week':
        if (this.weeklySelectionMode) {
          this.selectWeekFromDate(today);
          return;
        }
        // Current week (Monday to Sunday)
        const currentDay = today.getDay();
        const mondayDiff = currentDay === 0 ? -6 : 1 - currentDay;
        const monday = new Date(today);
        monday.setDate(today.getDate() + mondayDiff);
        
        const sunday = new Date(monday);
        sunday.setDate(monday.getDate() + 6);
        
        this.startDate = monday;
        this.endDate = sunday;
        break;
      case 'month':
        // Current month
        this.startDate = new Date(today.getFullYear(), today.getMonth(), 1);
        this.endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0);
        break;
      case 'lastWeek':
        // Last week
        const lastMonday = new Date(today);
        lastMonday.setDate(today.getDate() - (today.getDay() === 0 ? 13 : 6 + today.getDay()));
        
        const lastSunday = new Date(lastMonday);
        lastSunday.setDate(lastMonday.getDate() + 6);
        
        this.startDate = lastMonday;
        this.endDate = lastSunday;
        break;
      case 'lastMonth':
        // Last month
        this.startDate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        this.endDate = new Date(today.getFullYear(), today.getMonth(), 0);
        break;
      default:
        return;
    }
    
    // Make sure the calendar shows the month of the start date
    this.currentDate = new Date(this.startDate);
    this.generateCalendarDays();
    
    this.showSelectedRange = true;
    this.isSelectingRange = false;
    this.emitDateRange();
  }
  
  emitDateRange(): void {
    if (!this.startDate || !this.endDate) return;
    
    this.dateRangeSelected.emit({
      startDate: this.formatDateForInput(this.startDate),
      endDate: this.formatDateForInput(this.endDate)
    });
  }
  
  formatDateForInput(date: Date): string {
    return date.toISOString().split('T')[0];
  }
  
  formatDate(date: Date | null): string {
    if (!date) return '';
    
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
  
  calculateDaysDifference(): number {
    if (!this.startDate || !this.endDate) return 0;
    
    const diffTime = Math.abs(this.endDate.getTime() - this.startDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    return diffDays + 1; // Include both start and end days
  }
}