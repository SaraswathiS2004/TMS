import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IncomeItem } from '../models/income-item.model';
import { ApiMessage } from '../models/people.model';

@Injectable({ providedIn: 'root' })
export class IncomeService {

  private apiUrl = 'api/income';

  constructor(private http: HttpClient) {}

  getAll(): Observable<IncomeItem[]> {
    return this.http.get<IncomeItem[]>(this.apiUrl);
  }

  add(item: IncomeItem): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(this.apiUrl, item);
  }

  update(item: IncomeItem): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(this.apiUrl, item);
  }

  delete(id: number): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.apiUrl}?id=${id}`);
  }
}
