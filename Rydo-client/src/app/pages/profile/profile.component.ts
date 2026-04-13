import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { UserResponseDTO } from '../../models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  user: UserResponseDTO | null = null;
  loading = true;
  saving = false;
  deleting = false;
  editMode = false;
  success = '';
  error = '';
  showDeleteConfirm = false;

  form = { fullName: '', email: '' };

  constructor(private auth: AuthService, private userService: UserService) {}

  ngOnInit() {
    const userId = this.auth.getUserId();
    if (userId) {
      this.userService.getUserById(userId).subscribe({
        next: u => { this.user = u; this.resetForm(); this.loading = false; },
        error: () => this.loading = false
      });
    }
  }

  resetForm() {
    this.form = { fullName: this.user?.fullName || '', email: this.user?.email || '' };
  }

  startEdit() { this.editMode = true; this.success = ''; this.error = ''; }
  cancelEdit() { this.editMode = false; this.resetForm(); }

  saveProfile() {
    const userId = this.auth.getUserId();
    if (!userId) return;
    this.saving = true;
    this.error = '';
    this.userService.updateUser(userId, this.form).subscribe({
      next: u => {
        this.user = u;
        this.editMode = false;
        this.success = 'Profile updated successfully!';
        this.saving = false;
        setTimeout(() => this.success = '', 3000);
      },
      error: e => { this.error = e.error?.message || 'Update failed'; this.saving = false; }
    });
  }

  confirmDelete() { this.showDeleteConfirm = true; }
  cancelDelete() { this.showDeleteConfirm = false; }

  deleteAccount() {
    const userId = this.auth.getUserId();
    if (!userId) return;
    this.deleting = true;
    this.userService.deleteUser(userId).subscribe({
      next: () => { this.auth.logout(); window.location.href = '/auth'; },
      error: e => { this.error = e.error?.message || 'Delete failed'; this.deleting = false; this.showDeleteConfirm = false; }
    });
  }

  getInitial(name: string): string { return name ? name.charAt(0).toUpperCase() : '?'; }
}
