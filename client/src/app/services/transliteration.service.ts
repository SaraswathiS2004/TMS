import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class TransliterationService {

  constructor(private http: HttpClient) {}

  getSuggestions(text: string): Observable<string[]> {
    const trimmed = text.trim();
    if (!trimmed) { return of([]); }
    const url = `https://inputtools.google.com/request?text=${encodeURIComponent(trimmed)}&itc=ta-t-i0-und&num=5&cp=0&cs=1&ie=utf-8&oe=utf-8&app=jsapi`;
    return this.http.get<any[]>(url).pipe(
      map(resp => {
        if (resp?.[0] === 'SUCCESS') {
          return (resp[1]?.[0]?.[1] as string[]) ?? [];
        }
        return [];
      }),
      catchError(() => of([]))
    );
  }
}
