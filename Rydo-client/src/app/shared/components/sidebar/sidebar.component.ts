import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { UserService } from '../../../services/user.service';
import { UserResponseDTO } from '../../../models';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  currentUser: UserResponseDTO | null = null;
  mobileOpen = false;

  allNavItems = [
    { path: '/dashboard', icon: '🏠', label: 'Dashboard', roles: ['USER', 'RIDER', 'DRIVER', 'ADMIN'] },
    { path: '/profile', icon: '👤', label: 'My Profile', roles: ['USER', 'RIDER', 'DRIVER', 'ADMIN'] },
    { path: '/driver/apply', icon: '🚗', label: 'Become a Driver', roles: ['USER', 'RIDER'] },
    { path: '/driver/profile', icon: '🛞', label: 'Driver Profile', roles: ['DRIVER'] },
    { path: '/driver/rides', icon: '🛣️', label: 'Find Riders', roles: ['DRIVER'] },
    { path: '/admin/drivers', icon: '🛡️', label: 'Manage Drivers', roles: ['ADMIN'] },
    { path: '/admin/users', icon: '👥', label: 'Manage Users', roles: ['ADMIN'] },
  ];

  bookRideForm() {
    this.router.navigate(['/form']);
  }

  getARider() {
    this.router.navigate(['/driver/rides']);
  }

  constructor(private auth: AuthService, private userService: UserService, private router: Router) {}

  ngOnInit() {
    const userId = this.auth.getUserId();
    if (userId && userId !== 'undefined') {
      this.userService.getUserById(userId).subscribe({
        next: u => this.currentUser = u,
        error: () => {}
      });
    }
  }

  navItems() {
    const role = this.auth.getRole();
    return role ? this.allNavItems.filter(item => item.roles.includes(role)) : [];
  }

  isRider(): boolean {
    const role = this.auth.getRole();
    return role === 'USER' || role === 'RIDER';
  }

  isDriver(): boolean {
    return this.auth.getRole() === 'DRIVER';
  }

  getInitial(name: string): string {
    return name ? name.charAt(0).toUpperCase() : '?';
  }

  getRoleBadge(role: string): string {
    const map: Record<string, string> = { ADMIN: 'badge-admin', DRIVER: 'badge-driver', USER: 'badge-user', RIDER: 'badge-user' };
    return 'badge ' + (map[role] || 'badge-user');
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/auth']);
  }
}
