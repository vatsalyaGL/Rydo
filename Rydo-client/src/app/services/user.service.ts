import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponseDTO, UserCreateDTO, UserUpdateDTO } from '../models';
import { API_URLS } from '../core/api.config';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly BASE = API_URLS.USERS_BASE;

  constructor(private http: HttpClient) {}

  getUserById(id: string): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.BASE}/${id}`);
  }

  getUserByPhone(phoneNumber: string): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.BASE}/phone`, { params: { phoneNumber } });
  }

  createUser(data: UserCreateDTO): Observable<UserResponseDTO> {
    return this.http.post<UserResponseDTO>(this.BASE, data);
  }

  updateUser(id: string, data: UserUpdateDTO): Observable<UserResponseDTO> {
    return this.http.put<UserResponseDTO>(`${this.BASE}/${id}`, data);
  }

  deleteUser(id: string): Observable<string> {
    return this.http.delete<string>(`${this.BASE}/${id}`, { responseType: 'text' as any });
  }
}
