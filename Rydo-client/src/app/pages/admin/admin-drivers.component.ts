import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DriverService } from '../../services/driver.service';
import { DriverResponseDTO } from '../../models';

@Component({
  selector: 'app-admin-drivers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-drivers.component.html',
  styleUrl: './admin-drivers.component.css'
})
export class AdminDriversComponent implements OnInit {
  drivers: DriverResponseDTO[] = [];
  loading = false;
  approving: string | null = null;
  rejecting: string | null = null;
  success = '';
  error = '';
  showRejectModal = false;
  rejectReason = '';
  rejectDriverId: string | null = null;

  statuses = ['PENDING', 'DOCS_SUBMITTED', 'UNDER_REVIEW', 'BACKGROUND_CHECK', 'VERIFIED', 'REJECTED'];
  selectedStatus = 'PENDING';

  constructor(private driverService: DriverService) {}

  ngOnInit() { this.fetchDrivers(); }

  fetchDrivers() {
    this.loading = true;
    this.error = '';
    this.driverService.getDriversByStatus(this.selectedStatus).subscribe({
      next: d => { this.drivers = d; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Failed to load drivers'; this.loading = false; }
    });
  }

  approve(id: string) {
    this.approving = id;
    this.error = '';
    this.driverService.approveDriver(id).subscribe({
      next: () => {
        this.success = 'Driver approved successfully!';
        this.approving = null;
        this.fetchDrivers();
        setTimeout(() => this.success = '', 3000);
      },
      error: e => { this.error = e.error?.message || 'Approval failed'; this.approving = null; }
    });
  }

  openRejectModal(id: string) {
    this.rejectDriverId = id;
    this.rejectReason = '';
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectDriverId = null;
    this.rejectReason = '';
  }

  confirmReject() {
    if (!this.rejectReason.trim()) {
      this.error = 'Please provide a reason for rejection.';
      return;
    }
    if (!this.rejectDriverId) return;

    this.rejecting = this.rejectDriverId;
    this.error = '';
    this.driverService.rejectDriver(this.rejectDriverId, this.rejectReason).subscribe({
      next: () => {
        this.success = 'Driver rejected successfully.';
        this.rejecting = null;
        this.closeRejectModal();
        this.fetchDrivers();
        setTimeout(() => this.success = '', 3000);
      },
      error: e => { this.error = e.error?.message || 'Rejection failed'; this.rejecting = null; }
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
}
