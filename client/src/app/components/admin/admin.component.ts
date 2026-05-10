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
      next: (s) => { this.status = s; },
      error: () => {}
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
      payload.spreadsheetId = this.spreadsheetIdInput;
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
}
