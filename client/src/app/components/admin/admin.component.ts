import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, SheetSyncStatus } from '../../services/admin.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit, OnDestroy {

  status: SheetSyncStatus | null = null;
  setupAction: 'create' | 'link' = 'create';
  credentialsFileName = 'google-credentials.json';
  spreadsheetTitle = 'TMS Backup';
  spreadsheetIdInput = '';

  serverMode: 'READ_WRITE' | 'READ_ONLY' = 'READ_WRITE';
  modeMessage = '';
  modeError = '';

  loading = false;
  setupMessage = '';
  setupError = '';
  restoreMessage = '';
  restoreError = '';
  showRestoreConfirm = false;

  private pollInterval: any;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadStatus();
    this.pollInterval = setInterval(() => this.loadStatus(), 30000);
  }

  ngOnDestroy(): void {
    clearInterval(this.pollInterval);
  }

  loadStatus(): void {
    this.adminService.getSheetStatus().subscribe({
      next: (s) => {
        this.status = s;
        if (s.serverMode) {
          this.serverMode = s.serverMode;
        }
      },
      error: () => {}
    });
  }

  onSetMode(): void {
    this.modeMessage = '';
    this.modeError = '';
    this.adminService.setServerMode(this.serverMode).subscribe({
      next: (res) => {
        if (res.status === 'SUCCESS') {
          this.modeMessage = `Mode set to ${this.serverMode === 'READ_WRITE' ? 'Primary' : 'Read Only'}.`;
        } else {
          this.modeError = res.message;
        }
      },
      error: (err) => {
        this.modeError = err.error?.message || 'Failed to update mode.';
      }
    });
  }

  onSetup(): void {
    this.loading = true;
    this.setupMessage = '';
    this.setupError = '';

    const payload: any = { action: this.setupAction, credentialsFileName: this.credentialsFileName };
    if (this.setupAction === 'create') {
      payload.title = this.spreadsheetTitle;
    } else {
      const extractedId = this.extractSpreadsheetId(this.spreadsheetIdInput);
      if (!extractedId) {
        this.loading = false;
        this.setupError = 'Invalid spreadsheet link or ID. Please paste a valid Google Sheets URL or ID.';
        return;
      }
      payload.spreadsheetId = extractedId;
    }

    this.adminService.setupSheet(payload).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.status === 'SUCCESS') {
          this.setupMessage = 'Sheet linked. Full sync started in background.';
          this.loadStatus();
        } else {
          this.setupError = res.message;
        }
      },
      error: (err) => {
        this.loading = false;
        this.setupError = err.error?.message || 'Setup failed. Check credentials file and try again.';
      }
    });
  }

  onSyncNow(): void {
    this.adminService.syncNow().subscribe({
      next: () => {
        setTimeout(() => this.loadStatus(), 2000);
      },
      error: () => {}
    });
  }

  onRestoreConfirm(): void {
    this.showRestoreConfirm = true;
  }

  onRestoreCancel(): void {
    this.showRestoreConfirm = false;
  }

  onRestore(): void {
    this.showRestoreConfirm = false;
    this.restoreMessage = '';
    this.restoreError = '';
    this.loading = true;

    this.adminService.restoreFromSheet().subscribe({
      next: (res) => {
        this.loading = false;
        if (res.status === 'SUCCESS') {
          this.restoreMessage = res.message;
        } else {
          this.restoreError = res.message;
        }
      },
      error: (err) => {
        this.loading = false;
        this.restoreError = err.error?.message || 'Restore failed. Please try again.';
      }
    });
  }

  formatTime(ts: number): string {
    if (!ts) { return 'Never'; }
    return new Date(ts).toLocaleString();
  }

  tableNames(): string[] {
    return this.status?.tableStatuses ? Object.keys(this.status.tableStatuses) : [];
  }

  /**
   * Extracts the spreadsheet ID from a Google Sheets URL, or returns the
   * trimmed input if it already looks like a raw ID.
   *
   * Supported URL shapes:
   *   https://docs.google.com/spreadsheets/d/<ID>/edit#gid=0
   *   https://docs.google.com/spreadsheets/d/<ID>/edit?usp=sharing
   *   https://docs.google.com/spreadsheets/d/<ID>
   *
   * Returns null when no valid ID can be extracted.
   */
  private extractSpreadsheetId(input: string): string | null {
    if (!input) { return null; }
    const trimmed = input.trim();
    if (!trimmed) { return null; }

    // Match /d/<ID> in any Google Sheets URL.
    const urlMatch = trimmed.match(/\/spreadsheets\/d\/([a-zA-Z0-9-_]+)/);
    if (urlMatch && urlMatch[1]) {
      return urlMatch[1];
    }

    // Fallback: input is not a recognizable URL — assume it's already an ID
    // and return it unchanged.
    return trimmed;
  }
}
