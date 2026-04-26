import { Component, HostListener } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateService, Lang } from './services/translate.service';
import { TranslatePipe } from './pipes/translate.pipe';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule, CommonModule, TranslatePipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

  showInvMenu = false;

  constructor(public translateService: TranslateService) {}

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
