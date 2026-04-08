import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { DriverService } from '../../services/driver.service';
import { UserResponseDTO, DriverResponseDTO } from '../../models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  user: UserResponseDTO | null = null;
  driverProfile: DriverResponseDTO | null = null;
  loading = true;
  role = '';

  constructor(
    private auth: AuthService,
    private userService: UserService,
    private driverService: DriverService
  ) {}

  ngOnInit() {
    this.role = this.auth.getRole() || 'RIDER';
    const userId = this.auth.getUserId();
    if (userId) {
      this.userService.getUserById(userId).subscribe({
        next: u => {
          this.user = u;
          // Sync role from user object if available
          if (u.role) this.role = u.role;
          this.loading = false;
          if (this.role === 'DRIVER') {
            this.driverService.getMyProfile().subscribe({
              next: d => this.driverProfile = d,
              error: () => {}
            });
          }
        },
        error: () => this.loading = false
      });
    } else {
      this.loading = false;
    }
  }

  getGreeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 17) return 'Good afternoon';
    return 'Good evening';
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      VERIFIED: 'badge-verified',
      PENDING: 'badge-pending',
      REJECTED: 'badge-rejected',
      UNDER_REVIEW: 'badge-review',
      DOCS_SUBMITTED: 'badge-review',
      BACKGROUND_CHECK: 'badge-review'
    };
    return map[status] || 'badge-pending';
  }
}
