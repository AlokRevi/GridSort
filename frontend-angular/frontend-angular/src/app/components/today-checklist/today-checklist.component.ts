import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

import {
  ChecklistItem,
  TodayChecklistResponse
} from '../../models/dashboard.models';
import { DateFormatService } from '../../services/date-format.service';

@Component({
  selector: 'app-today-checklist',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './today-checklist.component.html',
  styleUrl: './today-checklist.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TodayChecklistComponent {
  @Input() checklist: TodayChecklistResponse | null = null;

  @Output() markComplete = new EventEmitter<{
    taskId: number;
    occurrenceDate: string;
  }>();

  // Mobile/touch state for showing overdue list.
  overdueExpanded = false;

  constructor(public dateFormat: DateFormatService) {}

  get dueTodayItems(): ChecklistItem[] {
    return this.checklist?.items.filter(item => item.status === 'DUE_TODAY') ?? [];
  }

  get overdueItems(): ChecklistItem[] {
    return this.checklist?.items.filter(item => item.status === 'OVERDUE') ?? [];
  }

  get completedTodayItems(): ChecklistItem[] {
    return this.checklist?.items.filter(item => item.status === 'COMPLETED') ?? [];
  }

  get totalCount(): number {
    return this.checklist?.items.length ?? 0;
  }

  get activeCount(): number {
    return this.dueTodayItems.length + this.overdueItems.length;
  }

  toggleOverdueList(): void {
    this.overdueExpanded = !this.overdueExpanded;
  }

  onMarkComplete(item: ChecklistItem): void {
    this.markComplete.emit({
      taskId: item.taskId,
      occurrenceDate: item.occurrenceDate
    });
  }

  getDayNumber(item: ChecklistItem): number {
    return this.dateFormat.getDayNumber(item.occurrenceDate);
  }

  trackByChecklistItem(index: number, item: ChecklistItem): string {
    return `${item.taskId}-${item.occurrenceDate}`;
  }
}
