import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DriverApplyDTO, DriverResponseDTO } from '../models';
import { AuthService } from './auth.service';
import { API_URLS } from '../core/api.config';

@Injectable({ providedIn: 'root' })
export class DriverService {
  private readonly BASE = API_URLS.DRIVER_BASE;
  private readonly ADMIN = API_URLS.ADMIN_BASE;

  constructor(private http: HttpClient, private auth: AuthService) {}

  private userIdHeader(): HttpHeaders {
    const userId = this.auth.getUserId();
    const headers: { [key: string]: string } = {};
    if (userId && userId !== 'undefined' && userId !== 'null') {
      headers['X-User-Id'] = userId;
    }
    return new HttpHeaders(headers);
  }

  applyForDriver(data: DriverApplyDTO): Observable<DriverResponseDTO> {
    return this.http.post<DriverResponseDTO>(`${this.BASE}/apply`, data, { headers: this.userIdHeader() });
  }

  getMyProfile(): Observable<DriverResponseDTO> {
    return this.http.get<DriverResponseDTO>(`${this.BASE}/me`, { headers: this.userIdHeader() });
  }

  updateProfile(data: DriverApplyDTO): Observable<DriverResponseDTO> {
    return this.http.put<DriverResponseDTO>(`${this.BASE}/update`, data, { headers: this.userIdHeader() });
  }

  // Admin
  approveDriver(id: string): Observable<string> {
    return this.http.put<string>(`${this.ADMIN}/drivers/${id}/approve`, {}, { responseType: 'text' as any });
  }

  rejectDriver(id: string, reason: string): Observable<string> {
    return this.http.put<string>(`${this.ADMIN}/drivers/${id}/reject`, {}, {
      params: { reason },
      responseType: 'text' as any
    });
  }

  getDriversByStatus(status: string): Observable<DriverResponseDTO[]> {
    return this.http.get<DriverResponseDTO[]>(`${this.ADMIN}/drivers`, {
      params: { status },
      headers: this.userIdHeader()
    });
  }
}
