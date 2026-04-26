import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { People, ApiMessage, InvitedStatus } from '../models/people.model';

@Injectable({ providedIn: 'root' })
export class PeopleService {

  // Change this URL when deploying the backend elsewhere
  private apiUrl = 'api/people';

  constructor(private http: HttpClient) {}

  // Fetch every person in the invitation list
  getAllPeople(): Observable<People[]> {
    return this.http.get<People[]>(this.apiUrl);
  }

  // Fetch people filtered by their invitation status
  getPeopleByStatus(status: InvitedStatus): Observable<People[]> {
    return this.http.get<People[]>(`${this.apiUrl}?type=${status}`);
  }

  // Add a new person to the invitation list
  addPerson(person: People): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(this.apiUrl, person);
  }

  // Update the invitation status of an existing person
  updateInvitedStatus(id: number, invitedStatus: InvitedStatus): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(this.apiUrl, { id, invitedStatus });
  }
}
