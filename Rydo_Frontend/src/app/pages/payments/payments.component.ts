import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payments.component.html',
  styleUrls: ['./payments.component.css'] // ✅ fixed
})
export class PaymentsComponent implements OnInit, OnDestroy {

  tripId!: string;

  // 🔥 dynamic data
  riderId!: string;
  driverId: string | null = null; // ✅ allow null
  amount!: number;

  payment: any;
  loading = true;
  status: 'PENDING' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED' = 'PENDING';

  pollInterval: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService // ✅ renamed for clarity
  ) {}

  ngOnInit() {
    this.tripId = this.route.snapshot.paramMap.get('tripId')!;

    // 🔥 GET DATA FROM NAVIGATION STATE
    const nav = history.state;

    this.riderId = nav?.riderId;
    this.amount = nav?.amount;

    // ✅ get driver from auth
    this.driverId = this.authService.getUserId();

    // ⚠️ FALLBACK IF PAGE REFRESHED
    if (!this.riderId || !this.amount) {
      this.fetchTripDetails();   // 🔥 important
    } else {
      this.createPayment();
    }
  }

  // 🔥 FETCH TRIP DATA IF STATE LOST
  fetchTripDetails() {
    this.http.get<any>(`http://localhost:8082/api/trips/${this.tripId}`)
      .subscribe({
        next: (res) => {
          this.riderId = res.riderId;
          this.amount = res.estimatedFare;
          
          this.createPayment();
        },
        error: () => {
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

    this.http.post<any>('http://localhost:8007/api/payments', payload)
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
    this.http.get<any>(`http://localhost:8007/api/payments/${this.tripId}`)
      .subscribe(res => {
        this.status = res.status;
        this.payment = res;

        if (res.status === 'SUCCEEDED' || res.status === 'FAILED') {
          clearInterval(this.pollInterval);
        }
      });
  }

  onPaymentDone() {
    console.log('Payment completed. Redirecting to dashboard...');
    // Clear the polling interval if still running
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
    // Redirect to dashboard or home page
    this.router.navigate(['/dashboard']);
  }

  ngOnDestroy() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
  }
}