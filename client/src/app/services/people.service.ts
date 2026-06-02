import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { People, ApiMessage } from '../models/people.model';

@Injectable({ providedIn: 'root' })
export class PeopleService {

  private apiUrl = 'api/people';

  constructor(private http: HttpClient) {}

  getAllPeople(): Observable<People[]> {
    return this.http.get<People[]>(this.apiUrl);
  }

  addPerson(person: People): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(this.apiUrl, person);
  }

  updatePerson(person: People): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(this.apiUrl, person);
  }

  updateFunctionInvitations(id: number, invitedFunctionIds: number[]): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(this.apiUrl, { id, invitedFunctionIds });
  }

  updateFunctionStatus(personId: number, functionId: number, status: string): Observable<ApiMessage> {
    return this.http.put<ApiMessage>('api/person-function', { personId, functionId, status });
  }

  // personId is the individual person's (Invitation_Persons) id
  updatePersonInvitedStatus(personId: number, functionId: number, status: string): Observable<ApiMessage> {
    return this.http.put<ApiMessage>('api/invitation-person-function', { personId, functionId, status });
  }

  deletePerson(id: number): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.apiUrl}?id=${id}`);
  }
}
