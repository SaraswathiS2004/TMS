import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GuestGroup } from '../models/guest-group.model';
import { ApiMessage } from '../models/people.model';

@Injectable({ providedIn: 'root' })
export class GuestGroupService {

  private apiUrl = 'api/guest-groups';

  constructor(private http: HttpClient) {}

  getAllGroups(): Observable<GuestGroup[]> {
    return this.http.get<GuestGroup[]>(this.apiUrl);
  }

  addGroup(group: Partial<GuestGroup>): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(this.apiUrl, group);
  }

  updateGroup(group: GuestGroup): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(this.apiUrl, group);
  }

  deleteGroup(id: number): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.apiUrl}?id=${id}`);
  }
}
