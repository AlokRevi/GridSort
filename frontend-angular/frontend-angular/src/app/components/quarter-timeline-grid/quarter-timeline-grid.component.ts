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
  selector: 'app-quarter-timeline-grid',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './quarter-timeline-grid.component.html',
  styleUrl: './quarter-timeline-grid.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class QuarterTimelineGridComponent {
  @Input() dashboard: TimelineDashboardResponse | null = null;
  @Input() categories: TimelineCategoryResponse[] = [];

  get gridTemplateColumns(): string {
    return `220px repeat(${this.dashboard?.cells.length ?? 0}, 54px)`;
  }

  bucketForCell(
    task: TimelineTaskResponse,
    cell: TimelineCellResponse
  ): TimelineOccurrenceBucketResponse | null {
    return task.buckets.find(bucket => bucket.cellKey === cell.key) ?? null;
  }
}
