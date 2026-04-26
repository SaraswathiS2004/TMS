import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';

export type Lang = 'en' | 'ta';

@Injectable({ providedIn: 'root' })
export class TranslateService {

  private translations: Record<string, string> = {};
  private langSubject = new BehaviorSubject<Lang>('en');

  readonly lang$ = this.langSubject.asObservable();

  constructor(private http: HttpClient) {
    const saved = (localStorage.getItem('tms_lang') as Lang) || 'en';
    this.loadLanguage(saved);
  }

  get currentLang(): Lang {
    return this.langSubject.value;
  }

  setLanguage(lang: Lang): void {
    this.loadLanguage(lang);
  }

  translate(key: string): string {
    return this.translations[key] ?? key;
  }

  private loadLanguage(lang: Lang): void {
    this.http.get<Record<string, string>>(`assets/i18n/${lang}.json`).subscribe({
      next: (data) => {
        this.translations = data;
        this.langSubject.next(lang);
        localStorage.setItem('tms_lang', lang);
      }
    });
  }
}
