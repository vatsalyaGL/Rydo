import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

interface RatingData {
  tripId: string;
  rateeId: string;
  rateeName: string;
  rateeRole: 'RIDER' | 'DRIVER';
  score: number;
  comment: string;
  tags: string[];
}

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rating.component.html',
  styleUrl: './rating.component.css'
})
export class RatingComponent implements OnInit {
  tripId!: string;
  ratingData: RatingData | null = null;
  loading = true;
  submitting = false;

  // Rating form
  score: number = 5;
  comment: string = '';
  selectedTags: string[] = [];

  // Available tags
  availableTags = [
    'Safe Driving', 'Clean Vehicle', 'On Time', 'Friendly', 'Professional',
    'Good Communication', 'Helpful', 'Respectful', 'Fast Service', 'Reliable'
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.tripId = this.route.snapshot.paramMap.get('tripId')!;

    if (!this.tripId) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.loadRatingData();
  }

  loadRatingData() {
    const userId = this.auth.getUserId();
    const userRole = this.auth.getRole();

    if (!userId || !userRole) {
      this.router.navigate(['/auth']);
      return;
    }

    // Get trip details to determine who to rate
    this.http.get<any>(`http://localhost:8082/api/trips/${this.tripId}`)
      .subscribe({
        next: (trip) => {
          let rateeId: string;
          let rateeName: string;
          let rateeRole: 'RIDER' | 'DRIVER';

          if (userRole === 'DRIVER') {
            // Driver rates the rider
            rateeId = trip.riderId;
            rateeName = 'Rider'; // Could fetch actual name from user service
            rateeRole = 'RIDER';
          } else {
            // Rider rates the driver
            rateeId = trip.driverId;
            rateeName = 'Driver'; // Could fetch actual name from user service
            rateeRole = 'DRIVER';
          }

          this.ratingData = {
            tripId: this.tripId,
            rateeId: rateeId,
            rateeName: rateeName,
            rateeRole: rateeRole,
            score: 5,
            comment: '',
            tags: []
          };

          this.loading = false;
        },
        error: (err) => {
          console.error('Error loading trip data:', err);
          this.router.navigate(['/dashboard']);
        }
      });
  }

  toggleTag(tag: string) {
    const index = this.selectedTags.indexOf(tag);
    if (index > -1) {
      this.selectedTags.splice(index, 1);
    } else {
      this.selectedTags.push(tag);
    }
  }

  setScore(newScore: number) {
    this.score = newScore;
  }

  submitRating() {
    if (!this.ratingData || this.submitting) return;

    this.submitting = true;

    const userId = this.auth.getUserId();
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      ...(userId ? { 'X-User-Id': userId } : {})
    });

    const payload = {
      tripId: this.tripId,
      rateeId: this.ratingData.rateeId,
      score: this.score,
      comment: this.comment,
      tags: this.selectedTags
    };

    this.http.post('http://localhost:8010/api/v1/ratings', payload, { headers })
      .subscribe({
        next: () => {
          console.log('Rating submitted successfully');
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          console.error('Error submitting rating:', err);
          this.submitting = false;
          // Could show error message to user
        }
      });
  }

  skipRating() {
    this.router.navigate(['/dashboard']);
  }
}