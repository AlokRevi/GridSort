import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

import {
  TimelineCategoryResponse,
  TimelineCellResponse,
  TimelineDashboardResponse,
  TimelineOccurrenceBucketResponse,
  TimelineTaskResponse
} from '../../models/dashboard.models';

@Component({
  selector: 'app-quadrimester-timeline-grid',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './quadrimester-timeline-grid.component.html',
  styleUrl: './quadrimester-timeline-grid.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class QuadrimesterTimelineGridComponent {
  @Input() dashboard: TimelineDashboardResponse | null = null;
  @Input() categories: TimelineCategoryResponse[] = [];

  get gridTemplateColumns(): string {
    return `240px repeat(${this.dashboard?.cells.length ?? 0}, 62px)`;
  }

  bucketForCell(
    task: TimelineTaskResponse,
    cell: TimelineCellResponse
  ): TimelineOccurrenceBucketResponse | null {
    return task.buckets.find(bucket => bucket.cellKey === cell.key) ?? null;
  }

  hasOccurrences(task: TimelineTaskResponse): boolean {
    return task.buckets.some(bucket => bucket.totalOccurrences > 0);
  }

  cellBucketLabel(cell: TimelineCellResponse): string {
    if (cell.cellType === 'WEEKDAY_BUCKET') {
      return 'Weekdays';
    }

    if (cell.cellType === 'WEEKEND_BUCKET') {
      return 'Weekend';
    }

    return cell.secondaryLabel || cell.label;
  }

  cellBucketRangeLabel(cell: TimelineCellResponse): string {
    if (cell.cellType === 'WEEKDAY_BUCKET') {
      return 'Mon-Fri';
    }

    if (cell.cellType === 'WEEKEND_BUCKET') {
      return 'Sat-Sun';
    }

    return cell.startDate === cell.endDate
      ? cell.startDate
      : `${cell.startDate} to ${cell.endDate}`;
  }

  bucketDetailLabel(bucket: TimelineOccurrenceBucketResponse): string {
    const total = bucket.totalOccurrences;

    if (total === 1) {
      return '1 generated';
    }

    return `${total} generated`;
  }

  bucketAccessibleLabel(
    bucket: TimelineOccurrenceBucketResponse,
    cell: TimelineCellResponse
  ): string {
    const total = bucket.totalOccurrences;

    if (total === 0) {
      return `No generated occurrences from ${cell.startDate} to ${cell.endDate}`;
    }

    return `${bucket.completedOccurrences} completed of ${total} generated occurrences from ${cell.startDate} to ${cell.endDate}`;
  }
}
