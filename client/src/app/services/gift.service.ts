import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Gift } from '../models/gift.model';
import { ApiMessage } from '../models/people.model';

@Injectable({ providedIn: 'root' })
export class GiftService {

  private apiUrl = 'api/gifts';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Gift[]> {
    return this.http.get<Gift[]>(this.apiUrl);
  }

  add(gift: Gift): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(this.apiUrl, gift);
  }

  update(gift: Gift): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(this.apiUrl, gift);
  }

  delete(id: number): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.apiUrl}?id=${id}`);
  }
}
