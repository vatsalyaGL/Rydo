import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { API_URLS } from '../../core/api.config';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payments.component.html',
  styleUrls: ['./payments.component.css']
})
export class PaymentsComponent implements OnInit, OnDestroy {

  tripId!: string;
  riderId!: string;
  driverId: string | null = null;
  amount!: number;
  currentRole: string = '';
  loading = true;
  status: string = 'PENDING';
  payment: any = null;
  error = '';

  pollInterval: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.tripId      = this.route.snapshot.paramMap.get('tripId')!;
    this.currentRole = this.auth.getRole() ?? '';
    this.driverId    = this.auth.getUserId();

    const nav    = history.state;
    this.riderId = nav?.riderId;
    this.amount  = nav?.amount;

    // If state was lost on refresh, fetch trip to get amount
    if (!this.riderId || !this.amount) {
      this.fetchTripDetails();
    } else {
      this.loading = false;   // ← show page immediately, no payment creation needed
    }
  }

  fetchTripDetails() {
    this.http.get<any>(API_URLS.TRIPS_BASE + `/${this.tripId}`)
      .subscribe({
        next: (res) => {
          this.riderId = res.riderId ?? res.rider?.id;
          this.amount  = res.estimatedFare ?? res.finalFare ?? 0;
          this.loading = false;
        },
        error: () => {
          // Even if fetch fails, still show the page so driver can confirm
          this.loading = false;
        }
      });
  }

createPayment() {
    if (!this.driverId) {
      console.error('Driver ID is missing');
      this.loading = false;
      return;
    }

    const payload = {
      tripId: this.tripId,
      riderId: this.riderId,
      driverId: this.driverId,
      amount: this.amount
    };

    this.http.post<any>(API_URLS.PAYMENTS_BASE, payload)
      .subscribe({
        next: (res) => {
          this.payment = res;
          this.status = res.status;
          this.loading = false;

          this.startPolling();
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  startPolling() {
    this.pollInterval = setInterval(() => {
      this.checkStatus();
    }, 3000);
  }

  checkStatus() {
    this.http.get<any>(API_URLS.PAYMENT_BY_TRIP(this.tripId))
      .subscribe(res => {
        this.status = res.status;
        this.payment = res;

        if (res.status === 'SUCCEEDED' || res.status === 'FAILED') {
          clearInterval(this.pollInterval);
        }
      });
  }


  // Called by the confirm button
  onPaymentDone() {
    if (this.pollInterval) clearInterval(this.pollInterval);
    console.log('✅ Navigating to rating for trip:', this.tripId);
    this.router.navigate(['/rating', this.tripId]);
  }

  ngOnDestroy() {
    if (this.pollInterval) clearInterval(this.pollInterval);
  }
}