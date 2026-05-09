import { Component, HostListener, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateService, Lang } from './services/translate.service';
import { TranslatePipe } from './pipes/translate.pipe';
import { AdminService, SheetSyncStatus } from './services/admin.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule, CommonModule, TranslatePipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {

  showInvMenu = false;
  syncStatus: SheetSyncStatus | null = null;

  private syncPollInterval: any;

  constructor(
    public translateService: TranslateService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.pollSyncStatus();
    this.syncPollInterval = setInterval(() => this.pollSyncStatus(), 30000);
  }

  ngOnDestroy(): void {
    clearInterval(this.syncPollInterval);
  }

  private pollSyncStatus(): void {
    this.adminService.getSheetStatus().subscribe({
      next: (s) => { this.syncStatus = s; },
      error: () => {}
    });
  }

  get showSyncWarning(): boolean {
    return !!(this.syncStatus?.configured && !this.syncStatus.lastSyncSuccess);
  }

  toggleInvMenu(): void {
    this.showInvMenu = !this.showInvMenu;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.inv-menu-wrapper')) {
      this.showInvMenu = false;
    }
  }

  toggleLanguage(): void {
    const next: Lang = this.translateService.currentLang === 'en' ? 'ta' : 'en';
    this.translateService.setLanguage(next);
  }

  get isTamil(): boolean {
    return this.translateService.currentLang === 'ta';
  }
}
