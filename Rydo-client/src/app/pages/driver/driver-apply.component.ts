import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DriverService } from '../../services/driver.service';

@Component({
  selector: 'app-driver-apply',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './driver-apply.component.html',
  styleUrl: './driver-apply.component.css'
})
export class DriverApplyComponent implements OnInit {
  loading = false;
  success = '';
  error = '';

  vehicleTypes = ['ECONOMY', 'COMFORT', 'XL', 'BLACK', 'MOTO', 'AUTO'];

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

  years = Array.from({ length: 30 }, (_, i) => new Date().getFullYear() - i);

  constructor(private driverService: DriverService, private router: Router) {}

  ngOnInit() {
    // Check if user already has a driver profile
    this.driverService.getMyProfile().subscribe({
      next: () => {
        // If profile exists, redirect to profile page
        this.router.navigate(['/driver/profile']);
      },
      error: () => {
        // If no profile, allow application
      }
    });
  }

  submit() {
    if (!this.form.licenseNumber || !this.form.vehicleMake || !this.form.vehicleModel ||
        !this.form.vehiclePlate || !this.form.vehicleType) {
      this.error = 'Please fill in all required fields.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.driverService.applyForDriver(this.form).subscribe({
      next: () => {
        this.success = 'Application submitted! Our team will review your details shortly.';
        this.loading = false;
        setTimeout(() => this.router.navigate(['/driver/profile']), 2000);
      },
      error: e => { this.error = e.error?.message || 'Submission failed. Please try again.'; this.loading = false; }
    });
  }
}
