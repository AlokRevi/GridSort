import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DateFormatService {
  toIsoDate(date = new Date()): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  toShortMonthDay(dateInput: string | Date): string {
    const date = this.toLocalDate(dateInput);
    const month = date.toLocaleDateString('en-US', { month: 'short' });

    return `${month} ${date.getDate()}`;
  }

  toWeekdayLongMonthOrdinal(dateInput: string | Date): string {
    const date = this.toLocalDate(dateInput);
    const weekday = date.toLocaleDateString('en-US', { weekday: 'short' });
    const month = date.toLocaleDateString('en-US', { month: 'long' });
    const day = date.getDate();

    return `${weekday}, ${month} ${day}${this.getOrdinalSuffix(day)}`;
  }

  getDayNumber(dateInput: string | Date): number {
    return this.toLocalDate(dateInput).getDate();
  }

  private toLocalDate(dateInput: string | Date): Date {
    if (dateInput instanceof Date) {
      return dateInput;
    }

    return new Date(`${dateInput}T00:00:00`);
  }

  private getOrdinalSuffix(day: number): string {
    if (day % 100 >= 11 && day % 100 <= 13) {
      return 'th';
    }

    switch (day % 10) {
      case 1:
        return 'st';
      case 2:
        return 'nd';
      case 3:
        return 'rd';
      default:
        return 'th';
    }
  }
}
