import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TmsFunction } from '../models/function.model';
import { ApiMessage } from '../models/people.model';

@Injectable({ providedIn: 'root' })
export class FunctionService {

  private apiUrl = 'api/functions';

  constructor(private http: HttpClient) {}

  getAllFunctions(): Observable<TmsFunction[]> {
    return this.http.get<TmsFunction[]>(this.apiUrl);
  }

  addFunction(fn: Partial<TmsFunction>): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(this.apiUrl, fn);
  }

  updateFunction(fn: TmsFunction): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(this.apiUrl, fn);
  }

  deleteFunction(id: number): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.apiUrl}?id=${id}`);
  }
}
