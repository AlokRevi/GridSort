import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  DayStripItem,
  ScaleBar
} from '../../models/dashboard.models';

@Component({
  selector: 'app-timeline-strip',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './timeline-strip.component.html',
  styleUrl: './timeline-strip.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TimelineStripComponent {
  @Input() dayStrip: DayStripItem[] = [];
  @Input() scaleBar: ScaleBar | null = null;

  private readonly todayIso = this.toIsoDate(new Date());

  getPastCellClass(day: DayStripItem): string {
    const daysPast = this.getDaysPast(day);

    if (daysPast <= 0) {
      return '';
    }

    if (daysPast <= 7) {
      return 'past-recent';
    }

    if (daysPast <= 21) {
      return 'past-mid';
    }

    return 'past-old';
  }

  private getDaysPast(day: DayStripItem): number {
    if (day.date >= this.todayIso) {
      return 0;
    }

    const dayTime = this.toLocalDate(day.date).getTime();
    const todayTime = this.toLocalDate(this.todayIso).getTime();

    return Math.floor((todayTime - dayTime) / 86_400_000);
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private toLocalDate(isoDate: string): Date {
    return new Date(`${isoDate}T00:00:00`);
  }
}
