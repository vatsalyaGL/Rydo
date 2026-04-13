import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RatingService, RatingSummaryDTO } from '../../services/rating.service';
import { AuthService } from '../../services/auth.service';
import { isPlatformBrowser } from '@angular/common';
import { Inject, PLATFORM_ID } from '@angular/core';

@Component({
    selector: 'app-rating-summary',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './rating-summary.component.html',
    styleUrls: ['./rating-summary.component.css']
})
export class RatingSummaryComponent implements OnInit {

    ratingSummary: RatingSummaryDTO | null = null;
    loading = true;
    error = false;

    constructor(
  private ratingService: RatingService,
  private auth: AuthService,
  @Inject(PLATFORM_ID) private platformId: Object
) {}

    ngOnInit() {
  let userId = this.auth.getUserId();

  // ✅ ONLY access localStorage in browser
  if (!userId && isPlatformBrowser(this.platformId)) {
    userId = localStorage.getItem('userId');
  }

  console.log("Rating Summary User ID:", userId);

  if (!userId) {
    this.error = true;
    this.loading = false;
    return;
  }

  this.ratingService.getRatingSummary(userId).subscribe({
    next: (res) => {
      this.ratingSummary = res;
      this.loading = false;
    },
    error: (err) => {
      console.error("API ERROR:", err);
      this.loading = false;
      this.error = true;
    }
  });
}

    get distributionEntries() {
        if (!this.ratingSummary?.scoreDistribution) return [];
        return Object.entries(this.ratingSummary.scoreDistribution)
            .sort((a, b) => Number(b[0]) - Number(a[0]));
    }
}