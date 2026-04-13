import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DriverService } from '../../services/driver.service';
import { DriverResponseDTO } from '../../models';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './driver-profile.component.html',
  styleUrl: './driver-profile.component.css'
})
export class DriverProfileComponent implements OnInit {
  driver: DriverResponseDTO | null = null;
  loading = true;
  saving = false;
  editMode = false;
  success = '';
  error = '';
  notFound = false;

  vehicleTypes = ['ECONOMY', 'COMFORT', 'XL', 'BLACK', 'MOTO', 'AUTO'];
  years = Array.from({ length: 30 }, (_, i) => new Date().getFullYear() - i);

  form = {
    licenseNumber: '',
    vehicleMake: '',
    vehicleModel: '',
    vehicleYear: new Date().getFullYear(),
    vehicleColor: '',
    vehiclePlate: '',
    vehicleType: '',
    cityId: 1
  };

  constructor(private driverService: DriverService) {}

  ngOnInit() {
    this.driverService.getMyProfile().subscribe({
      next: d => { this.driver = d; this.resetForm(); this.loading = false; },
      error: () => { this.notFound = true; this.loading = false; }
    });
  }

  resetForm() {
    if (!this.driver) return;
    this.form = {
      licenseNumber: '',
      vehicleMake: this.driver.vehicleMake || '',
      vehicleModel: this.driver.vehicleModel || '',
      vehicleYear: new Date().getFullYear(),
      vehicleColor: '',
      vehiclePlate: this.driver.vehiclePlate || '',
      vehicleType: this.driver.vehicleType || '',
      cityId: 1
    };
  }

  startEdit() { this.editMode = true; this.success = ''; this.error = ''; }
  cancelEdit() { this.editMode = false; this.resetForm(); }

  save() {
    this.saving = true;
    this.error = '';
    this.driverService.updateProfile(this.form).subscribe({
      next: d => {
        this.driver = d;
        this.editMode = false;
        this.success = 'Driver profile updated!';
        this.saving = false;
        setTimeout(() => this.success = '', 3000);
      },
      error: e => { this.error = e.error?.message || 'Update failed'; this.saving = false; }
    });
  }

  getStatusClass(s: string): string {
    const map: Record<string,string> = {
      VERIFIED: 'badge-verified', PENDING: 'badge-pending',
      REJECTED: 'badge-rejected', UNDER_REVIEW: 'badge-review',
      DOCS_SUBMITTED: 'badge-review', BACKGROUND_CHECK: 'badge-review'
    };
    return map[s] || 'badge-pending';
  }

  getStatusIcon(s: string): string {
    const map: Record<string,string> = {
      VERIFIED: '✅', PENDING: '🕐', REJECTED: '❌',
      UNDER_REVIEW: '🔍', DOCS_SUBMITTED: '📄', BACKGROUND_CHECK: '🔒'
    };
    return map[s] || '🕐';
  }

  isStepDone(step: string): boolean {
    const order = ['PENDING','DOCS_SUBMITTED','UNDER_REVIEW','BACKGROUND_CHECK','VERIFIED'];
    const current = this.driver?.verificationStatus || 'PENDING';
    return order.indexOf(current) > order.indexOf(step);
  }
}

// Helper added at end of class body - patch it properly
