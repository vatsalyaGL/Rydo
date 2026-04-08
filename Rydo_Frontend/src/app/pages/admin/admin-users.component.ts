import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { UserResponseDTO } from '../../models';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent {
  searchPhone = '';
  searchId = '';
  user: UserResponseDTO | null = null;
  loading = false;
  deleting = false;
  success = '';
  error = '';
  showDeleteConfirm = false;
  searched = false;

  constructor(private userService: UserService) {}

  searchByPhone() {
    if (!this.searchPhone.trim()) { this.error = 'Enter a phone number'; return; }
    this.loading = true; this.error = ''; this.user = null; this.searched = false;
    this.userService.getUserByPhone(this.searchPhone).subscribe({
      next: u => { this.user = u; this.loading = false; this.searched = true; },
      error: e => { this.error = e.error?.message || 'User not found'; this.loading = false; this.searched = true; }
    });
  }

  searchById() {
    if (!this.searchId.trim()) { this.error = 'Enter a user ID'; return; }
    this.loading = true; this.error = ''; this.user = null; this.searched = false;
    this.userService.getUserById(this.searchId).subscribe({
      next: u => { this.user = u; this.loading = false; this.searched = true; },
      error: e => { this.error = e.error?.message || 'User not found'; this.loading = false; this.searched = true; }
    });
  }

  confirmDelete() { this.showDeleteConfirm = true; }
  cancelDelete() { this.showDeleteConfirm = false; }

  deleteUser() {
    if (!this.user) return;
    this.deleting = true;
    this.userService.deleteUser(this.user.id).subscribe({
      next: () => {
        this.success = `User "${this.user?.fullName}" deleted successfully.`;
        this.user = null; this.showDeleteConfirm = false; this.deleting = false;
        setTimeout(() => this.success = '', 4000);
      },
      error: e => { this.error = e.error?.message || 'Delete failed'; this.deleting = false; this.showDeleteConfirm = false; }
    });
  }

  getInitial(name: string): string { return name ? name.charAt(0).toUpperCase() : '?'; }

  getRoleClass(role: string): string {
    const map: Record<string,string> = { ADMIN: 'badge-admin', DRIVER: 'badge-driver', USER: 'badge-user' };
    return map[role] || 'badge-user';
  }

  clear() { this.user = null; this.searched = false; this.error = ''; this.searchPhone = ''; this.searchId = ''; }
}
