import { Injectable, inject } from '@angular/core';
import { MessageService } from 'primeng/api';

export type NotifyLevel = 'success' | 'error' | 'info' | 'warn';

@Injectable({ providedIn: 'root' })
export class NotifyService {
  private readonly messages = inject(MessageService);

  show(level: NotifyLevel, message: string, summary?: string): void {
    const sev =
      level === 'success' ? 'success' : level === 'error' ? 'error' : level === 'warn' ? 'warn' : 'info';
    this.messages.add({
      severity: sev,
      summary: summary ?? (level === 'error' ? 'Error' : level === 'success' ? 'Success' : 'Notice'),
      detail: message,
      life: 3500
    });
  }
}
