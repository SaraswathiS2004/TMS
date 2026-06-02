import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BudgetItem } from '../models/budget-item.model';
import { ApiMessage } from '../models/people.model';

@Injectable({ providedIn: 'root' })
export class BudgetService {

  private apiUrl = 'api/budget';

  constructor(private http: HttpClient) {}

  getAll(): Observable<BudgetItem[]> {
    return this.http.get<BudgetItem[]>(this.apiUrl);
  }

  add(item: BudgetItem): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(this.apiUrl, item);
  }

  update(item: BudgetItem): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(this.apiUrl, item);
  }

  delete(id: number): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.apiUrl}?id=${id}`);
  }
}
