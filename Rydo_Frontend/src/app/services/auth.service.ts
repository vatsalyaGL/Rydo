import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponseDTO, RequestOtpDTO, VerifyOtpDTO } from '../models';
import { UserService } from './user.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly BASE = 'http://localhost:8081/api/v1/auth';
  private tokenSubject = new BehaviorSubject<string | null>(null);
  token$ = this.tokenSubject.asObservable();

  constructor(private http: HttpClient, private userService: UserService) {
    this.tokenSubject.next(this.getToken());
  }

  requestOtp(phoneNumber: string): Observable<string> {
    return this.http.post<string>(`${this.BASE}/request-otp`, { phoneNumber }, { responseType: 'text' as any });
  }

  verifyOtp(phoneNumber: string, otp: string): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.BASE}/verify-otp`, { phoneNumber, otp }).pipe(
      tap(res => {
        if (typeof localStorage !== 'undefined') {
          localStorage.setItem('token', res.token);
          if (res.userId) {
            localStorage.setItem('userId', res.userId);
          }
          if (res.role) {
            localStorage.setItem('role', res.role);
          }

          if ((!res.userId || !res.role) && phoneNumber) {
            this.userService.getUserByPhone(phoneNumber).subscribe({
              next: u => {
                if (!res.userId && u.id) {
                  localStorage.setItem('userId', u.id);
                }
                if (!res.role && u.role) {
                  localStorage.setItem('role', u.role);
                }
              },
              error: () => console.log('Failed to fetch user by phone')
            });
          }
        }
        this.tokenSubject.next(res.token);
      })
    );
  }

  signInAdmin(uuid: string): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.BASE}/admin/signin`, { uuid }).pipe(
      tap(res => {
        if (typeof localStorage !== 'undefined') {
          localStorage.setItem('token', res.token);
          if (res.userId) localStorage.setItem('userId', res.userId);
          localStorage.setItem('role', res.role || 'ADMIN');
        }
        this.tokenSubject.next(res.token);
      })
    );
  }

  logout() {
    if (typeof localStorage !== 'undefined') {
      localStorage.clear();
    }
    this.tokenSubject.next(null);
  }

  getToken(): string | null {
    return typeof localStorage !== 'undefined' ? localStorage.getItem('token') : null;
  }

  getUserId(): string | null {
    const id = typeof localStorage !== 'undefined' ? localStorage.getItem('userId') : null;
    return id && id !== 'undefined' && id !== 'null' ? id : null;
  }

  getRole(): string | null {
    const role = typeof localStorage !== 'undefined' ? localStorage.getItem('role') : null;
    return role && role !== 'undefined' ? role : null;
  }

  getVehicleType():string|null{
    const role = typeof localStorage !== 'undefined' ? localStorage.getItem('selectedVehicleType') : null;
    return role && role !== 'undefined' ? role : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
