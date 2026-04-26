import { Component } from '@angular/core';
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

  constructor(public translateService: TranslateService) {}

  toggleLanguage(): void {
    const next: Lang = this.translateService.currentLang === 'en' ? 'ta' : 'en';
    this.translateService.setLanguage(next);
  }

  get isTamil(): boolean {
    return this.translateService.currentLang === 'ta';
  }
}
