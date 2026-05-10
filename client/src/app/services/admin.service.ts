import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TableSyncInfo {
  success: boolean;
  error: string | null;
  timestamp: number;
}

export interface SheetSyncStatus {
  configured: boolean;
  spreadsheetId: string | null;
  spreadsheetUrl: string | null;
  lastSyncSuccess: boolean;
  lastSyncError: string | null;
  lastSyncTimestamp: number;
  tableStatuses: { [tableName: string]: TableSyncInfo };
  serverMode: 'READ_WRITE' | 'READ_ONLY' | null;
}

export interface AdminApiMessage {
  status: 'SUCCESS' | 'FAIL';
  message: string;
  spreadsheetId?: string;
  spreadsheetUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {

  private baseUrl = 'api/admin';

  constructor(private http: HttpClient) {}

  getSheetStatus(): Observable<SheetSyncStatus> {
    return this.http.get<SheetSyncStatus>(`${this.baseUrl}/sheet-status`);
  }

  setupSheet(payload: {
    action: 'create' | 'link';
    credentialsFileName: string;
    title?: string;
    spreadsheetId?: string;
  }): Observable<AdminApiMessage> {
    return this.http.post<AdminApiMessage>(`${this.baseUrl}/sheet-setup`, payload);
  }

  restoreFromSheet(): Observable<AdminApiMessage> {
    return this.http.post<AdminApiMessage>(`${this.baseUrl}/restore-from-sheet`, {});
  }

  syncNow(): Observable<AdminApiMessage> {
    return this.http.post<AdminApiMessage>(`${this.baseUrl}/sync-now`, {});
  }

  setServerMode(mode: 'READ_WRITE' | 'READ_ONLY'): Observable<AdminApiMessage> {
    return this.http.post<AdminApiMessage>(`${this.baseUrl}/set-mode`, { mode });
  }
}
