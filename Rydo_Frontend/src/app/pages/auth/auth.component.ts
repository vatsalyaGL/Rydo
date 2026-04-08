import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css'
})
export class AuthComponent {
  step: 'phone' | 'otp' = 'phone';
  phoneNumber = '';
  otp = '';
  loading = false;
  error = '';
  success = '';

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) this.router.navigate(['/dashboard']);
  }

  requestOtp() {
    if (!this.phoneNumber.trim()) { this.error = 'Please enter a phone number'; return; }
    this.loading = true;
    this.error = '';
    this.authService.requestOtp(this.phoneNumber).subscribe({
      next: () => { this.step = 'otp'; this.success = 'OTP sent! Check your phone.'; this.loading = false; },
      error: (e) => { this.error = e.error?.message || 'Failed to send OTP. Try again.'; this.loading = false; }
    });
  }

  verifyOtp() {
    if (!this.otp.trim()) { this.error = 'Please enter the OTP'; return; }
    this.loading = true;
    this.error = '';
    this.authService.verifyOtp(this.phoneNumber, this.otp).subscribe({
      next: () => { this.router.navigate(['/dashboard']); },
      error: (e) => { this.error = e.error?.message || 'Invalid OTP. Please try again.'; this.loading = false; }
    });
  }

  goBack() { this.step = 'phone'; this.error = ''; this.success = ''; this.otp = ''; }
}
